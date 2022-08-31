package repoconnect

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Encounter

/**
 * Transforms encounter bundles.
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.2022.3.0
 */
bundle {
  context.bundles.each { final Bundle sourceBundle ->
    sourceBundle.getEntry()
        .findAll { final Bundle.BundleEntryComponent sourceEntry -> sourceEntry.getResource() != null }
        .findAll { final Bundle.BundleEntryComponent sourceEntry -> (sourceEntry.getResource().fhirType() == new Encounter().fhirType()) }
        .each { final Bundle.BundleEntryComponent sourceEntry ->
          final Encounter sourceEncounter = sourceEntry.getResource() as Encounter
          entry {
            resource {
              encounter {
                id = sourceEncounter.getId() // important for references in other resources

                final String sourceId = sourceEncounter.getIdElement().getIdPart()
                if (sourceId != null) {
                  identifier {
                    value = sourceId
                    type {
                      coding {
                        system = FhirUrls.System.IdContainerType.BASE_URL
                        code = "EPISODEID"
                      }
                    }
                  }
                }
                subject = sourceEncounter.getSubject()
                period = sourceEncounter.getPeriod()
                serviceProvider {
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
