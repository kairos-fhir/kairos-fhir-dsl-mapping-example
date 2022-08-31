package repoconnect


import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition

/**
 * Transforms condition bundles.
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.2022.3.0
 */
bundle {
  context.bundles.each { final Bundle sourceBundle ->
    sourceBundle.getEntry()
        .findAll { final Bundle.BundleEntryComponent sourceEntry -> sourceEntry.getResource() != null }
        .findAll { final Bundle.BundleEntryComponent sourceEntry -> (sourceEntry.getResource().fhirType() == new Condition().fhirType()) }
        .each { final Bundle.BundleEntryComponent sourceEntry ->
          final Condition sourceCondition = sourceEntry.getResource() as Condition
          entry {
            resource {
              condition {
                id = sourceCondition.getId()
                final String sourceId = sourceCondition.getIdElement().getIdPart()
                if (sourceId != null) {
                  identifier {
                    value = sourceId
                    system = "urn:centraxx"

                  }
                }
                if (sourceCondition.hasCode()) {
                  code {
                    final Coding snomedCtCoding = sourceCondition.getCode().getCoding().find { it.getSystem() == "http://snomed.info/sct" }
                    if (snomedCtCoding != null) {
                      coding {
                        system = "urn:centraxx:CodeSystem/IcdCatalog-16"
                        version = "code"
                        code = mapSctToIcd10(snomedCtCoding.getCode())
                      }
                    }
                  }
                }

                // onsetDateTime is mandatory and business key
                if (sourceCondition.hasOnsetDateTimeType()) {
                  onsetDateTime = sourceCondition.getOnsetDateTimeType();
                } else if (sourceCondition.hasRecordedDate()) {
                  onsetDateTime = sourceCondition.getRecordedDateElement()
                } else if (sourceCondition.getMeta().hasLastUpdated()) {
                  onsetDateTime = sourceCondition.getMeta().getLastUpdated()
                } else {
                  onsetDateTime = new Date()
                }

                subject = sourceCondition.getSubject()
                recordedDate = sourceCondition.getRecordedDateElement()
                note = sourceCondition.getNote();
                encounter = sourceCondition.getEncounter()
              }
            }
          }
        }
  }
}

static String mapSctToIcd10(final String snomedCtCode) {
  if (snomedCtCode == "65363002") { // Otitis Media
    return "H66.9"
  }
  //TODO: add other mappings
  return "U99" // not assigned
}