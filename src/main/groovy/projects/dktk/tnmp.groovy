package projects.dktk

import org.hl7.fhir.r4.model.Observation

/**
 * Represented by a CXX TNM
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.6
 */
observation {

  id = "Observation/Tnm-" + context.source["id"]

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

  if (context.source["stadium"]) {
    valueCodeableConcept {
      coding {
        version = context.source["version"]
        code = (context.source["stadium"] as String).trim()
      }
    }
  }

  //TNM-T
  if (context.source["t"]) {
    component {
      if (context.source["praefixTDict"]) {
        extension {
          url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix"
          valueCoding {
            system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMcpuPraefixTCS"
            code = context.source["praefixTDict.code"] as String
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
          code = (context.source["t"] as String).trim()
        }
      }
    }
  }

  //TNM-N
  if (context.source["n"]) {
    component {
      if (context.source["praefixNDict"]) {
        extension {
          url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix"
          valueCoding {
            system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMcpuPraefixTCS"
            code = context.source["praefixNDict.code"] as String
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
          code = (context.source["n"] as String).trim()
        }
      }
    }
  }

  //TNM-M
  if (context.source["m"]) {
    component {
      if (context.source["praefixMDict"]) {
        extension {
          url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-TNMcpuPraefix"
          valueCoding {
            system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/TNMcpuPraefixTCS"
            code = context.source["praefixMDict.code"] as String
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
          code = (context.source["m"] as String).trim()
        }
      }
    }
  }

  //TNM-y
  if (context.source["ySymbol"]) {
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
          code = context.source["ySymbol"] as String
        }
      }
    }
  }

  //TNM-r
  if (context.source["recidivClassification"]) {
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
          code = context.source["recidivClassification"] as String
        }
      }
    }
  }

  //TNM-m
  if (context.source["multiple"]) {
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
          code = context.source["multiple"] as String
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
