package projects.cxx.v2

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.calendarEvent
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

/**
 * test script to export a data from a LaborMapping that is linked to a medication
 * @since KAIROS-FHIR-DSL.v.1.37.0, CXX 2024.4.0, CXX 2024.3.8
 */
medication {
  id = context.source[medication().id()]

  println(context.source)

  final def mapping = context.source[calendarEvent().laborMappings()].find {
    it[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "MEDICATION_TEST_MAPPING"
  }

  if (mapping) {
    final def lflv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find {
      it[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == "MEDICATION_TEST_LV"
    }

    if (lflv) {
      id = context.source[medication().id()]

      code {
        text = lflv[LaborFindingLaborValue.STRING_VALUE] as String
      }
    }
  }
}