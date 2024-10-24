package projects.cxx.v2

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.calendarEvent

/**
 * test script to export a data from a LaborMapping that is linked to a medication
 * @since KAIROS-FHIR-DSL.v.1.37.0, CXX 2024.4.0, CXX 2024.3.8
 */
appointment {
  println(context.source)

  final def mapping = context.source[calendarEvent().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "APPOINTMENT_TEST_MAPPING"
  }


  if (mapping) {
    final def lflv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == "APPOINTMENT_TEST_LV"
    }

    if (lflv) {
      id = context.source[calendarEvent().id()]

      description {
        value = lflv[LaborFindingLaborValue.STRING_VALUE] as String
      }
    }
  }
}