package projects.mii.modul.person

import org.hl7.fhir.r4.model.Observation

import java.time.LocalDate
import java.time.Period

import static de.kairos.fhir.centraxx.metamodel.PrecisionDate.DATE
import static de.kairos.fhir.centraxx.metamodel.PrecisionDate.PRECISION
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * represented by a CXX patient
 * @author Jonas Küttner
 * @since v.1.8.0, CXX.v.3.18.1
 */

observation {
  id = "Observation/" + context.source[patientMasterDataAnonymous().patientContainer().id()]
  meta {
    source = "urn:centraxx"
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Vitalstatus"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "survey"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "75186-7"
    }
  }

  subject {
    reference = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]
  }

  effectiveDateTime {
    date = normalizeDate(context.source[patientMasterDataAnonymous().creationDate()] as String)
  }

  valueCodeableConcept {
    final String[] codeDisplay = mapVitalStatus(context.source[patientMasterDataAnonymous().birthdate()], context.source[patientMasterDataAnonymous().dateOfDeath()])
    coding {
      system = "https://www.medizininformatik-initiative.de/fhir/core/modul-person/CodeSystem/Vitalstatus"
      code = codeDisplay[0]
      display = codeDisplay[1]
    }
  }


}

/**
 * A date of death with UNKNOWN precision is interpreted as the person has died but the exact date is unknown.
 * An age, which is older than the oldest known person is interpreted as the person has died, but the date of death has not been documented.
 */

static String[] mapVitalStatus(final Object dateOfBirth, final Object dateOfDeath) {
  if (dateOfDeath != null) {
    return ["T", "Patient verstorben"]
  }

  if (dateOfBirth == null) {
    return ["X", "unbekannt"]
  }

  final String dateString = dateOfBirth[DATE]
  final String precisionString = dateOfBirth[PRECISION]
  if (dateString == null || precisionString == "UNKNOWN") {
    return ["X", "unbekannt"]
  }

  final LocalDate date = LocalDate.parse(dateString.substring(0, 10))
  return isOlderThanTheOldestVerifiedPerson(date) ? ["T", "Patient verstorben"] : ["L", "Patient lebt"]
}

/**
 * source: https://en.wikipedia.org/wiki/List_of_the_verified_oldest_people
 */
static boolean isOlderThanTheOldestVerifiedPerson(final LocalDate dateOfBirth) {
  final Period age = Period.between(dateOfBirth, LocalDate.now())
  return age.getYears() > 123
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}
