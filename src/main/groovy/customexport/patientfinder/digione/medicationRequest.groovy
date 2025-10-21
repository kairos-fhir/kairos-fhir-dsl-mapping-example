package customexport.patientfinder.digione


import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.enums.MedicationServiceType
import org.hl7.fhir.r4.model.MedicationRequest

import javax.annotation.Nullable

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

/**
 * Represents a HDRP Medication
 *
 * @author Mike Wähnert, Jonas Küttner
 * @since v.1.52.0, HDRP.v.2025.3.0
 */


medicationRequest {

  if (context.source[medication().serviceType()] != MedicationServiceType.VER.name()) {
    return
  }

  id = "MedicationRequest/" + context.source[medication().id()]

  identifier {
    value = context.source[medication().fillerOrderNumber()]
  }

  status = MedicationRequest.MedicationRequestStatus.COMPLETED

  subject {
    reference = "Patient/" + context.source[medication().patientContainer().id()]
  }

  medicationCodeableConcept {
    coding {
      code = context.source[medication().code()] as String
    }
  }

  if (!isFakeEpisode(context.source[medication().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[medication().episode().id()]
    }
  }

  authoredOn {
    date = context.source[medication().transcriptionDate()]
  }

  if (context.source[medication().attendingDoctor()]) {
    performer {
      reference = "Practitioner/" + context.source[medication().attendingDoctor().id()]
    }
  }

  dosageInstruction {
    text = context.source[medication().dosisSchema()] as String

    additionalInstruction {
      text = context.source[medication().notes()] as String
    }


    if (context.source[medication().dosis()] != null) {
      doseAndRate {
        doseQuantity {
          value = sanitizeScale(context.source[medication().dosis()] as String)
          unit = context.source[medication().unit().code()]
        }
      }
    }

    if (context.source[medication().trgDate()] != null){
      timing {
        event(context.source[medication().trgDate()])
      }
    }

  }

  if ((context.source[medication().observationBegin()] && context.source[medication().observationBegin().date()]) ||
      (context.source[medication().observationEnd()] && context.source[medication().observationEnd().date()])) {
    extension {
      url = "https://fhir.iqvia.com/patientfinder/extension/period-extension"
      valuePeriod {
        if (context.source[medication().observationBegin()] && context.source[medication().observationBegin().date()]) {
          start = context.source[medication().observationBegin().date()]
        }

        if (context.source[medication().observationEnd()] && context.source[medication().observationEnd().date()]) {
          end = context.source[medication().observationEnd().date()]
        }
      }
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

static boolean isFakeEpisode(final def episode) {
  if (episode == null) {
    return true
  }

  final def fakeId = episode[Episode.ID_CONTAINER]?.find { (it[PSN] as String).toUpperCase().startsWith("FAKE") }
  return fakeId != null
}
