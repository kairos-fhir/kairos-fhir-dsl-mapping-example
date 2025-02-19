package projects.mii.greifswald

import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

organization {
  id = context.source[organizationUnit().id()]
}