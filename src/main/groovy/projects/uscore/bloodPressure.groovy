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
 * Represents a CXX LaborMapping for the US Core Vital Sign Observation Blood Pressure.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-blood-pressure.html
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
  if ("US_CORE_BLOOD_PRESSURE" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-blood-pressure")
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
      code = "85354-9"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  final def systolic = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "BLOOD_PRESSURE_SYS" }

  final def diastolic = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "BLOOD_PRESSURE_DIA" }

  component {
    code {
      coding {
        system = "http://loinc.org"
        code = "8480-6"
      }
    }

    valueQuantity {
      value = systolic[NUMERIC_VALUE]
      unit = systolic[LABOR_VALUE][LaborValueNumeric.UNIT][NAME_MULTILINGUAL_ENTRIES]
          .find { final ml -> ml[LANG] == "de" }?.getAt(VALUE)
      system = "http://unitsofmeasure.org"
      code = "mm[Hg]"
    }
  }

  component {
    code {
      coding {
        system = "http://loinc.org"
        code = "8462-4"
      }
    }

    valueQuantity {
      value = diastolic[NUMERIC_VALUE]
      unit = diastolic[LABOR_VALUE][LaborValueNumeric.UNIT][NAME_MULTILINGUAL_ENTRIES]
          .find { final ml -> ml[LANG] == "de" }?.getAt(VALUE)
      system = "http://unitsofmeasure.org"
      code = "mm[Hg]"
    }
  }

}