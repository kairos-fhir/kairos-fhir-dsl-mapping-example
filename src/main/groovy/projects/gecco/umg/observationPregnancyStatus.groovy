package projects.gecco.umg

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/pregnancystatus
 * @author Lukas Reinert, Mike Wähnert
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
  if (crfName != "GECCO - DEMOGRAPHIE" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemPreg = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_SCHWANGERSCHAFT" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemPreg) {
    return
  }
  if (crfItemPreg[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Observation/Pregnancy-" + context.source[studyVisitItem().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pregnancy-status"
    }

    status = Observation.ObservationStatus.UNKNOWN

    code {
      coding {
        system = "http://loinc.org"
        code = "82810-3"
      }
    }

    subject {
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }

    valueCodeableConcept {
      crfItemPreg[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def LOINCcode = mapPreg(item[CatalogEntry.CODE] as String)
        if (LOINCcode) {
          coding {
            system = "http://loinc.org"
            code = LOINCcode
          }

        }
      }
      crfItemPreg[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = mapPregSNOMED(item[CatalogEntry.CODE] as String)
        if (SNOMEDcode) {
          coding {
            system = "http://snomed.info/sct"
            code = SNOMEDcode
          }
        }
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String mapPreg(final String smokingStatus) {
  switch (smokingStatus) {
    default:
      return null
    case "COV_JA":
      return "LA15173-0"
    case "COV_NEIN":
      return "LA26683-5"
    case "COV_UNBEKANNT":
      return "LA4489-6"
  }
}

static String mapPregSNOMED(final String smokingStatus) {
  switch (smokingStatus) {
    default:
      return null
    case "COV_JA":
      return "77386006"
    case "COV_NEIN":
      return "60001007"
    case "COV_UNBEKANNT":
      return "261665006"
  }
}
