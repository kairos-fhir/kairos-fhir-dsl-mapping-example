package projects.uscore

import org.hl7.fhir.r4.model.Goal

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a CXX LaborMapping for the US Core Resource Profile: US Core Goal Profile.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-goal.html
 *
 * TODO: work in progress
 *
 * @author Mike Wähnert
 * @since v.1.14.0, CXX.v.2022.1.0
 */
goal {

  id = "Goal/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-goal")
  }

  lifecycleStatus = Goal.GoalLifecycleStatus.PROPOSED

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  target {
    dueDate = new Date()
  }

}