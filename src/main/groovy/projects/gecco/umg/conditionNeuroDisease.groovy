package projects.gecco.umg

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/guide/GermanCoronaConsensusDataSet-ImplementationGuide/Chronicneurologicalormentaldiseases
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
  final def crfItemNeuro = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_NEURO_ERKRANKUNG" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemNeuro) {
    return
  }
  if (crfItemNeuro[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Condition/NeuroDisease-" + context.source[studyVisitItem().crf().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-neurological-mental-diseases"
    }

    crfItemNeuro[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
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
        code = "394591006"
      }
      coding {
        system = "http://snomed.info/sct"
        code = "394587001"
      }
    }

    subject {
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }


    code {
      /*crfItemNeuro[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = matchResponseToICD(item[CatalogEntry.CODE] as String)
        if (ICDcode) {
          coding {
            system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
            version = "2020"
            code = ICDcode
          }
        }
      }*/
      crfItemNeuro[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
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
      date = normalizeDate(crfItemNeuro[CrfItem.CREATIONDATE] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }
}

/*
static String matchResponseToICD(final String resp) {
  switch (resp) {
    case ("COV_ANGSTERKRANKUNG"):
      return "F41.9"
    case ("COV_DEPRESSION"):
      return "F32.9"
    case ("COV_PSYCHOSE"):
      return "F29"
    case ("COV_PARKINSON"):
      return "G20"
    case ("COV_DEMENZ"):
      return "F03"
    case ("COV_MS"):
      return "G35"
    case ("COV_NEUROMUSK_ERKRANKUNG"):
      return "G70.9"
    case ("COV_EPILEPSIE"):
      return "G40.9"
    case ("COV_MIGRAENE"):
      return "G43.9"
    default: null
  }
}*/

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_ANGSTERKRANKUNG"):
      return "197480006"
    case ("COV_DEPRESSION"):
      return "35489007"
    case ("COV_PSYCHOSE"):
      return "69322001"
    case ("COV_PARKINSON"):
      return "49049000"
    case ("COV_DEMENZ"):
      return "52448006"
    case ("COV_MS"):
      return "24700007"
    case ("COV_NEUROMUSK_ERKRANKUNG"):
      return "257277002"
    case ("COV_EPILEPSIE"):
      return "84757009"
    case ("COV_MIGRAENE"):
      return "37796009"
    case ("COV_APOPLEX_RESIDUEN"):
      return "440140008"
    case ("COV_APOPLEX_O_RESIDUEN"):
      return "429993008"
    case ("COV_UNBEKANNT"):
      return "128283000" //generic code for chronic nervous system disorder
    case ("COV_NEIN"):
      return "128283000" //generic code for chronic nervous system disorder
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
