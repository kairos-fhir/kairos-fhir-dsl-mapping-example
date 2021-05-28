package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/diagnosiscovid19
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * NOTE: Due to the Cardinality-restraint (1..1) for "code", multiple selections in CXX for this parameter
 *       will be added as additional codings.
 */


condition {
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "KRANKHEITSBEGINN / AUFNAHME" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemStage = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_STAGE_DIAGNOSIS" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemStage){
    return
  }
  if (crfItemStage[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "StageAtDiagnosis/" + context.source[studyVisitItem().crf().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/diagnosis-covid-19"
    }

    category {
      coding {
        system = "http://snomed.info/sct"
        code = "394807007"
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }


    code {
      coding {
        system = "http://snomed.info/sct"
        code = "840539006"
      }
    }
    recordedDate {
      date = normalizeDate(crfItemStage[CrfItem.CREATIONDATE] as String)
    }

    stage {
      summary {
        crfItemStage[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = matchResponseToSNOMED(item[CatalogEntry.CODE] as String)
          if (SNOMEDcode) {
            coding {
              system = "http://snomed.info/sct"
              code = "840539006"
            }
          }
        }
      }
    }

  }
}


static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_UNCOMPLICATED_PHASE"):
      return "255604002"
    case ("COV_COMPLICATED_PHASE"):
      return "6736007"
    case ("COV_CRITICAL_PHASE"):
      return "24484000"
    case ("COV_RECOVERY_PHASE"):
      return "277022003"
    case ("COV_DEAD"):
      return "399166001"
    case ("COV_UNKNOWN"):
      return "261665006"
    default: null
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}