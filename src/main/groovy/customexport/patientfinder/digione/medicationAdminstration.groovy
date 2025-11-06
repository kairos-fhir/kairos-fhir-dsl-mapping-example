package customexport.patientfinder.digione


import org.hl7.fhir.r4.model.MedicationAdministration

import javax.annotation.Nullable

import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

medicationAdministration {

  id = "MediationAdministration/" + context.source[medication().id()]

  identifier {
    value = context.source[medication().fillerOrderNumber()]
  }

  status = MedicationAdministration.MedicationAdministrationStatus.COMPLETED

  subject {
    reference = "Patient/" + context.source[medication().patientContainer().id()]
  }

  request {
    reference = "MedicationRequest/" + context.source[medication().id()]
  }

  medicationCodeableConcept {
    coding {
      code = context.source[medication().code()] as String
      display = context.source[medication().name()]
    }
  }

  encounter {
    reference = "Encounter/" + context.source[medication().episode().id()]
  }

  if (context.source[medication().attendingDoctor()]) {
    performer {
      reference = "Practitioner/" + context.source[medication().attendingDoctor().id()]
    }
  }

  if (context.source[medication().trgDate()]) {
    effectiveDateTime = context.source[medication().trgDate()]
  }

  dosage {
    dose {
      value = sanitizeScale(context.source[medication().dosis()] as String)
      unit = context.source[medication().unit().code()]
    }
  }
}

@Nullable
static BigDecimal sanitizeScale(final String numeric) {
  try {
    return BigDecimal.valueOf(Double.parseDouble(numeric))
  } catch (final NumberFormatException | NullPointerException ignored) {
    return null
  }
}

