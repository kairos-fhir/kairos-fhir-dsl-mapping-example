package projects.uscore

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a CXX LaborMapping for the US Core Resource Profile: US Core Provenance profile.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-provenance.html
 *
 * TODO: work in progress
 *
 * @author Mike Wähnert
 * @since v.1.14.0, CXX.v.2022.1.0
 */
provenance {

  id = "Provenance/" + context.source[laborMapping().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-provenance")
  }

  target {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }
}
