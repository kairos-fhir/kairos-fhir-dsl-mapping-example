package customimport.example

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.ResourceType

bundle {

  // filter for patient entries
  context.bundles.each {final def bundle ->
    bundle.getEntry().findAll { it.getResource().getResourceType() == ResourceType.Patient }
        .each {
          final Patient patient = it.getResource() as Patient

          // get bundle request to set to entries. Could also transform it here.
          final Bundle.BundleEntryRequestComponent requestToSet = it.getRequest()

          println(patient.generalPractitioner.reference.toString())

          entry {
            fullUrl = "something"
            resource = patient
            request = requestToSet
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
              request = requestToSet
            }
          }
        }
  }
}