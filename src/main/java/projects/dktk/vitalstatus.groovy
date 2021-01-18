package projects.dktk

import org.hl7.fhir.r4.model.Observation

import java.time.LocalDate
import java.time.Period

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * Specified by https://simplifier.net/oncology/vitalstatus
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.11
 */
observation {
  id = "Observation/Vitalstatus-" + context.source["patientcontainer.id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-Vitalstatus"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://hl7.org/fhir/observation-category"
      code = "activity"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "75186-7"
    }
  }

  subject {
    reference = "Patient/" + context.source["patientcontainer.id"]
  }

  if (context.source["episode"]) {
    encounter {
      reference = "Encounter/" + context.source["episode.id"]
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source["date"] as String)
  }


  valueCodeableConcept {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS"
      code = mapVitalStatus(context.source["birthdate"], context.source["dateOfDeath"])
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

/**
 * A date of death with UNKNOWN precision is interpreted as, we are sure the person has died, but we dont know when more exactly.
 * An age, which is older than the oldest known person is interpreted as, the person has been died, but the date of death has not been documented.
 * return lebend, verstorben or unbekannt
 */
static String mapVitalStatus(final Object dateOfBirth, final Object dateOfDeath) {
  if (dateOfDeath != null) {
    return "verstorben"
  }

  if (dateOfBirth == null) {
    return "unbekannt"
  }

  final String dateString = dateOfBirth["date"]
  final String precisionString = dateOfBirth["precision"]
  if (dateString == null || precisionString == "UNKNOWN") {
    return "unbekannt"
  }

  final LocalDate date = LocalDate.parse(dateString.substring(0, 10))
  return isOlderThanTheOldestVerifiedPerson(date) ? "verstorben" : "lebend"
}

/**
 * source: https://en.wikipedia.org/wiki/List_of_the_verified_oldest_people
 */
static boolean isOlderThanTheOldestVerifiedPerson(final LocalDate dateOfBirth) {
  final Period age = Period.between(dateOfBirth, LocalDate.now())
  return age.getYears() > 123
}
