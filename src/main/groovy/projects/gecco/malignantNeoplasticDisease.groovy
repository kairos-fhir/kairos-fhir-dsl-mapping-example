package projects.gecco


import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis
/**
 * Represented by a CXX Diagnosis
 * @authors Mike Wähnert, Lukas Reinert
 *
 */
condition {

  final String diagnosisCode = context.source[diagnosis().icdEntry().code()] as String
  final boolean hasRelevance = isMalignantNeoplasmaticDisease(diagnosisCode)
  if (hasRelevance) {

    id = "Condition/" + context.source[diagnosis().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/malignant-neoplastic-disease"
    }


    extension {
      url = "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/uncertainty-of-presence"
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

    category {
      coding {
        system = "http://snomed.info/sct"
        code = "394593009"
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
      note {
        text = context.source[diagnosis().comments()] as String
      }
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

static boolean isMalignantNeoplasmaticDisease(final String icdCode) {
  return icdCode.matches("^C.*") //Regex specified in profile
}

static String primaryDiagnosis(final String icdCode, final Boolean isPrimary) {
  return isPrimary ? (icdCode) : null
}
