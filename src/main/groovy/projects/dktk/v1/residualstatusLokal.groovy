package projects.dktk.v1

import org.hl7.fhir.r4.model.Observation

/**
 * Represented by a CXX Surgery
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.7
 */
observation {
  id = "Observation/LokaleBeurteilungResidualstatus-" + context.source["id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-LokaleBeurteilungResidualstatus"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://hl7.org/fhir/observation-category"
      code = "procedure"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "84892-9"
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
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS"
      code = context.source["rClassificationLocalDict"]?.getAt("code")?.toString()?.toUpperCase()
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
