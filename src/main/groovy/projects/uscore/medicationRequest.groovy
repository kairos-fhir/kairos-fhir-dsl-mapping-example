package projects.uscore


import org.hl7.fhir.r4.model.MedicationRequest

import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

/**
 * Represents a CXX Medication for the US Core Resource Profile: US Core MedicationRequest Profile.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-medicationrequest.html
 *
 * hints: The "prescribedBy" field of a CXX Medication is just a String. Therefore, a US-CORE Requester
 * can not be referenced.
 *
 * @author Mike Wähnert, Jonas Küttner
 * @since v.1.14.0, CXX.v.2022.1.0
 */

medicationRequest {

  id = "MedicationRequest/" + context.source[medication().id()]

  meta {
    profile "http://hl7.org/fhir/us/core/StructureDefinition/us-core-medicationrequest"
  }

  status = MedicationRequest.MedicationRequestStatus.ACTIVE
  intent = MedicationRequest.MedicationRequestIntent.PROPOSAL

  medication {
    medicationCodeableConcept {
      coding {
        system = "http://fhir.de/CodeSystem/ifa/pzn"
        code = context.source[medication().code()] as String
      }
      text = context.source[medication().name()] as String
    }
  }

  subject {
    reference = "Patient/" + context.source[medication().patientContainer().id()]
  }

  if (context.source[medication().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[medication().episode().id()]
    }
  }

  authoredOn {
    date = context.source[medication().transcriptionDate()]
  }

  requester {
    display = context.source[medication().prescribedBy()]
  }

  dosageInstruction {
    text = context.source[medication().dosisSchema()] as String
  }
  dosageInstruction {
    text = context.source[medication().ordinanceReleaseForm()] as String
  }


}
