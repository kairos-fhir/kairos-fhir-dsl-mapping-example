package projects.mii.modul.diagnose

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX DIAGNOSIS
 * Specified by (stable release) https://simplifier.net/medizininformatikinitiative-moduldiagnosen/diagnose v.2.0.0
 * Working draft (future release) https://simplifier.net/guide/MedizininformatikInitiative-ModulDiagnosen-ImplementationGuide/Condition
 * @author Jonas Küttner, Mike Wähnert
 * @since v.1.8.0, CXX.v.3.18.1
 * hints:
 * bodysite might not depicted in CXX
 */

condition {

  if (!context.source[diagnosis().icdEntry()]) {
    return // only ICD 10 supported
  }

  id = "Condition/" + context.source[diagnosis().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-diagnose/StructureDefinition/Diagnose"
    versionId = "2.0.0"
  }

  final def diagnosisId = context.source[diagnosis().diagnosisId()]
  if (diagnosisId) {
    identifier {
      value = diagnosisId
      type {
        coding {
          system = "urn:centraxx"
          code = "diagnosisId"
        }
      }
    }
  }

  clinicalStatus {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/condition-clinical"
      text = "active"
    }
  }

  final List codeDisplay = getVerificationStatus(context.source[diagnosis().diagnosisCertainty()])
  if (codeDisplay) {
    verificationStatus {
      coding {
        code = codeDisplay[0]
        display = codeDisplay[1]
        system = "http://terminology.hl7.org/CodeSystem/condition-ver-status"
      }
    }
  }

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/condition-category"
      code = "encounter-diagnosis"
      display = "Encounter Diagnosis"
    }
  }

  code {
    coding {
      if (context.source[diagnosis().diagnosisCertainty()]) {
        extension {
          url = "http://fhir.de/StructureDefinition/icd-10-gm-diagnosesicherheit"
          valueCoding {
            system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ICD_DIAGNOSESICHERHEIT"
            code = context.source[diagnosis().diagnosisCertainty()] as String
          }
        }
      }

      if (context.source[diagnosis().diagnosisLocalisation()]) {
        extension {
          url = "http://fhir.de/StructureDefinition/seitenlokalisation"
          valueCoding {
            system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ICD_SEITENLOKALISATION"
            code = context.source[diagnosis().diagnosisLocalisation()] as String
          }
        }
      }

      if (context.source[diagnosis().icdEntry().usage()]) {
        extension {
          url = "http://fhir.de/StructureDefinition/icd-10-gm-mehrfachcodierungs-kennzeichen"
          valueCoding {
            system = "http://fhir.de/CodeSystem/icd-10-gm-mehrfachcodierungs-kennzeichen"
            code = mapIcdUsage(context.source[diagnosis().icdEntry().usage()] as String)
          }
        }
      }

      system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
      version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()]
      code = context.source[diagnosis().icdEntry().code()] as String
    }
  }

  subject {
    reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
  }

  onsetDateTime {
    date = context.source[diagnosis().diagnosisDate().date()]
  }

  recordedDate = normalizeDate(context.source[diagnosis().creationDate()] as String)

  if (context.source[diagnosis().creator().id()]) {
    recorder {
      reference = "Person/" + context.source[diagnosis().creator().id()]
    }
  }

  final def comment = context.source[diagnosis().comments()]
  if (comment) {
    note {
      text = comment as String
    }
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
}

static List getVerificationStatus(final Object cxxVerificationState) {
  switch (cxxVerificationState) {
    case "V": return ["provisional", "Provisional"]
    case "A": return ["refuted", "Refuted"]
    case "G": return ["confirmed", "Confirmed"]
    case null: return null
    default: return null
  }
}

static String mapIcdUsage(final String icdEntryUsage) {
  switch (icdEntryUsage) {
    case "aster": return "*"
    case "dagger": return "†"
    case "optional": return "!"
    default: return null
  }
}