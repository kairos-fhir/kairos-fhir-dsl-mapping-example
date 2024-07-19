package customimport.ctcue.customimport

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.ResourceType

bundle {

  // filter for patient entries
  context.bundles.each { final def bundle ->
    bundle.getEntry().findAll { it.getResource().getResourceType() == ResourceType.Patient }.each {

      final Bundle.BundleEntryRequestComponent sourceRequest = it.getRequest()
      final Patient sourcePatient = it.getResource() as Patient

      entry {
        fullUrl = "something"
        request = sourceRequest
        resource {
          patient {
            id = sourcePatient.getId()
            meta = sourcePatient.getMeta()
            identifier {
              type {
                coding {
                  system = FhirUrls.System.IdContainerType.BASE_URL
                  code = "MPI"
                }
              }
              value = sourcePatient.getIdentifierFirstRep().getValue()
            }
            name = sourcePatient.getName()
            telecom = sourcePatient.getTelecom()
            gender = sourcePatient.getGender()
            birthDate = sourcePatient.getBirthDate()
            address = sourcePatient.getAddress()
            generalPractitioner {
              reference = "Organization/1"
            }
            // TODO add practitioner as a custom CtCue measurement parameter
          }
        }
      }
    }
  }
}