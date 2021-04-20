package projects.gecco


import org.hl7.fhir.r4.model.Annotation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Diagnosis
 * @author Lukas Reinert
 * @since CXX.v.3.18.0
 */
condition {

  final String diagnosisCode = context.source[diagnosis().icdEntry().code()] as String
  final boolean hasRelevance = isComplication(diagnosisCode)
  if (hasRelevance) {

    id = "ComplicationsOfCovid/" + context.source[diagnosis().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/complications-covid-19"
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
        code = "404989005"
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

    stage {
      summary {
        coding {
          system = "http://snomed.info/sct"
        }
      }
    }

    if (context.source[diagnosis().comments()]) {
      final Annotation annot = new Annotation()
      annot.setText(context.source[diagnosis().comments()] as String)
      note.add(annot)
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

static boolean isComplication(final String icdCode) {
  return ["J18.9",
          "A41.9",
          "I82.9",
          "I26.9",
          "I64",
          "I21.9",
          "N17.9"].contains(icdCode) //Regex specified in profile
}

static String primaryDiagnosis(final String icdCode, final Boolean isPrimary) {
  return isPrimary ? (icdCode) : null
}
