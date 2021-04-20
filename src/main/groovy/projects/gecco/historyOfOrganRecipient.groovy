package projects.gecco


import org.hl7.fhir.r4.model.Annotation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Diagnosis
 * @author Lukas Reinert
 * @since v.1.7.0, CXX.v.3.17.2
 */

condition {

  final String diagnosisCode = context.source[diagnosis().icdEntry().code()] as String
  final boolean hasRelevance = isRelevantDisease(diagnosisCode)
  if (hasRelevance) {

    extension {
      url = "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/uncertainty-of-presence"
      valueCodeableConcept {
        coding {
          system = "http://snomed.info/sct"
          code = "261665006"
        }
      }
    }

    meta {
      profile "https://simplifier.net/forschungsnetzcovid-19/organrecipient"
    }

    id = "HistoryOfOrganRecipient/" + context.source[diagnosis().id()]

    clinicalStatus {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/condition-clinical"
        text = "active"
      }
    }

    verificationStatus {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/condition-ver-status"
        code = conditionVerificationStatusMapping(context.source[diagnosis().diagnosisCertainty()] as String)
      }
      coding {
        system = "http://snomed.info/sct"
        code = conditionVerificationStatusMappingSNOMED(context.source[diagnosis().diagnosisCertainty()] as String)
      }
    }

    category {
      coding {
        system = "http://snomed.info/sct"
        code = "788415003"
      }
    }

    code {
      coding {
        if (context.source[diagnosis().diagnosisCertainty()]) {
          extension {
            coding {
              url = "http://fhir.de/StructureDefinition/icd-10-gm-diagnosesicherheit"
              valueCode = context.source[diagnosis().diagnosisCertainty()] as String
            }
          }
        }
        extension {
          coding {
            url = "http://fhir.de/StructureDefinition/seitenlokalisation"
            valueCoding {
              code = "UNKNOWN"
            }
          }
        }
        extension {
          coding {
            url = "http://fhir.de/StructureDefinition/icd-10-gm-primaercode"
            code = primaryDiagnosis(diagnosisCode, context.source[diagnosis().parent()] as Boolean)
          }
        }
        system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
        version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()]
        code = diagnosisCode
      }
    }
    bodySite {
      coding {
        system = "http://snomed.info/sct"
        code = "UNKNOWN"
      }
    }

    subject {
      reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
    }

    if (context.source[diagnosis().episode()]) {
      encounter {
        reference = "Encounter/" + context.source[diagnosis().episode().id()]
      }
    }

    onsetDateTime {
      date = context.source[diagnosis().diagnosisDate().date()]
    }

    recordedDate = normalizeDate(context.source[diagnosis().creationDate()] as String)

    if (context.source[diagnosis().comments()]) {
      final Annotation annotation = new Annotation()
      annotation.setText(context.source[diagnosis().comments()] as String)
      note.add(annotation)
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}

static String conditionVerificationStatusMappingSNOMED(final String diagCertainty) {
  switch (diagCertainty) {
    case ("G"):
      return "410605003"
    case ("A"):
      return "410594000"
    default: null
  }
}

static String conditionVerificationStatusMapping(final String diagCertainty) {
  switch (diagCertainty) {
    case ("V"):
      return "provisional"
    case ("G"):
      return "confirmed"
    case ("A"):
      return "entered-in-error"
    default: null
  }
}

static boolean isOrganTransplant(final String icdCode) {
  return ["Z94.1",
          "Z94.2",
          "Z94.4",
          "Z94.0",
          "Z94.88",
          "Z94.5",
          "Z94.7",
          "Z95.5",
          "Z95.88",
          "Z94.6",
          "Z94.9",
          "Z94.80",
          "Z94.81",
          "Z94.3"].contains(icdCode)
}

static String primaryDiagnosis(final String icdCode, final Boolean isPrimary) {
  return isPrimary ? (icdCode) : null
}

static boolean isRelevantDisease(final String icdCode) {
  return isOrganTransplant(icdCode)
}
