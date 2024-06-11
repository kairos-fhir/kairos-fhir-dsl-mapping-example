package projects.dktk.v2

import de.kairos.fhir.dsl.r4.execution.Fhir4Source
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.tnm

/**
 * Represented by a CXX TNM
 * Hints:
 *  CCP-IT has decided on 2020-11-17 to use the TNMc profile only if all TNM prefixes are clinical.
 *  If only one prefix is not clinical (c) the profile TNMp is used, even if it is no prefix p (pathology), but e.g a (autopsy) or u (ultrasonic).
 *  Both profiles differ only in the loinc codes for Observation.code.coding.code and Observation.component:TNM-T/N/M.code.coding.code
 *  Reference to focus condition has been added additionally, because a reverse reference is not possible yet.
 * @author Mike Wähnert
 * @since CXX.v.3.18.1.21, CXX.v.3.18.2, kairos-fhir-dsl-1.13.0
 */
observation {

  id = "Observation/Tnm-" + context.source[tnm().id()]

  final boolean isClinical = isClinical(context.source)
  meta {
    profile isClinical ? "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-TNMc"
        : "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-TNMp"
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
      code = isClinical ? "21908-9" : "21902-2"
    }
  }

  subject {
    reference = "Patient/" + context.source[tnm().patientContainer().id()]
  }

  effectiveDateTime {
    date = normalizeDate(context.source[tnm().date()] as String)
  }

  if (context.source[tnm().stadium()]) {
    valueCodeableConcept {
      coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/UiccstadiumCS"
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
          url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix"
          valueCodeableConcept {
            coding {
              system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMcpuPraefixTCS"
              code = context.source[tnm().praefixTDict().code()] as String
            }
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
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMTCS"
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
          url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix"
          valueCodeableConcept {
            coding {
              system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMcpuPraefixTCS"
              code = context.source[tnm().praefixNDict().code()] as String
            }
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
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMNCS"
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
          url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix"
          valueCodeableConcept {
            coding {
              system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMcpuPraefixTCS"
              code = context.source[tnm().praefixMDict().code()] as String
            }
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
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMMCS"
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
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMySymbolCS"
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
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMrSymbolCS"
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
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMmSymbolCS"
          code = context.source[tnm().multiple()] as String
        }
      }
    }
  }

  if (context.source[tnm().tumour()] && hasRelevantCode(context.source[tnm().tumour().centraxxDiagnosis().diagnosisCode()] as String)) {
    focus {
      reference = "Condition/" + context.source[tnm().tumour().centraxxDiagnosis().id()]
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

static boolean isClinical(final Fhir4Source source) {
  final String clinicalPrefix = "c"
  final String prefixT = source[tnm().praefixTDict().code()]
  final String prefixN = source[tnm().praefixNDict().code()]
  final String prefixM = source[tnm().praefixMDict().code()]
  return clinicalPrefix.equalsIgnoreCase(prefixT) && clinicalPrefix.equalsIgnoreCase(prefixN) && clinicalPrefix.equalsIgnoreCase(prefixM)
}

static boolean hasRelevantCode(final String icdCode) {
  return icdCode != null && (icdCode.toUpperCase().startsWith('C') || icdCode.toUpperCase().startsWith('D'))
}
