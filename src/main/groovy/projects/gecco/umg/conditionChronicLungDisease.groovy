package projects.gecco.umg

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/chroniclungdiseases
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * NOTE: Due to the Cardinality-restraint (1..1) for "code", multiple selections in CXX for this parameter
 *       will be added as additional codings.
 */
condition {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2") {
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "GECCO - ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemLung = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_LUNGENERKRANKUNG" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemLung[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Condition/ChronicLungDisease-" + context.source[studyVisitItem().crf().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-lung-diseases"
    }

    crfItemLung[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
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
        code = "418112009"
      }
    }

    subject {
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }


    code {
      /*crfItemLung[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = matchResponseToICD(item[CatalogEntry.CODE] as String)
        if (ICDcode) {
          coding {
            system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
            version = "2020"
            code = ICDcode
          }
        }
      }*/
      crfItemLung[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = matchResponseToSNOMED(item[CatalogEntry.CODE] as String)
        if (SNOMEDcode) {
          coding {
            system = "http://snomed.info/sct"
            code = SNOMEDcode
          }
        }
        else {
          coding {
            extension{
              url = "http://hl7.org/fhir/StructureDefinition/data-absent-reason"
              valueCode = "unsupported"
            }
          }
        }
      }
    }
    recordedDate {
      date = normalizeDate(crfItemLung[CrfItem.CREATIONDATE] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }
}

/*
static String matchResponseToICD(final String resp) {
  switch (resp) {
    case ("COV_ASTHMA"):
      return "J45.9"
    case ("COV_COPD"):
      return "J44.9"
    case ("COV_LUNGENFIBROSE"):
      return "J84.1"
    case ("COV_PUL_HYPERTONII"):
      return "I27.0"
    case ("COV_OHS"):
      return "E66.29"
    case ("COV_SCHLAFAPNOE"):
      return "G47.3"
    case ("COV_OSAS"):
      return "G47.31"
    case ("COV_CYSTISCHE_FIBR"):
      return "E84.9"
    case ("COV_ANDERE"):
      return "J44.9"
    case ("COV_UNBEKANNT"):
      return "Unknown"
    default: null
  }
}*/

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_ASTHMA"):
      return "195967001"
    case ("COV_COPD"):
      return "13645005"
    case ("COV_LUNGENFIBROSE"):
      return "51615001"
    case ("COV_PUL_HYPERTONII"):
      return "70995007"
    case ("COV_OHS"):
      return "190966007"
    case ("COV_SCHLAFAPNOE"):
      return "73430006"
    case ("COV_OSAS"):
      return "78275009"
    case ("COV_CYSTISCHE_FIBR"):
      return "190905008"
    case ("COV_UNBEKANNT"):
      return "413839001" //Generic chronic lung disease
    case ("COV_NEIN"):
      return "413839001" //Generic chronic lung disease
    case ("COV_ANDERE"):
      return "413839001" //Generic chronic lung disease
    default: null
  }
}

static String matchResponseToVerificationStatus(final String resp) {
  switch (resp) {
    case null:
      return null
    case ("COV_UNBEKANNT"):
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
    case ("COV_UNBEKANNT"):
      return "unconfirmed"
    case ("COV_NEIN"):
      return "refuted"
    default: "confirmed"
  }
}
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}
