package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/smokingstatus
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * hints:
 *  A StudyEpisode is no regular episode and cannot reference an encounter
 */
observation {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2") {
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "SarsCov2_ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemSmoke = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_covid19f-dataelement-1240" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemSmoke) {
    return //no export
  }
  if (crfItemSmoke[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Observation/SmokingStatus-" + context.source[studyVisitItem().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/smokingStatus"
    }

    status = Observation.ObservationStatus.UNKNOWN

    code {
      coding {
        system = "http://loinc.org"
        code = "72166-2"
      }
    }

    subject {
      reference = "Patient/Patient-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }


    crfItemSmoke[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
      final def LOINCcode = mapSmokingStatus(item[CatalogEntry.CODE] as String)
      if (LOINCcode) {
        valueCodeableConcept {
          coding {
            system = "http://loinc.org"
            code = LOINCcode
          }
        }
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

//Function to map CXX controlled vocabulary codes to LOINC codes
//Alternatively CXX controlled vocabulary codes could also directly be set to LOINC codes
static String mapSmokingStatus(final String smokingStatus) {
  switch (smokingStatus) {
    case "COV_LA18978-9":
      return "LA18978-9"
    case "COV_LA15920-4":
      return "LA15920-4"
    case "COV_LA18976-3":
      return "LA18976-3"
    case "COV_UNKNOWN":
      return "LA18980-5"
    default:
      return null
  }
}
