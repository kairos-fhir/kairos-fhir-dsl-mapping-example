package projects.patientfinder

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Transforms CXX LaborFinding of the LaborMethod "SACT_Profil" to a FHIR CarePlan
 *
 * Hints:
 *  This is a special mapping for Hull
 */
carePlan {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "SACT_Profile") {
    return
  }

  id = "CarePlan/" + context.source[laborMapping().laborFinding().id()]

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->
    final def laborValue = lflv[LaborFindingLaborValue.LABOR_VALUE] != null
        ? lflv[LaborFindingLaborValue.LABOR_VALUE] // before CXX.v.2022.3.0
        : lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE] // from CXX.v.2022.3.0
    final String laborValueCode = laborValue?.getAt(CODE) as String

    if (laborValueCode.equals("Regimen")) {
      identifier { value = lflv[LaborFindingLaborValue.STRING_VALUE] }
    } else if (laborValueCode.equals("Date_Decision_To_Treat")) {
      created {
        date = lflv[LaborFindingLaborValue.DATE_VALUE][PrecisionDate.DATE]
      }
    } else if (laborValueCode.equals("Start_Date_Of_Regimen")) {
      period {
        start {
          date = lflv[LaborFindingLaborValue.DATE_VALUE][PrecisionDate.DATE]
        }
      }
    }
  }

}

