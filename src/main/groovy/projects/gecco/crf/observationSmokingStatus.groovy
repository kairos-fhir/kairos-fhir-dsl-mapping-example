package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import org.hl7.fhir.r4.model.CodeableConcept
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
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemSmoke = context.source[studyVisitItem().crf().items()].find {
    "COV_UMG_AMBU_RAUCHSTATUS" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemSmoke[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Observation/SmokingStatus-" + context.source[studyVisitItem().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/smoking-status"
    }

    status = Observation.ObservationStatus.UNKNOWN

    code {
      coding {
        system = "http://loinc.org"
        code = "72166-2"
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
    }


    crfItemSmoke[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
      final def ICDcode = mapSmokingStatus(item[CatalogEntry.CODE] as String)
      if (ICDcode) {
        valueCodeableConcept {
          coding{
            code = ICDcode
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
    case "COV_UMG_AMBU_JA":
      return "LA18976-3"
    case "COV_UMG_AMBU_NEIN":
      return "LA18978-9"
    default:
      return null
  }
}
