package customimport.mii.modulperson

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.ResearchSubject
import org.hl7.fhir.r4.model.ResourceType

/**
 * transforms MII research subject to CXX research subject
 * Consent is not supported for research subjects, since the consent it self is linked to the patient the research subject is also linked to.
 * CXX requires a study center (extension) and the actualArm field to be set, which are not mandatory in MII.
 */
bundle {

  // filter for patient entries
  context.bundles.each { final def bundle ->
    bundle.getEntry().findAll { it.getResource().getResourceType() == ResourceType.ResearchSubject }
        .each {
          final ResearchSubject sourceSubject = it.getResource() as ResearchSubject

          // get bundle request to set to entries. Could also transform it here.
          final Bundle.BundleEntryRequestComponent requestToSet = it.getRequest()


          entry {
            request = requestToSet
            resource {
              researchSubject {
                id = sourceSubject.getId()

                // study center is mandatory
                extension = sourceSubject.getExtension()

                if (sourceSubject.hasIdentifier()) {
                  identifier {
                    system = FhirUrls.System.StudyMember.INTERNAL_STUDYMEMBERID
                    value = sourceSubject.getIdentifierFirstRep().getValue()
                  }
                }

                status = sourceSubject.getStatus()

                if (sourceSubject.hasPeriod()) {
                  period = sourceSubject.getPeriod()
                }

                study = sourceSubject.getStudy()
                individual = sourceSubject.getIndividual()
                // actualArm is mandatory in CXX
                actualArm = sourceSubject.getActualArm()
              }
            }
          }
        }
  }
}

