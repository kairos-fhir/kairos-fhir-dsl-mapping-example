package projects.patientfinder


import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Transforms CXX LaborFinding of the LaborMethod "SACT_Profil" to a FHIR MedicationAdministration
 * @since CXX.v.2023.4.1, CXX.v.2023.5.0
 *
 * Hints:
 *  This is a special mapping for Hull
 *
 */
medicationAdministration {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "SACT_Profile") {
    return
  }

  final String drugName = null
  final def dmpd = null
  final def actualDosePerAdministration = null
  final def unitOfMeasurementSnomed = null
  final def sactAdminRoute = null
  final def routeOfAdministrationSnomed = null
  final def adminDate = null

  id = "MedicationAdministration/SACT-" + context.source[laborMapping().laborFinding().id()]

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->
    final def laborValue = lflv[LaborFindingLaborValue.LABOR_VALUE] != null
        ? lflv[LaborFindingLaborValue.LABOR_VALUE] // before CXX.v.2022.3.0
        : lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE] // from CXX.v.2022.3.0
    final String laborValueCode = laborValue?.getAt(CODE) as String


    if (laborValueCode.equals("Drug_Name")) {
      drugName = lflv[LaborFindingLaborValue.STRING_VALUE]
    } else if (laborValueCode.equals("DM+D")) {
      dmpd = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
    } else if (laborValueCode.equals("Actual_Dose_Per_Administration")) {
      actualDosePerAdministration = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
    } else if (laborValueCode.equals("Unit_Of_Measurement_(SNOMED_CT_DM+D)")) {
      unitOfMeasurementSnomed = lflv[LaborFindingLaborValue.STRING_VALUE]
    } else if (laborValueCode.equals("SACT_Administration_Route")) {
      lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final ce ->
        sactAdminRoute = ce[CODE]
      }
    } else if (laborValueCode.equals("Route_Of_Administration_(SNOMED_CT_DM+D)")) {
      routeOfAdministrationSnomed = lflv[LaborFindingLaborValue.STRING_VALUE]
    } else if (laborValueCode.equals("Administration_Date")) {
      adminDate = lflv[LaborFindingLaborValue.DATE_VALUE][PrecisionDate.DATE]
    }
  }

  medicationCodeableConcept {
    coding {
      display = drugName
      code = dmpd
    }
  }

  dosage {
    dose {
      value = actualDosePerAdministration
      unit = unitOfMeasurementSnomed
    }

    route {
      coding {
        display = sactAdminRoute
        code = routeOfAdministrationSnomed
      }
    }
  }

  effectiveDateTime = adminDate
}