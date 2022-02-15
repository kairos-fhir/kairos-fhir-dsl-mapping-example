package projects.uscore

import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import de.kairos.fhir.centraxx.metamodel.Unity

import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.*
import static de.kairos.fhir.centraxx.metamodel.LaborValue.*
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.*
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFindingLaborValue
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

observation {
  if ("US_CORE_RESPIRATORY_RATE" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-bmi")
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "9279-1"
    }
  }

  final laborFindingLaborValue = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "RESPIRATORY_RATE" }

  valueQuantity {
    value = laborFindingLaborValue[NUMERIC_VALUE]
    unit = laborFindingLaborValue[LABOR_VALUE][LaborValueNumeric.UNIT][NAME_MULTILINGUAL_ENTRIES]
        .find { final ml -> ml[LANG] == "de" }?.getAt(VALUE)
    system = "http://unitsofmeasure.org"
    code = "/min"
  }
}


