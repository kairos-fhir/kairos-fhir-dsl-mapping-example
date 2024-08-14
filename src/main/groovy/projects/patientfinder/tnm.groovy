package projects.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.dsl.r4.execution.Fhir4Source
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.tnm

/**
 * Represented by a CXX TNM
 * @author Mike Wähnert
 * @since CXX.v.3.18.1.21, CXX.v.3.18.2, kairos-fhir-dsl-1.13.0
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
    valueCodeableConcept {
      coding {
        system = "https://fhir.centraxx.de/system/tnm/stadium"
        code = (context.source[tnm().stadium()] as String).trim()
        version = context.source[tnm().version()]
      }
    }
  }

  //TNM-T
  if (context.source[tnm().t()]) {
    component {
      if (context.source[tnm().praefixTDict()]) {
        extension {
          url = "https://fhir.centraxx.de/extension/tnm/tPraefix"
          valueCoding {
            system = "https://fhir.centraxx.de/system/tnm/tPraefix"
            code = context.source[tnm().praefixTDict().code()] as String
          }
        }
      }
      code {
        coding {
          system = "http://loinc.org"
          code = isClinical ? "21905-5" : "21899-0"
        }
      }
      valueCodeableConcept {
        coding {
          system = "https://fhir.centraxx.de/system/tnm/t"
          code = (context.source[tnm().t()] as String).trim()
        }
      }
    }
  }

  //TNM-N
  if (context.source[tnm().n()]) {
    component {
      if (context.source[tnm().praefixNDict()]) {
        extension {
          url = "https://fhir.centraxx.de/extension/tnm/nPraefix"
          valueCoding {
            system = "https://fhir.centraxx.de/system/tnm/nPraefix"
            code = context.source[tnm().praefixNDict().code()] as String
          }
        }
      }
      code {
        coding {
          system = "http://loinc.org"
          code = isClinical ? "21906-3" : "21900-6"
        }
      }
      valueCodeableConcept {
        coding {
          system = "https://fhir.centraxx.de/system/tnm/n"
          code = (context.source[tnm().n()] as String).trim()
        }
      }
    }
  }

  //TNM-M
  if (context.source[tnm().m()]) {
    component {
      if (context.source[tnm().praefixMDict()]) {
        extension {
          url = "https://fhir.centraxx.de/extension/tnm/mPraefix"
          valueCoding {
            system = "https://fhir.centraxx.de/system/tnm/mPraefix"
            code = context.source[tnm().praefixMDict().code()] as String
          }
        }
      }
      code {
        coding {
          system = "http://loinc.org"
          code = isClinical ? "21907-1" : "21901-4"
        }
      }
      valueCodeableConcept {
        coding {
          system = "https://fhir.centraxx.de/system/tnm/m"
          code = (context.source[tnm().m()] as String).trim()
        }
      }
    }
  }

  //TNM-y
  if (context.source[tnm().ySymbol()]) {
    component {
      code {
        coding {
          system = "http://loinc.org"
          code = "59479-6"
        }
      }
      valueCodeableConcept {
        coding {
          system = "https://fhir.centraxx.de/system/tnm/ySymbol"
          code = context.source[tnm().ySymbol()] as String
        }
      }
    }
  }

  //TNM-r
  if (context.source[tnm().recidivClassification()]) {
    component {
      code {
        coding {
          system = "http://loinc.org"
          code = "21983-2"
        }
      }
      valueCodeableConcept {
        coding {
          system = "https://fhir.centraxx.de/system/tnm/rSymbol"
          code = context.source[tnm().recidivClassification()] as String
        }
      }
    }
  }

  //TNM-m
  if (context.source[tnm().multiple()]) {
    component {
      code {
        coding {
          system = "http://loinc.org"
          code = "42030-7"
        }
      }
      valueCodeableConcept {
        coding {
          system = "https://fhir.centraxx.de/system/tnm/mSymbol"
          code = context.source[tnm().multiple()] as String
        }
      }
    }
  }

  if (context.source[tnm().tumour()]) {
    focus {
      reference = "Condition/" + context.source[tnm().tumour().centraxxDiagnosis().id()]
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
