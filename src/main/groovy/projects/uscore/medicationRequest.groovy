package projects.uscore


import org.hl7.fhir.r4.model.MedicationRequest

import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

/**
 * Represents a CXX Medication for the US Core Resource Profile: US Core MedicationRequest Profile.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-medicationrequest.html
 *
 * TODO: Work in progress
 *
 * @author Mike Wähnert
 * @since v.1.14.0, CXX.v.2022.1.0
 */
medicationRequest {

  id = "MedicationRequest/" + context.source[medication().id()]

  meta {
    profile "http://hl7.org/fhir/us/core/StructureDefinition/us-core-medicationrequest"
  }

  status = MedicationRequest.MedicationRequestStatus.ACTIVE
  intent = MedicationRequest.MedicationRequestIntent.PROPOSAL

  subject {
    reference = "Patient/" + context.source[medication().patientContainer().id()]
  }

  if (context.source[medication().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[medication().episode().id()]
    }
  }

  authoredOn {
    date = context.source[medication().creationDate()]
  }

  dosageInstruction {
    text = context.source[medication().dosisSchema()] as String
  }

}
