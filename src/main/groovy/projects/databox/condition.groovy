package projects.databox


import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by CXX Diagnosis
 * @author Marvin Schmidtke
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

  recordedDate {
    date = context.source[diagnosis().icdEntry().creationDate()]
  }

  if (context.source[diagnosis().creator().id()]) {
    recorder {
      reference = "Person/" + context.source[diagnosis().creator().id()]
    }
  } else {
    recorder {
      reference = "Person/122"
    }
  }

  code {
    coding {
      system = "urn:centraxx:CodeSystem/IcdCatalog-" + context.source[diagnosis().icdEntry().catalogue().id()]
      code = context.source[diagnosis().icdEntry().code()] as String
      version = context.source[diagnosis().icdEntry().kind()]
    }
  }
}
