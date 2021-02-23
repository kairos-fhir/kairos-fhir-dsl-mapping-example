package projects.dktk.v1

import org.hl7.fhir.r4.model.Observation

/**
 * Represented by a CXX Progress
 * Specified by https://simplifier.net/oncology/lokalertumorstatus
 *
 * hints:
 * Resource is only exported, if a Progress.assessmentPrimaryDict exists.
 *
 * @author Mike Wähnert
 * @since CXX v.3.17.0.11, v.3.17.1
 */
observation {

  if (context.source["assessmentPrimaryDict"] == null) {
    return
  }

  id = "Observation/TumorstatusLokal-" + context.source["id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-LokalerTumorstatus"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://hl7.org/fhir/observation-category"
      code = "imaging"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "LA4583-6"
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
    date = normalizeDate(context.source["buildingDate"] as String)
  }

  valueCodeableConcept {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VerlaufLokalerTumorstatusCS"
      code = (context.source["assessmentPrimaryDict.code"] as String).toUpperCase()
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
