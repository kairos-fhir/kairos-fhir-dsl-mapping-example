package projects.uscore

import org.hl7.fhir.r4.model.CarePlan

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.CATALOG_ENTRY_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static org.hl7.fhir.r4.model.CarePlan.CarePlanStatus.fromCode
/**
 * Represents a CXX LaborMapping for the US Core Resource Profile: US Core CarePlan Profile.
 * Specified by https://hl7.org/fhir/us/core/STU4/StructureDefinition-us-core-careplan.html
 *
 * The Script works with a CentraXX measurement profile with the code US_CORE_CARE_PLAN
 * The master data definition for this profile can be found in xml/carePlan.xml and can be imported over
 * the CentraXX XML import interface
 *
 * The export of the "text" field is not supported yet
 *
 * @author Mike Wähnert, Jonas Küttner
 * @since v.1.14.0, CXX.v.2022.1.0
 */

carePlan {

  if ("US_CORE_CARE_PLAN" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "CarePlan/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-careplan")
  }

  final def lblvNarrative = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_CARE_PLAN_NARRATIVE" }


  final def lblvStatus = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_CARE_PLAN_STATUS" }

  status = fromCode((lblvStatus[CATALOG_ENTRY_VALUE] as List)[0][CODE] as String)

  final def lblvIntent = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_CARE_PLAN_INTENT" }

  intent = CarePlan.CarePlanIntent.fromCode((lblvIntent[CATALOG_ENTRY_VALUE] as List)[0][CODE] as String)

  category {
    coding {
      system = "http://hl7.org/fhir/us/core/CodeSystem/careplan-category"
      code = "assess-plan"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }
}


