package customexport.patientfinder.digione.mmci

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.dsl.r4.execution.Fhir4Source
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.tnm

/**
 * Represented by a HDRP TNM
 * @author Mike Wähnert
 * @since HDRP.v.2025.3.2, kairos-fhir-dsl-1.54.0
 */
observation {

  id = "Observation/Tnm-" + context.source[tnm().id()]

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://hl7.org/fhir/observation-category"
      code = "laboratory"
    }
  }

  final boolean isClinical = isClinical(context.source)
  code {
    coding {
      system = "http://loinc.org"
      code = isClinical ? "21908-9" : "21902-2"
    }
  }

  subject {
    reference = "Patient/" + context.source[tnm().patientContainer().id()]
  }

  if (!isFakeEpisode(context.source[tnm().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[tnm().episode().id()]
    }
  }

  if (context.source[tnm().date()]) {
    effectiveDateTime {
      date = context.source[tnm().date()]
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }

  if (context.source[tnm().stadium()]) {
    component {
      code {
        coding {
          code = "stadium"
        }
      }
      valueString = (context.source[tnm().stadium()] as String).trim()
    }
  }

  if (context.source[tnm().t()]) {
    component {
      code {
        coding {
          code = "t"
        }
      }
      valueString = (context.source[tnm().t()] as String).trim()
    }
  }

  if (context.source[tnm().praefixTDict()]) {
    component {
      code {
        coding {
          code = "t-prefix"
        }
      }
      valueCodeableConcept {
        coding {
          code = (context.source[tnm().praefixTDict().code()] as String).trim()
          display = context.source[tnm().praefixTDict().multilinguals()].find { final def ml ->
            ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
          }?.getAt(Multilingual.SHORT_NAME)
        }
      }
    }
  }

  if (context.source[tnm().n()]) {
    component {
      code {
        coding {
          code = "n"
        }
      }
      valueString = (context.source[tnm().n()] as String).trim()
    }
  }

  if (context.source[tnm().praefixNDict()]) {
    component {
      code {
        coding {
          code = "n-prefix"
        }
      }
      valueCodeableConcept {
        coding {
          code = (context.source[tnm().praefixNDict().code()] as String).trim()
          display = context.source[tnm().praefixNDict().multilinguals()].find { final def ml ->
            ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
          }?.getAt(Multilingual.SHORT_NAME)
        }
      }
    }
  }

  if (context.source[tnm().m()]) {
    component {
      code {
        coding {
          code = "m"
        }
      }
      valueString = (context.source[tnm().m()] as String).trim()
    }
  }

  if (context.source[tnm().praefixMDict()]) {
    component {
      code {
        coding {
          code = "m-prefix"
        }
      }
      valueCodeableConcept {
        coding {
          code = (context.source[tnm().praefixMDict().code()] as String).trim()
          display = context.source[tnm().praefixMDict().multilinguals()].find { final def ml ->
            ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
          }?.getAt(Multilingual.SHORT_NAME)
        }
      }
    }
  }

  if (context.source[tnm().ySymbol()]) {
    component {
      code {
        coding {
          code = "y-symbol"
        }
      }
      valueString = (context.source[tnm().ySymbol()] as String).trim()
    }
  }

  if (context.source[tnm().sourceDict()]) {
    component {
      code {
        coding {
          code = "source"
        }
      }
      valueCodeableConcept {
        coding {
          code = (context.source[tnm().sourceDict().code()] as String).trim()
          display = context.source[tnm().sourceDict().multilinguals()].find { final def ml ->
            ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
          }?.getAt(Multilingual.SHORT_NAME)
        }
      }
    }
  }

  if (context.source[tnm().recidivClassification()]) {
    component {
      code {
        coding {
          code = "recidiv classification"
        }
      }
      valueString = (context.source[tnm().recidivClassification()] as String).trim()
    }
  }

  if (context.source[tnm().multiple()]) {
    component {
      code {
        coding {
          code = "multiple"
        }
      }
      valueString = (context.source[tnm().multiple()] as String).trim()
    }
  }
}

static boolean isClinical(final Fhir4Source source) {
  final String clinicalPrefix = "c"
  final String prefixT = source[tnm().praefixTDict().code()]
  final String prefixN = source[tnm().praefixNDict().code()]
  final String prefixM = source[tnm().praefixMDict().code()]
  return clinicalPrefix.equalsIgnoreCase(prefixT) && clinicalPrefix.equalsIgnoreCase(prefixN) && clinicalPrefix.equalsIgnoreCase(prefixM)
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
