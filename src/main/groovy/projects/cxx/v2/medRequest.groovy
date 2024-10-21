package projects.cxx.v2

import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

medicationAdministration {

  id = "Medication/" + context.source[medication().id()]

  if (context.source[medication().parent()]) {
    partOf {
      reference = "MedicationRequest/" + context.source[medication().parent().id]
    }
  }

}

