package projects.uscore


import org.hl7.fhir.r4.model.Goal

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.CATALOG_ENTRY_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a CXX LaborMapping for the US Core Resource Profile: US Core Goal Profile.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-goal.html
 *
 * @author Mike Wähnert, Niklas Biedka
 * @since v.1.14.0, CXX.v.2022.1.0
 */
goal {

  if ("US_CORE_GOAL" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Goal/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-goal")
  }

  final def lblvDescription = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_GOAL_DESCRIPTION"
  }

  description {
    text = (lblvDescription[CATALOG_ENTRY_VALUE] as List)[0][CODE] as String
  }

  final def lblvLifecycleStatus = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_GOAL_LIFECYCLE_STATUS"
  }

  lifecycleStatus = Goal.GoalLifecycleStatus.fromCode((lblvLifecycleStatus[CATALOG_ENTRY_VALUE] as List)[0][CODE] as String)

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  final def lblvDueDate = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_GOAL_DUE_DATE"
  }

  target {
    dueDate = (lblvDueDate[CATALOG_ENTRY_VALUE] as List)[0][CODE] as String
  }

}
