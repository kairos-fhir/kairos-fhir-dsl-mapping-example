package repoconnect

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Quantity
import org.hl7.fhir.r4.model.Specimen

/**
 * Transforms a specimen bundles.
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.2022.3.0
 */
bundle {
  context.bundles.each { final Bundle sourceBundle ->
    sourceBundle.getEntry()
        .findAll { final Bundle.BundleEntryComponent sourceEntry -> sourceEntry.getResource() != null }
        .findAll { final Bundle.BundleEntryComponent sourceEntry -> (sourceEntry.getResource().fhirType() == new Specimen().fhirType()) }
        .each { final Bundle.BundleEntryComponent sourceEntry ->
          final Specimen sourceSpecimen = sourceEntry.getResource() as Specimen
          entry {
            resource {
              specimen {
                id = sourceSpecimen.getId()

                final String sourceId = sourceSpecimen.getIdElement().getIdPart()
                if (sourceId != null) {
                  identifier {
                    value = sourceId
                    type {
                      coding {
                        system = FhirUrls.System.IdContainerType.BASE_URL
                        code = "SAMPLEID"
                      }
                    }
                  }
                }

                subject = sourceSpecimen.getSubject()
                final String snomedSpecimenType = findSnomedSpecimenType(sourceSpecimen)
                final String cxxSampleTypeCode = mapToCxxSampleType(snomedSpecimenType)
                if (cxxSampleTypeCode != null) {
                  type {
                    coding {
                      system = FhirUrls.System.Sample.SampleType.BASE_URL
                      code = cxxSampleTypeCode
                    }
                  }
                }

                container {
                  final Quantity sourceQuantity = sourceSpecimen.getContainerFirstRep().getSpecimenQuantity()
                  if (sourceQuantity != null) {
                    specimenQuantity {
                      value = sourceQuantity.value
                    }
                  }
                }
              }
            }
          }
        }
  }
}

private static String findSnomedSpecimenType(final Specimen sourceSpecimen) {
  for (final CodeableConcept type : sourceSpecimen.getType()) {
    for (final Coding coding : type.getCoding()) {
      if (coding.getSystem() == "http://snomed.info/sct") {
        return coding.getCode()
      }
    }
  }
  return null
}

private static String mapToCxxSampleType(final String snomedSpecimenType) {
  if (snomedSpecimenType == "119361006") { //Plasma specimen
    return "PLA" // Plasma
  } else if (snomedSpecimenType == "87612001") { //Blood (substance)
    return "BLD" //Blood
  } else {
    return null //TODO add other mappings
  }
}