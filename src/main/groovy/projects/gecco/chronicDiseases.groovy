package projects.gecco


import org.hl7.fhir.r4.model.Annotation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Diagnosis
 * @authors Mike Wähnert, Lukas Reinert
 *
 * Maps profiles:
 *  - Cardiovascular Disease
 *  - Chronic Kidney Disease
 *  - Chronic Liver Disease
 *  - Chronic Lung Disease
 *  - Chronic Neurological or Mental Disease
 *  - Diabetes Mellitus
 *  - Gastrointestinal Ulcers
 *  - Human Immunodeficiency Virus Disease
 *  - Rheumatic Immunological Diseases
 */
condition {

  final String diagnosisCode = context.source[diagnosis().icdEntry().code()] as String
  final boolean hasRelevance = isRelevantDisease(diagnosisCode)
  if (hasRelevance) {

    id = "Condition/" + context.source[diagnosis().id()]

    extension {
      url = "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/uncertainty-of-presence"
      valueCodeableConcept {
        coding {
          system = "http://snomed.info/sct"
          code = "261665006"
        }
      }
    }

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
            text = "-"
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


    if (isCardiovascularDisease(diagnosisCode)) {
      meta {
        profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/cardiovascular-diseases"
      }

      category {
        coding {
          system = "http://snomed.info/sct"
          code = "722414000"
        }
      }
    }

    if (isKidneyDisease(diagnosisCode)) {
      meta {
        profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-kidney-diseases"
      }

      category {
        coding {
          system = "http://snomed.info/sct"
          code = "394589003"
        }
      }
    }

    if (isLiverDisease(diagnosisCode)) {
      meta {
        profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-liver-diseases"
      }

      category {
        coding {
          system = "http://snomed.info/sct"
          code = "408472002"
        }

      }
    }

    if (isLungDisease(diagnosisCode)) {
      meta {
        profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-lung-diseases"
      }

      category {
        coding {
          system = "http://snomed.info/sct"
          code = "418112009"
        }
      }
    }

    if (isNeurologicalDisease(diagnosisCode)) {
      meta {
        profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-neurological-mental-diseases"
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
    }

    if (isDiabetes(diagnosisCode)) {
      meta {
        profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/diabetes-mellitus"
      }
      category {
        coding {
          system = "http://snomed.info/sct"
          code = "408475000"
        }
      }
    }

    if (isGastrointestinalUlcer(diagnosisCode)) {
      meta {
        profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/gastrointestinal-ulcers"
      }
      category {
        coding {
          system = "http://snomed.info/sct"
          code = "394584008"
        }
      }
    }

    if (isHIV(diagnosisCode)) {
      meta {
        profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/gastrointestinal-ulcers"
      }
      category {
        coding {
          system = "http://snomed.info/sct"
          code = "394807007"
        }
      }
    }

    if (isRheumaticImmunologicDisease(diagnosisCode)) {
      meta {
        profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/rheumatological-immunological-diseases"
      }
      category {
        coding {
          system = "http://snomed.info/sct"
          code = "394810000"
        }
      }
      category {
        coding {
          system = "http://snomed.info/sct"
          code = "408480009"
        }
      }
    }


    if (context.source[diagnosis().diagnosisLocalisation()]) {
      bodySite {
        coding {
          system = "http://snomed.info/sct"
          code = context.source[diagnosis().diagnosisLocalisation()] as String
        }
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
    if (context.source[diagnosis().creator().id()]) {
      recorder {
        reference = "Person/" + context.source[diagnosis().creator().id()]
      }
    }

    if (context.source[diagnosis().comments()]) {
      final Annotation annotation = new Annotation()
      annotation.setText(context.source[diagnosis().comments()] as String)
      note.add(annotation)
    }
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
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

static boolean isCardiovascularDisease(final String icdCode) {
  return ["I25.29", "I10.90", "I73.9", "I49.9", "I50.9", "I25.1", "I65.2", "Z95.1", "Z95.5"].contains(icdCode)
}

static boolean isKidneyDisease(final String icdCode) {
  return ["N18.9", "N18.5", "N18.1", "N18.2", "N18.3", "N18.4", "N18.5", "Z99.2"].contains(icdCode)
}

static boolean isLiverDisease(final String icdCode) {
  return ["K76.0", "K70.0", "K74.6", "B18.9", "K76.9"].contains(icdCode)
}

static boolean isLungDisease(final String icdCode) {
  return ["G47.3", "G47.31", "E66.29", "J84.1", "A16.2", "J60", "J61", "J64", "J66.0", "J67.0",
          "J67.1", "J68.4", "J70.1", "J70.4", "P27.8", "Z87.0", "J44.9", "J45.9", "E84.9"].contains(icdCode)
}

static boolean isNeurologicalDisease(final String icdCode) {
  return ["F99", "G20", "G35", "G96.9", "F41.9", "F32.9", "F29", "F03", "G70.9", "G40.9", "G43.9", "I69.4", "Z86.7"].contains(icdCode)
}

static boolean isDiabetes(final String icdCode) {
  return ["E11.9", "E10.9", "E13.9"].contains(icdCode)
}

static boolean isGastrointestinalUlcer(final String icdCode) {
  return ["E11.9", "E10.9", "E13.9"].contains(icdCode)
}

static boolean isHIV(final String icdCode) {
  return ["B20", "B21", "B22", "B24"].contains(icdCode)
}

static boolean isRheumaticImmunologicDisease(final String icdCode) {
  return ["K52.9", "M06.99", "M35.9", "I77.6", "D84.8"].contains(icdCode)
}

static String primaryDiagnosis(final String icdCode, final Boolean isPrimary) {
  return isPrimary ? (icdCode) : null
}

static boolean isRelevantDisease(final String icdCode) {
  return isCardiovascularDisease(icdCode) ||
      isKidneyDisease(icdCode) ||
      isLiverDisease(icdCode) ||
      isLungDisease(icdCode) ||
      isNeurologicalDisease(icdCode) ||
      isDiabetes(icdCode) ||
      isGastrointestinalUlcer(icdCode) ||
      isHIV(icdCode) ||
      isRheumaticImmunologicDisease(icdCode)
}
