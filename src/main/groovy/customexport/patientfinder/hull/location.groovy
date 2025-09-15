package customexport.patientfinder.hull


import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientTransfer

/**
 * Represented by a HDRP PatientTransfer of an episode
 * @author Jonas Küttner
 * @since v.1.47.0, HDRP.v.2024.5.7
 */
location {
  id = "Location/PT-" + context.source[patientTransfer().id()]

  name = context.source[patientTransfer().bed().multilinguals()].find {final def ml ->
    ml[Multilingual.LANGUAGE] == "en" && ml[Multilingual.SHORT_NAME] != null
  }?.getAt(Multilingual.SHORT_NAME)

  if (context.source[patientTransfer().habitation()]) {
    managingOrganization {
      reference = "Organization/" + context.source[patientTransfer().habitation().id()]
    }
  }
}
