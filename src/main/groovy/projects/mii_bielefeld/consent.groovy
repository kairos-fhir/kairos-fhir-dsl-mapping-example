package projects.mii_bielefeld

import de.kairos.fhir.centraxx.metamodel.RootEntities

// script to export consent for referential integrity
consent {
  id = context.source[RootEntities.consent().id()]
}