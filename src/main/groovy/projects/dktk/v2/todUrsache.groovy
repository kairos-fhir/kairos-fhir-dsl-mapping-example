package projects.dktk.v2

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Diagnosis
 * Specified by https://simplifier.net/oncology/todursache
 * @author Mike Wähnert
 * @since kairos-fhir-dsl.v.1.20.0, CXX.v.2023.2.0, 2023.1.1, 2022.4.7, 3.18.4, 3.18.3.14
 */
observation {

  final boolean isCauseOfDeath = context.source[diagnosis().causeOfDeath()]
  if (!isCauseOfDeath) {
    return // If diagnosis is not cause of death, it is exported as a diagnosis only
  }

  id = "Observation/TodUrsache-" + context.source[diagnosis().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-TodUrsache"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://hl7.org/fhir/observation-category"
      code = "laboratory"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "68343-3"
    }
  }

  subject {
    reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
  }

  effectiveDateTime {
    date = normalizeDate(context.source[diagnosis().diagnosisDate().date()] as String)
    precision = TemporalPrecisionEnum.DAY.name()
  }

  valueCodeableConcept {
    final String icd10Code = context.source[diagnosis().icdEntry().code()] as String
    if (icd10Code) {
      coding {
        system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
        code = icd10Code
        version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()]
      }
      coding { //todTumorbedingt
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/JNUCS"
        code = isTumorDiagnosis(icd10Code) ? "J" : "N" // only causeOfDeath=true is exported
      }
    } else {
      coding { //todTumorbedingt
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/JNUCS"
        code = "U" // unbekannt
      }
    }
  }
}

static void isTumorDiagnosis(final String icd10Code) {
  ["C", "D0", "D32", "D33", "D35.2", "D35.3", "D35.4", "D37", "D38", "D39",
   "D40", "D41", "D42", "D43", "D44", "D45", "D46", "D47", "D48"].stream().anyMatch { final codePrefix -> icd10Code.startsWith(codePrefix) }
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}