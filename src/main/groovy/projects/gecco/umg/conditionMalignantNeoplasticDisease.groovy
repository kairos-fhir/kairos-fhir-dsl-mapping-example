package projects.gecco.umg

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem
/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/malignantneoplasticdisease
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 */


condition {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2"){
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "GECCO - ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemCancer = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_TUMORERKRANKUNG" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemCancer){
    return
  }
  if (crfItemCancer[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Condition/MalignantNeoplasticDisease-" + context.source[studyVisitItem().crf().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/malignant-neoplastic-disease"
    }

    crfItemCancer[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
      final def clinicalStatusCode = matchResponseToClinicalStatus(item[CatalogEntry.CODE] as String)
      if (clinicalStatusCode) {
        clinicalStatus {
          coding{
            system = "http://terminology.hl7.org/CodeSystem/condition-clinical"
            code = clinicalStatusCode
          }
        }
      }
    }
    crfItemCancer[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
      final def VERcode = matchResponseToVerificationStatus(item[CatalogEntry.CODE] as String)
      if (VERcode == "261665006") {
        modifierExtension {
          url = "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/uncertainty-of-presence"
          valueCodeableConcept {
            coding {
              system = "http://snomed.info/sct"
              code = "261665006"
            }
          }
        }
      } else if (["410594000", "410605003"].contains(VERcode)) {
        verificationStatus {
          coding {
            system = "http://snomed.info/sct"
            code = VERcode
          }
          coding {
            system = "http://terminology.hl7.org/CodeSystem/condition-ver-status"
            code = matchResponseToVerificationStatusHL7(item[CatalogEntry.CODE] as String)
          }
        }
      }
    }
    category {
      coding {
        system = "http://snomed.info/sct"
        code = "394593009"
      }
    }

    subject {
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    code {
      coding {
        system = "http://snomed.info/sct"
        code = "363346000"
        display = "Malignant neoplastic disease"
      }
    }

    recordedDate {
      date = normalizeDate(crfItemCancer[CrfItem.CREATIONDATE] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }
}


static String matchResponseToClinicalStatus(final String resp) {
  switch (resp) {
    case ("COV_AKTIV"):
      return "active"
    case ("COV_REMISSION"):
      return "remission"
    default: null
  }
}
static String matchResponseToVerificationStatus(final String resp) {
  switch (resp) {
    case ("COV_AKTIV"):
      return "410605003"
    case ("COV_REMISSION"):
      return "410605003"
    case ("COV_NEIN"):
      return "410594000"
    case ("COV_UNKNOWN"):
      return "261665006"
    default: null
  }
}
static String matchResponseToVerificationStatusHL7(final String resp) {
  switch (resp) {
    case ("COV_AKTIV"):
      return "confirmed"
    case ("COV_REMISSION"):
      return "confirmed"
    case ("COV_NEIN"):
      return "refuted"
    case ("COV_UNKNOWN"):
      return "unconfirmed"
    default: null
  }
}







static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}
