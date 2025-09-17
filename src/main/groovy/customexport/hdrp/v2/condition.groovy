package customexport.hdrp.v2


import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a HDRP Diagnosis
 * @author Mike Wähnert
 * @since v.1.6.0, HDRP.v.3.17.1.7
 */
condition {

  id = "Condition/" + context.source[diagnosis().id()]

  subject {
    reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
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

  final def clinician = context.source[diagnosis().clinician()]
  if (clinician) {
    recorder {
      identifier {
        display = clinician
      }
    }
  }

  onsetDateTime {
    date = context.source[diagnosis().diagnosisDate().date()]
  }

  code {
    coding {
      system = "urn:centraxx:CodeSystem/IcdCatalog-" + context.source[diagnosis().icdEntry().catalogue().id()]
      code = context.source[diagnosis().icdEntry().code()] as String
      version = context.source[diagnosis().icdEntry().kind()]
    }
  }
}

