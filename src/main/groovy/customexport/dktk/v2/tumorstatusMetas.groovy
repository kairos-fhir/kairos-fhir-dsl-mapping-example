package customexport.dktk.v2


import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.progress

/**
 * Represented by a HDRP Progress
 * Specified by https://simplifier.net/oncology/tumorstatusfernmetastasen
 *
 * hints:
 * Resource is only exported, if a Progress.assessmentMetaDict exists.
 *
 * @author Mike Wähnert
 * @since HDRP.v.3.17.1.6, v.3.17.2
 */
observation {

  if (context.source[progress().assessmentMetaDict()] == null) {
    return
  }

  id = "Observation/TumorstatusMetas-" + context.source[progress().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-TumorstatusFernmetastasen"
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
      code = "LA4226-2"
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
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/VerlaufTumorstatusFernmetastasenCS"
      code = (context.source[progress().assessmentMetaDict().code()] as String).toUpperCase()
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
