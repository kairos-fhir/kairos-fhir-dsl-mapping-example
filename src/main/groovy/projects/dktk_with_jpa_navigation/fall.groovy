package projects.dktk_with_jpa_navigation


import org.hl7.fhir.r4.model.Encounter

import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode

/**
 * Represents a CXX Episode.
 * Specified by https://simplifier.net/oncology/fall
 *
 * hints:
 * The DKTK-Encounter has been removed by CCP-IT JF on 2020-12-04 and must not longer be exported for the DKTK.
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 */
encounter {
  id = "Encounter/" + context.source[episode().id()]

  meta {
    profile("http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Encounter-Fall")
  }

  status = Encounter.EncounterStatus.UNKNOWN
  class_ {
    system = "http://terminology.hl7.org/CodeSystem/v3-ActCode"
    code = "unknown"
  }

  subject {
    reference = "Patient/" + context.source[episode().patientContainer().id()]
  }

  period {

    start {
      date = normalizeDate(context.source[episode().validFrom()] as String)
    }

    end {
      date = normalizeDate(context.source[episode().validUntil()] as String)
    }

  }

  if (context.source[episode().habitation()]) {
    serviceProvider {
      reference = "Organization/" + context.source[episode().habitation().id()]
    }
  }

  // Because of a bidirectional reference between Encounter/Condition and the referential integrity of the blaze store, this reference is disabled.
//  context.source["diagnoses"]?.each { final def d ->
//    diagnosis {
//      condition {
//        reference = "Condition/" + d["id"]
//      }
//    }
//  }
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}
