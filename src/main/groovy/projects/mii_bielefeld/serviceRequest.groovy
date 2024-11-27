package projects.mii_bielefeld

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding

/**
 * dummy script to create a service request for each Diagnostic report in order
 * Diagnostic report requires a basedOn  Reference
 */
serviceRequest {
  id = "ServiceRequest/" + context.source[laborFinding().id()]
}