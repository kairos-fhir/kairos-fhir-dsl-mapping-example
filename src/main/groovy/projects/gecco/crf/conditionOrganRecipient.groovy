package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/organrecipient
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
  if (crfName != "SarsCov2_ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemOrgan = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_ORGANTRANSPLANIERT" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemOrgan) {
    return
  }
  if (crfItemOrgan[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Condition/HistoryOfOrganTransplant-" + context.source[studyVisitItem().crf().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/organ-recipient"
    }

    crfItemOrgan[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
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
        code = "788415003"
      }
    }

    subject {
      reference = "Patient/Patient-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    code {
      crfItemOrgan[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = matchResponseToICD(item[CatalogEntry.CODE] as String)
        if (ICDcode) {
          coding {
            system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
            version = "2020"
            code = ICDcode
          }
        }
      }
      crfItemOrgan[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = matchResponseToSNOMED(item[CatalogEntry.CODE] as String)
        if (SNOMEDcode) {
          coding {
            system = "http://snomed.info/sct"
            code = SNOMEDcode
          }
        }
      }

    }
    recordedDate {
      date = normalizeDate(crfItemOrgan[CrfItem.CREATIONDATE] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }
}

static String matchResponseToICD(final String resp) {
  switch (resp) {
    case ("COV_HERZ"):
      return "Z94.1"
    case ("COV_LUNGE"):
      return "Z94.2"
    case ("COV_LEBER"):
      return "Z94.4"
    case ("COV_NIEREN"):
      return "Z94.0"
    case ("COV_DARM"):
      return "Z94.88"
    case ("COV_HAUT"):
      return "Z94.5"
    case ("COV_HORNHAUT"):
      return "Z94.7"
    case ("COV_HERZKLAPPEN"):
      return "Z95.5"
    case ("COV_BLUTGEFAESSE"):
      return "Z95.88"
    case ("COV_KNOCHENGEWEBE"):
      return "Z94.6"
    default: null
  }
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_HERZ"):
      return "302509004"
    case ("COV_LUNGE"):
      return "181216001"
    case ("COV_LEBER"):
      return "181268008"
    case ("COV_NIEREN"):
      return "181414000"
    case ("COV_DARM"):
      return "181254001"
    case ("COV_HAUT"):
      return "119181002"
    case ("COV_HORNHAUT"):
      return "181162001"
    case ("COV_GEHOERKNOECHELCHEN"):
      return "41845008"
    case ("COV_HERZKLAPPEN"):
      return "181285005"
    case ("COV_BLUTGEFAESSE"):
      return "119206002"
    case ("COV_HIRNHAUT"):
      return "8935007"
    case ("COV_KNOCHENGEWEBE"):
      return "3138006"
    case ("COV_KNORPELGEWEBE"):
      return "309312004"
    case ("COV_SEHNE"):
      return "13024002"
    case ("COV_NEIN"):
      return "161663000" //generic code for organ recipient
    case ("COV_UNBEKANNT"):
      return "161663000" //generic code for organ recipient
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
