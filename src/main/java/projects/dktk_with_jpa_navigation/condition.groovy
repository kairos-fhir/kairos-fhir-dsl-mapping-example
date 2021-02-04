package projects.dktk_with_jpa_navigation

import de.kairos.fhir.centraxx.metamodel.AbstractSample
import de.kairos.fhir.centraxx.metamodel.Diagnosis
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.RootEntities
import de.kairos.fhir.centraxx.metamodel.SampleAbstraction

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample
import static de.kairos.fhir.centraxx.metamodel.SampleAbstraction.*

/**
 * Represented by a CXX Diagnosis
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.2
 * TODO: Extension Fernmetastasen
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
      display = context.source["icdEntry.name"]
    }
  }
diagnosis().icdEntry().
  //TODO: "icdEntry.catalogue.version" catalogueVersion and version the same?
  //TODO: "icdEntry.name" ?

  context.source[diagnosis().samples()]?.each { final sample ->
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-Specimen"
      valueReference {
        reference = "Specimen/" + sample[ID]
      }
    }
  }
}
