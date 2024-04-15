package projects.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Episode
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.annArbor

/**
 * Represented by a CXX AnnArbor classification
 * @author Mike Wähnert
 * @since CXX.v.2023.2.0, kairos-fhir-dsl-1.21.0
 */
observation {

  id = "Observation/AnnArbor-" + context.source[annArbor().id()]

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://hl7.org/fhir/observation-category"
      code = "laboratory"
    }
  }

  subject {
    reference = "Patient/" + context.source[annArbor().patientContainer().id()]
  }

  if (!isFakeEpisode(context.source[annArbor().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[annArbor().episode().id()]
    }
  }

  if (context.source[annArbor().date()]) {
    effectiveDateTime {
      date = context.source[annArbor().date()]
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }

  if (context.source[annArbor().tumour()]) {
    focus {
      reference = "Condition/" + context.source[annArbor().tumour().centraxxDiagnosis().id()]
    }
  }

  if (context.source[annArbor().stadium()]) {
    valueCodeableConcept {
      coding {
        system = "https://fhir.centraxx.de/system/annArbor/stadium"
        code = (context.source[annArbor().stadium()] as String).trim()
      }
    }
  }

  //Extralymphatic infestation
  if (context.source[annArbor().extraDict()]) {
    component {
      code {
        coding {
          system = "https://fhir.centraxx.de/system/annArbor"
          code = "extra"
          display = "Extralymphatic infestation"
        }
      }
      valueCodeableConcept {
        coding {
          system = "https://fhir.centraxx.de/system/annArbor/extraDictionary"
          code = context.source[annArbor().extraDict().code()] as String
          display = context.source[annArbor().extraDict().nameMultilingualEntries()].find { it[LANG] == "en" }?.getAt(VALUE) as String
        }
      }
    }
  }

  //General symptoms
  if (context.source[annArbor().generalDict()]) {
    component {
      code {
        coding {
          system = "https://fhir.centraxx.de/system/annArbor"
          code = "general"
          display = "General symptoms"
        }
      }
      valueCodeableConcept {
        coding {
          system = "https://fhir.centraxx.de/system/annArbor/generalDictionary"
          code = context.source[annArbor().generalDict().code()] as String
          display = context.source[annArbor().generalDict().nameMultilingualEntries()].find { final def me ->
            me[LANG] == "en"
          }?.getAt(VALUE) as String
        }
      }
    }
  }

  //Spleen
  if (context.source[annArbor().spleenDict()]) {
    component {
      code {
        coding {
          system = "https://fhir.centraxx.de/system/annArbor"
          code = "spleen"
          display = "Spleen"
        }
      }
      valueCodeableConcept {
        coding {
          system = "https://fhir.centraxx.de/system/annArbor/infestationDictionary"
          code = context.source[annArbor().spleenDict().code()] as String
          display = context.source[annArbor().spleenDict().nameMultilingualEntries()].find { final def me ->
            me[LANG] == "en"
          }?.getAt(VALUE) as String
        }
      }
    }
  }
}

static boolean isFakeEpisode(final def episode) {
  if (episode == null) {
    return true
  }

  if (["SACT", "COSD"].contains(episode[Episode.ENTITY_SOURCE])) {
    return true
  }

  final def fakeId = episode[Episode.ID_CONTAINER]?.find { (it[PSN] as String).toUpperCase().startsWith("FAKE") }
  return fakeId != null
}
