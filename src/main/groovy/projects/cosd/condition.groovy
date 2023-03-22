package projects.cosd

import ca.uhn.fhir.model.api.TemporalPrecisionEnum

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Diagnosis
 * @author Mike Wähnert
 * @since v.1.6.0, CXX.v.3.17.1.7
 */
condition {

  id = "Condition/" + context.source[diagnosis().id()]

  subject {
    reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
  }

  encounter {
    reference = "Encounter" + context.source[diagnosis().episode().id()]
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

  if (context.source[diagnosis().diagnosisDate().date()]) {
    onsetDateTime {
      date = context.source[diagnosis().diagnosisDate().date()]
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }

  code {
    coding {
      system = context.source[diagnosis().icdEntry().catalogue().name()]
      code = context.source[diagnosis().icdEntry().code()] as String
      version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()]
    }
  }
}

