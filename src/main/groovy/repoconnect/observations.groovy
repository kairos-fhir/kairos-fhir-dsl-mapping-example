package repoconnect

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.ResourceType

/**
 * Transforms encounter bundles.
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.2022.3.0
 */
bundle {
  context.bundles.each { final Bundle sourceBundle ->
    sourceBundle.getEntry()
        .collect { it.getResource() }
        .findAll { it != null }
        .findAll { it.fhirType() == ResourceType.Observation.name() }
        .collect { it as Observation }
        .findAll { it.getCode().getCoding().find { it.getSystem() == "http://loinc.org" && it.getCode() == "29463-7" } } // body weight
        .each { final Observation sourceObservation ->
          entry {
            resource {
              observation {
                id = sourceObservation.getId() // important for references in other resources

                if (sourceObservation.hasEffectiveDateTimeType()) {
                  effectiveDateTime = sourceObservation.getEffectiveDateTimeType()
                }

                // must exists as labor method in CXX with all required labor value definitions. Staging in CXX cannot create master data yet
                method {
                  coding {
                    system = FhirUrls.System.LaborMethod.BASE_URL
                    version = 1
                    code = "FHIR-EXAMPLE-LABOR-METHOD"
                  }
                }

                code {
                  coding {
                    system = FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME
                    code = sourceObservation.getCode().getCoding().find { it.getSystem() == "http://loinc.org" }.display
                  }
                }

                subject = sourceObservation.getSubject()

                component {
                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = "WEIGHT" // must exists as labor value in CXX
                    }
                  }

                  valueQuantity = sourceObservation.getValueQuantity()
                }
              }
            }
          }
        }
  }
}
