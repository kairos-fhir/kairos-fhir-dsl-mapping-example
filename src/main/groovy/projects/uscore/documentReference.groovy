package projects.uscore

import de.kairos.fhir.centraxx.metamodel.RootEntities
import org.hl7.fhir.r4.model.Enumerations

/**
 * Represents a CXX DocumentMapping.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-documentreference.html
 *
 * TODO: work in progress
 *
 * @author Mike Wähnert
 * @since v.1.14.0, CXX.v.2022.1.0
 */
documentReference {

  id = "DocumentReference/" + context.source[RootEntities.documentMapping().id()]

  identifier {
    system = "urn:centraxx"
    value = context.source[RootEntities.documentMapping().finding().documentId()]
  }

  status = Enumerations.DocumentReferenceStatus.CURRENT
}