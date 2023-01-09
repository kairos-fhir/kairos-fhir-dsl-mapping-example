package projects.cxx.custom.aggregate

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX DIAGNOSIS
 * dummy script for aggregate bundle demonstration
 * @author Jonas Küttner
 */

condition {

  if (!context.source[diagnosis().icdEntry()]) {
    return // only ICD 10 supported
  }

  id = "Condition/" + context.source[diagnosis().id()]

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

  subject {
    reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
  }

}
