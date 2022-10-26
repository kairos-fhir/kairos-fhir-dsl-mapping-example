package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/complications-covid-19-profile
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * NOTE: Due to the Cardinality-restraint (1..1) for "code", multiple selections in CXX for this parameter
 *       will be added as additional codings.
 */


condition {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2"){
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "SarsCov2_KOMPLIKATIONEN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemBlutinfekt = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_BLUTSTROMINFEKTION" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemBlutinfekt){
    return // no export
  }

  if (crfItemBlutinfekt[CrfItem.CATALOG_ENTRY_VALUE] != []){
    id = "Condition/ComplicationsOfCovid-" + context.source[studyVisitItem().crf().id()] + "-" + crfItemBlutinfekt[CrfItem.ID]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/complications-covid-19"
    }

    crfItemBlutinfekt[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
      final def VERcode = matchResponseToVerificationStatus(item[CatalogEntry.CODE] as String)
      if (VERcode == "261665006") {
        extension {
          url = "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/uncertainty-of-presence"
          valueCodeableConcept {
            coding {
              system = "http://snomed.info/sct"
              code = "261665006"
            }
          }
        }
      }
      else if (["410594000","410605003"].contains(VERcode)){
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
        code = "116223007"
      }
    }

    subject {
      reference = "Patient/Patient-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    recordedDate {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }

  }
  code {
    if (crfItemBlutinfekt[CrfItem.CATALOG_ENTRY_VALUE] != []) {
      crfItemBlutinfekt[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = item[CatalogEntry.CODE] as String
        //if (ICDcode == "COV_JA") {
          coding {
            system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
            version = "2020"
            code = "A41.9"
          }
        //}
      }
      crfItemBlutinfekt[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = item[CatalogEntry.CODE] as String
        //if (SNOMEDcode == "COV_JA") {
          coding {
            system = "http://snomed.info/sct"
            code = "434156008"
          }
        //}
      }
    }
  }
}

static String matchResponseToVerificationStatus(final String resp) {
  switch (resp) {
    case null:
      return null
    case ("COV_NA"):
      return "261665006"
    case ("COV_NEIN"):
      return "410594000"
    default: "410605003"
  }
}
static String matchResponseToVerificationStatusHL7(final String resp) {
  switch (resp) {
    case null:
      return null
    case ("COV_NA"):
      return "unconfirmed"
    case ("COV_NEIN"):
      return "refuted"
    default: "confirmed"
  }
}
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}
