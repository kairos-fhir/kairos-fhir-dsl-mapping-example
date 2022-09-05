package repoconnect

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient

/**
 * Transforms a patient bundle.
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.2022.3.0
 */
bundle {
  context.bundles.each { final Bundle sourceBundle ->
    sourceBundle.getEntry()
        .findAll { final Bundle.BundleEntryComponent sourceEntry -> sourceEntry.getResource() != null }
        .findAll { final Bundle.BundleEntryComponent sourceEntry -> (sourceEntry.getResource().fhirType() == new Patient().fhirType()) }
        .each { final Bundle.BundleEntryComponent sourceEntry ->
          final Patient sourcePatient = sourceEntry.getResource() as Patient
          entry {
            resource {
              patient {
                id = sourcePatient.getId()

                final HumanName sourceName = sourcePatient.getNameFirstRep()
                humanName {
                  family = sourceName.getFamily()
                  sourceName.given.each {
                    given(it.getValue())
                  }
                }

                if (sourcePatient.hasGender()) {
                  gender = sourcePatient.getGender()
                }

                birthDate = sourcePatient.getBirthDateElement()

                identifier {
                  value = sourcePatient.getIdElement().getIdPart()
                  type {
                    coding {
                      system = FhirUrls.System.IdContainerType.BASE_URL
                      code = "MPI"
                    }
                  }
                }

                final Identifier sourceIdentifier = sourcePatient.getIdentifier().find { (it.system == "http://www.alpha-hospital.alp/patient-id") }
                if (sourceIdentifier != null) {
                  identifier {
                    value = sourceIdentifier.getValue()
                    type {
                      coding {
                        system = FhirUrls.System.IdContainerType.BASE_URL
                        code = "PATIENTID"
                      }
                    }
                  }
                }

                generalPractitioner {
                  identifier {
                    value = "CENTRAXX"
                  }
                }
              }
            }
          }
        }
  }
}
