package projects.dktk_with_jpa_navigation


import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.tnm

/**
 * Represented by a CXX TNM
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.6
 */
observation {

  id = "Observation/Tnm-" + context.source[tnm().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-TNMp"
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-TNMc"
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
      code = "21902-2"
    }
  }

  subject {
    reference = "Patient/" + context.source[tnm().patientContainer().id()]
  }

  if (context.source[tnm().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[tnm().episode().id()]
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source[tnm().date()] as String)
  }

  if (context.source[tnm().stadium()]) {
    valueCodeableConcept {
      coding {
        version = context.source[tnm().version()]
        code = (context.source[tnm().stadium()] as String).trim()
      }
    }
  }

  //TNM-T
  if (context.source[tnm().t()]) {
    component {
      if (context.source[tnm().praefixTDict()]) {
        extension {
          url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix"
          valueCoding {
            system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMcpuPraefixTCS"
            code = context.source[tnm().praefixTDict().code()] as String
          }
        }
      }
      code {
        coding {
          system = "http://loinc.org"
          code = "21899-0"
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
          valueCoding {
            system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMcpuPraefixTCS"
            code = context.source[tnm().praefixNDict().code()] as String
          }
        }
      }
      code {
        coding {
          system = "http://loinc.org"
          code = "21900-6"
        }
      }
      valueCodeableConcept {
        coding {
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMTCS"
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
          valueCoding {
            system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMcpuPraefixTCS"
            code = context.source[tnm().praefixMDict().code()] as String
          }
        }
      }
      code {
        coding {
          system = "http://loinc.org"
          code = "21901-4"
        }
      }
      valueCodeableConcept {
        coding {
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMTCS"
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

}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}
