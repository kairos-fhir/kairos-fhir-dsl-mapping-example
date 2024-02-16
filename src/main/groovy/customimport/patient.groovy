package customimport

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Patient

/**
 * Transforms condition bundles.
 * @author Jonas Küttner
 * @since v.1.9.0, CXX.v.2024.1.0
 */
bundle {
  if (!context.bundleEntryComponent.hasResource()) {
    return
  }

  final Patient patient = context.bundleEntryComponent.getResource() as Patient

  println(patient.generalPractitioner.reference.toString())

  entry {
    fullUrl = "something"
    resource = patient
  }

  final def sid = patient.identifier.find {
    it.type.codingFirstRep.code == "SID"
  }

  if (sid != null) {
    entry {
      resource {
        encounter {
          id = "992"
          identifier {
            system = FhirUrls.System.Episode.CXX_EPISODE_ID
            value = "New Episode Id Test"
          }

          subject {
            identifier = sid
          }

          serviceProvider = patient.generalPractitioner[0]
        }
      }
    }
  }
}