package projects.dktk

import org.hl7.fhir.r4.model.Observation

/**
 * Represented by a CXX Histology, because it is only a detailed information of the histology entity.
 * Specified by https://simplifier.net/oncology/grading
 *
 * hints:
 * Reference to a single specimen is not clearly determinable, because in CXX the reference might be histology 1->n diagnosis/tumor 1->n sample.
 * Gradings can be displayed, but not entered by CXX-UI. The grading import is only possible by interfaces.
 * Even when the Observation.value hast cardinality 1.., Histology.gradingDict is not mandatory.
 * Resource is only exported, if a Progress.gradingDict exists.
 *
 * @author Mike Wähnert
 * @since CXX v.3.17.0.11, v.3.17.1
 */
observation {

  if (context.source["gradingDict"] == null) {
    return
  }

  id = "Observation/Grading-" + context.source["id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-Grading"
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
      code = "59542-1"
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
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GradingCS"
      version = "32"
      code = (context.source["gradingDict.code"] as String).toUpperCase()
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
