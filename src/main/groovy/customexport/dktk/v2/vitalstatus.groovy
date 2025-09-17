package customexport.dktk.v2


import org.hl7.fhir.r4.model.Observation

import java.time.LocalDate
import java.time.Period

import static de.kairos.fhir.centraxx.metamodel.PrecisionDate.DATE
import static de.kairos.fhir.centraxx.metamodel.PrecisionDate.PRECISION
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * Represented by a HDRP PatientMasterDataAnonymous
 * Specified by https://simplifier.net/oncology/vitalstatus
 *
 * Hints:
 * A vitalstatus has no separate encounter, but belongs to all encounter of the patient/subject
 *
 * @author Mike Wähnert
 * @since HDRP.v.3.17.1.6, v.3.17.2
 */
observation {
  id = "Observation/Vitalstatus-" + context.source[patientMasterDataAnonymous().patientContainer().id()]

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
    reference = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]
  }

  effectiveDateTime {
    date = normalizeDate(context.source[patientMasterDataAnonymous().creationDate()] as String)
  }

  valueCodeableConcept {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VitalstatusCS"
      code = mapVitalStatus(context.source[patientMasterDataAnonymous().birthdate()], context.source[patientMasterDataAnonymous().dateOfDeath()])
    }
  }

}

/**
 * removes time zone and time.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
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

  final String dateString = dateOfBirth[DATE]
  final String precisionString = dateOfBirth[PRECISION]
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
