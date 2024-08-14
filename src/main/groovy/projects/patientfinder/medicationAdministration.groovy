package projects.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.enums.DatePrecision
import de.kairos.fhir.centraxx.metamodel.enums.MedicationServiceType
import org.hl7.fhir.r4.model.MedicationAdministration

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication
/**
 * Represents a CXX Medication
 *
 * @author Mike Wähnert
 * @since v.1.26.0, CXX.v.2023.5
 */
medicationAdministration {

  if (context.source[medication().entitySource()] != "SACT" &&
      context.source[medication().serviceType()] != MedicationServiceType.GAB.name()) {
    return
  }

  id = "MedicationAdministration/" + context.source[medication().id()]

  status = MedicationAdministration.MedicationAdministrationStatus.COMPLETED

  subject {
    reference = "Patient/" + context.source[medication().patientContainer().id()]
  }

  if (!isFakeEpisode(context.source[medication().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[medication().episode().id()]
    }
  }

  medicationCodeableConcept {
    coding {
      system = FhirUrls.System.Medication.BASE_URL
      code = context.source[medication().code()] as String
      display = context.source[medication().name()] as String
    }

    if (context.source[medication().agent()]) {
      coding {
        system = FhirUrls.System.Medication.AGENT
        code = context.source[medication().agent()] as String
      }
    }
    if (context.source[medication().agentGroup()]) {
      coding {
        system = FhirUrls.System.Medication.AGENT_GROUP
        code = context.source[medication().agentGroup()] as String
      }
    }
    if (context.source[medication().methodOfApplication()]) {
      coding {
        system = FhirUrls.System.Medication.APPLICATION_METHOD
        code = context.source[medication().methodOfApplication()] as String
      }
    }
  }

  effectivePeriod {
    start {
      date = normalizeDate(context.source[medication().observationBegin().date()] as String)
      final def beginPrecision = context.source[medication().observationBegin().precision()]
      if (beginPrecision != null && beginPrecision != DatePrecision.UNKNOWN.name()) {
        precision = convertPrecision(beginPrecision as String)
      }
    }
    end {
      date = normalizeDate(context.source[medication().observationEnd().date()] as String)
      final def endPrecision = context.source[medication().observationEnd().precision()]
      if (endPrecision != null && endPrecision != DatePrecision.UNKNOWN.name()) {
        precision = convertPrecision(endPrecision as String)
      }
    }
  }

  if (context.source[medication().attendingDoctor()]) {
    performer {
      actor {
        reference = "Practitioner/" + context.source[medication().attendingDoctor().id()]
      }
    }
  }

  dosage {
    text = context.source[medication().dosisSchema()] as String

    route {
      coding {
        system = FhirUrls.System.Medication.APPLICATION_FORM
        code = context.source[medication().applicationForm()]
      }
    }

    method {
      coding {
        system = FhirUrls.System.Medication.APPLICATION_MEDIUM
        code = context.source[medication().applicationMedium()]
      }
    }

    dose {
      value = sanitizeScale(context.source[medication().dosis()] as String)
      unit = context.source[medication().unit().code()]
    }

    rateQuantity {
      value = sanitizeScale(context.source[medication().quantity()] as String)
    }
  }

  reasonCode {
    text = context.source[medication().notes()] as String
  }

  if (context.source[medication().fillerOrderNumber()]) {
    extension {
      url = FhirUrls.Extension.Medication.FON
      valueString = context.source[medication().fillerOrderNumber()]
    }
  }

  if (context.source[medication().placerOrderNumber()]) {
    extension {
      url = FhirUrls.Extension.Medication.PON
      valueString = context.source[medication().placerOrderNumber()]
    }
  }

  if (context.source[medication().serviceType()]) {
    extension {
      url = FhirUrls.Extension.Medication.TYPE
      valueCoding {
        system = FhirUrls.System.Medication.ServiceType.BASE_URL
        code = context.source[medication().serviceType()]
      }
    }
  }

  if (context.source[medication().ordinanceReleaseMethod()]) {
    extension {
      url = FhirUrls.Extension.Medication.ORDINANCE_RELEASE_METHOD
      valueString = context.source[medication().ordinanceReleaseMethod()]
    }
  }

  if (context.source[medication().transcriptionist()]) {
    extension {
      url = FhirUrls.Extension.Medication.TRANSCRIPTIONIST
      valueString = context.source[medication().transcriptionist()]
    }
  }

  if (context.source[medication().prescribedBy()]) {
    extension {
      url = FhirUrls.Extension.Medication.PRESCRIBER
      valueString = context.source[medication().prescribedBy()]
    }
  }

  if (context.source[medication().prescription()]) {
    extension {
      url = FhirUrls.Extension.Medication.IS_PRESCRIPTION
      valueBoolean = context.source[medication().prescription()]
    }
  }

  if (context.source[medication().resultDate()]) {
    extension {
      url = FhirUrls.Extension.Medication.RESULTDATE
      valueBoolean = context.source[medication().resultDate()]
    }
  }
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String convertPrecision(final String cxxPrecision) {
  if (DatePrecision.EXACT.name() == cxxPrecision) {
    return TemporalPrecisionEnum.MILLI.name()
  } else if (DatePrecision.DAY.name() == cxxPrecision) {
    return TemporalPrecisionEnum.DAY.name()
  } else if (DatePrecision.MONTH.name() == cxxPrecision) {
    return TemporalPrecisionEnum.MONTH.name()
  } else if (DatePrecision.YEAR.name() == cxxPrecision) {
    return TemporalPrecisionEnum.YEAR.name()
  } else {
    return DatePrecision.DAY
  }
}

static BigDecimal sanitizeScale(final String numeric) {
  return numeric == null ? null : new BigDecimal(numeric).stripTrailingZeros()
}

static boolean isFakeEpisode(final def episode) {
  if (episode == null) {
    return true
  }

  if (["SACT", "COSD"].contains(episode[Episode.ENTITY_SOURCE])) {
    return true
  }

  final def fakeId = episode[Episode.ID_CONTAINER]?.find { (it[PSN] as String).toUpperCase().startsWith("FAKE") }
  return fakeId != null
}