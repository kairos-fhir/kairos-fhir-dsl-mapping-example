package projects.uscore

import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractCodeName.NAME_MULTILINGUAL_ENTRIES
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.NUMERIC_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
/**
 * Represents a CXX LaborMapping for the US Core Vital Sign Observation Pulse Oximetry.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-pulse-oximetry.html
 *
 * hints:
 * - Observation are specified by LOINC codes.
 * - Units are specified by UCUM codes.
 *
 * Note: The mapping requires labor methods, labor values and units defined in CXX that correspond to the specification of the
 * profile! For more information, see project README.md
 *
 * @author Jonas Küttner
 * @since v.1.13.0, CXX.v.2022.1.0
 */
observation {
  if ("US_CORE_PULSE_OXIMETRY" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-pulse-oximetry")
  }

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "vital-signs"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "59408-5"
    }
    coding {
      system = "http://loinc.org"
      code = "2708-6"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  final def flowRate = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "O2_FLOW_INHALED" }

  final def concentration = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "O2_CONC_INHALED" }

  if (flowRate != null) {
    component {
      code {
        coding {
          system = "http://loinc.org"
          code = "3151-8"
        }
      }

      valueQuantity {
        value = flowRate[NUMERIC_VALUE]
        unit = flowRate[LABOR_VALUE][LaborValueNumeric.UNIT][NAME_MULTILINGUAL_ENTRIES]
            .find { final ml -> ml[LANG] == "de" }?.getAt(VALUE)
        system = "http://unitsofmeasure.org"
        code = "L/min"
      }
    }
  }


  if (concentration != null) {
    component {
      code {
        coding {
          system = "http://loinc.org"
          code = "3150-0"
        }
      }

      valueQuantity {
        value = concentration[NUMERIC_VALUE]
        unit = concentration[LABOR_VALUE][LaborValueNumeric.UNIT][NAME_MULTILINGUAL_ENTRIES]
            .find { final ml -> ml[LANG] == "de" }?.getAt(VALUE)
        system = "http://unitsofmeasure.org"
        code = "%"
      }
    }
  }
}
