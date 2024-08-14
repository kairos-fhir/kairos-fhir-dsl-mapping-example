package repoconnect


import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Procedure
/**
 * Transforms encounter bundles.
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.2022.3.0
 */
bundle {
  context.bundles.each { final Bundle sourceBundle ->
    sourceBundle.getEntry()
        .findAll { final Bundle.BundleEntryComponent sourceEntry -> sourceEntry.getResource() != null }
        .findAll { final Bundle.BundleEntryComponent sourceEntry -> (sourceEntry.getResource().fhirType() == new Procedure().fhirType()) }
        .each { final Bundle.BundleEntryComponent sourceEntry ->
          final Procedure sourceProcedure = sourceEntry.getResource() as Procedure
          entry {
            resource {
              procedure {
                id = sourceProcedure.getId()
                final String sourceId = sourceProcedure.getIdElement().getIdPart()
                if (sourceId != null) {
                  identifier {
                    value = sourceId
                    system = "urn:centraxx"

                  }
                }
                if (sourceProcedure.hasCode()) {
                  code {
                    final Coding snomedCtCoding = sourceProcedure.getCode().getCoding().find { it.getSystem() == "http://snomed.info/sct" }
                    if (snomedCtCoding != null) {
                      coding {
                        system = "urn:centraxx:CodeSystem/OpsCatalog-2"
                        version = "code"
                        code = mapSctToOps(snomedCtCoding.getCode())
                      }
                    }
                  }
                }

                subject = sourceProcedure.getSubject()
                encounter = sourceProcedure.getEncounter() // mandatory

                if (sourceProcedure.hasPerformedDateTimeType()) {
                  performedDateTime = sourceProcedure.getPerformedDateTimeType()
                } else if (sourceProcedure.hasPerformedPeriod()) {
                  performedDateTime = sourceProcedure.getPerformedPeriod().getStart()
                } else if (sourceProcedure.getMeta().hasLastUpdated()) {
                  performedDateTime = sourceProcedure.getMeta().getLastUpdated()
                } else {
                  performedDateTime = new Date()
                }

                note = sourceProcedure.getNote()
              }
            }
          }
        }
  }
}

static String mapSctToOps(final String snomedCtCode) {
  if (snomedCtCode == "430193006") { // Medication Reconciliation (procedure)
    return "1-10" // Klinische Untersuchung
  }
  //TODO: add other mappings and defaults or filter
  return "9-990" // Obduktion
}