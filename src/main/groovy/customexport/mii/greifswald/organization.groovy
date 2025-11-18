package customexport.mii.greifswald

import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

/**
 * MII needs an address, which HDRP does not support
 */
organization {
  id = context.source[organizationUnit().id()]

  name = context.source[organizationUnit().code()]
}