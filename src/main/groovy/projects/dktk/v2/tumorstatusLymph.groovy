package projects.dktk.v2


import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.progress

/**
 * Represented by a CXX Progress
 * Specified by https://simplifier.net/oncology/tumorstatuslymphknoten
 *
 * hints:
 * Resource is only exported, if a Progress.assessmentLymphDict exists.
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 */
observation {

  if (context.source[progress().assessmentLymphDict()] == null) {
    return
  }

  id = "Observation/TumorstatusLymph-" + context.source[progress().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-TumorstatusLymphknoten"
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
      code = "LA4370-8"
    }
  }

  subject {
    reference = "Patient/" + context.source[progress().patientContainer().id()]
  }

  effectiveDateTime {
    date = normalizeDate(context.source[progress().buildingDate()] as String)
  }

  valueCodeableConcept {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VerlaufTumorstatusLymphknotenCS"
      code = (context.source[progress().assessmentLymphDict().code()] as String).toUpperCase()
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
