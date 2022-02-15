package projects.uscore

import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractCodeName.NAME_MULTILINGUAL_ENTRIES
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.NUMERIC_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

observation {
  if ("US_CORE_PULSE_OXIMETRY" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-bmi")
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "59408-5"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "2708-6"
    }
  }

  final flowRate = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "O2_FLOW_INHALED" }

  final concentration = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "O2_CONC_INHALED" }

  if (flowRate != null) {
    component {
      code {
        coding {
          system = "http://loinc.org"
          code = "3151-8"
        }
      }
    }


    component {
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
