package projects.mii.bielefeld

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding

/**
 * dummy script to create a service request for each Diagnostic report in order
 * Diagnostic report requires a basedOn  Reference
 * @author Jonas Küttner
 * @since v.1.43.0, CXX.v.2024.5.0
 */
serviceRequest {
  id = "ServiceRequest/" + context.source[laborFinding().id()]
}