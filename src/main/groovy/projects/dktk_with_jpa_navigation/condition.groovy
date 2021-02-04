package projects.dktk_with_jpa_navigation


import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ID
import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Diagnosis
 * Specified by https://simplifier.net/oncology/primaerdiagnose
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 */
condition {

  id = "Condition/" + context.source[diagnosis().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Condition-Primaerdiagnose"
  }

  subject {
    reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
  }

  if (context.source[diagnosis().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[diagnosis().episode().id()]
    }
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
      system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
      code = context.source[diagnosis().icdEntry().code()] as String
      version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()]
    }
  }

  context.source[diagnosis().samples()]?.each { final sample ->
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-Specimen"
      valueReference {
        reference = "Specimen/" + sample[ID]
      }
    }
  }
}
