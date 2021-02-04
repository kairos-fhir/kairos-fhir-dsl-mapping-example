package projects.dktk_with_jpa_navigation


import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.metastasis

/**
 * Represented by a CXX Metastasis
 * Specified by https://simplifier.net/oncology/fernmetastasen-duplicate-2
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 */
observation {

  id = "Observation/Metastasis-" + context.source[metastasis().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-Fernmetastasen"
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
      code = "21907-1"
    }
  }

  subject {
    reference = "Patient/" + context.source[metastasis().patientContainer().id()]
  }

  if (context.source[metastasis().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[metastasis().episode().id()]
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source[metastasis().date()] as String)
  }

  valueCodeableConcept {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/JNUCS"
      code = "J" // if a metastasis exists, this code is always J
    }
  }

  if (context.source[metastasis().localisationCodeDict()]) {
    bodySite {
      coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/FMLokalisationCS"
        code = (context.source[metastasis().localisationCodeDict().code()] as String).toUpperCase()
      }
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
