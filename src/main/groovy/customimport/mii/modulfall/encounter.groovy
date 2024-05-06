package customimport.mii.modulfall

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.IntegerType
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.StringType

/**
 * transforms MII research subject to CXX research subject
 * Consent is not supported for research subjects, since the consent it self is linked to the patient the research subject is also linked to.
 * CXX requires a study center (extension) and the actualArm field to be set, which are not mandatory in MII.
 *
 * TODO: incomplete, not all fields are mapped
 * required: import the CodeSystems of the Bindings as Catalogs into CXX
 */
bundle {

  // filter for patient entries
  context.bundles.each { final def bundle ->
    bundle.getEntry().findAll { it.getResource().getResourceType() == ResourceType.Encounter }
        .each {
          final Encounter sourceEncounter = it.getResource() as Encounter

          // get bundle request to set to entries. Could also transform it here.
          final Bundle.BundleEntryRequestComponent requestToSet = it.getRequest()

          final Identifier aufnahmeNummer = sourceEncounter.identifier.find {
            it.type?.codingFirstRep?.system == "http://terminology.hl7.org/CodeSystem/v2-0203" && it.type?.codingFirstRep?.code == "VN"
          }


          if (aufnahmeNummer == null) {
            return
          }

          final Identifier episodeIdentifier = new Identifier()
              .setType(
                  new CodeableConcept()
                      .addCoding(
                          new Coding()
                              .setSystem(FhirUrls.System.IdContainerType.BASE_URL)
                              .setCode("VN"))
              )
              .setValue(aufnahmeNummer.getValue())


          entry {
            request = requestToSet
            resource {
              encounter {
                id = sourceEncounter.getId()


                identifier = [episodeIdentifier]


                status = sourceEncounter.getStatus()

                // stay types must be defined accordingly
                class_ = sourceEncounter.getClass_()

                period = sourceEncounter.getPeriod()

                subject = sourceEncounter.getSubject()

                // service provider is mandatory for CXX
                if (sourceEncounter.hasServiceProvider()) {
                  serviceProvider = sourceEncounter.getServiceProvider()
                } else {
                  serviceProvider {
                    identifier {
                      system = FhirUrls.System.ORGANIZATION_UNIT
                      value = "CENTRAXX"
                    }
                  }
                }
              }
            }
          }

          entry {
            request = requestToSet
            resource {
              observation {

                id = "Observation/EpisodeLaborMapping-" + sourceEncounter.getId()
                // Trying to create the master data here from
                extension {
                  url = FhirUrls.Extension.LaborMapping.CREATE_PROFILE
                  valueBoolean = true
                }
                extension {
                  url = FhirUrls.Extension.LaborMapping.INCREMENT_LABORMETHOD_VERSION
                  valueBoolean = false
                }
                extension {
                  url = FhirUrls.Extension.LABOR_MAPPING
                  extension {
                    url = FhirUrls.Extension.LaborMapping.LABOR_MAPPING_TYPE
                    valueString = "EPISODE"
                  }
                  extension {
                    url = FhirUrls.Extension.LaborMapping.PATIENT
                    value = sourceEncounter.getSubject()
                  }
                  extension {
                    url = FhirUrls.Extension.LaborMapping.ENCOUNTER
                    valueReference {
                      identifier = episodeIdentifier
                    }
                  }
                  extension {
                    url = FhirUrls.Extension.LaborMapping.RELATED_REFERENCE
                    valueReference {
                      identifier = episodeIdentifier
                    }
                  }
                }
                status = Observation.ObservationStatus.UNKNOWN

                identifier {
                  system = FhirUrls.System.Finding.LABOR_FINDING_ID
                  value = "AdditionalEpisodeData_" + episodeIdentifier.getValue()
                }
                category {
                  coding {
                    system = FhirUrls.System.LaborMethod.Category.BASE_URL
                    code = "GENERAL"
                  }
                }

                code {
                  coding {
                    system = FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME
                    code = "Additional Diagnosis Data"
                    display = "Additional Diagnosis Data"
                  }
                }

                subject = sourceEncounter.getSubject()

                method {
                  coding {
                    system = FhirUrls.System.LaborMethod.BASE_URL
                    code = "ADDITIONAL_DIAG_DATA"
                    display = "Additional Diagnosis Data"
                  }
                }


                if (sourceEncounter.hasType()) {
                  final CodeableConcept kontaktEbene = sourceEncounter.getType().find {
                    final def t -> t.getCoding().any { final def c -> c.getSystem().equals("http://fhir.de/CodeSystem/Kontaktebene") }
                  }

                  if (kontaktEbene != null) {

                    component {
                      extension = createCatalogSelectOneExtension()

                      code {
                        coding {
                          system = FhirUrls.System.LaborValue.BASE_URL
                          code = "KONTAKT_EBENE"
                          display = "Kontaktebene"
                        }
                      }

                      // put the right catalog Oid here
                      valueCodeableConcept {
                        coding {
                          system = "urn:centraxx:CodeSystem/CustomCatalog-1469"
                          code = kontaktEbene.getCodingFirstRep().getCode()
                        }
                      }


                    }
                  }

                  final CodeableConcept kontaktArt = sourceEncounter.getType().find {
                    final def t -> t.getCoding().any { final def c -> c.getSystem().equals("http://fhir.de/CodeSystem/kontaktart-de") }
                  }

                  if (kontaktArt != null) {

                    component {
                      extension = createCatalogSelectOneExtension()


                      code {
                        coding {
                          system = FhirUrls.System.LaborValue.BASE_URL
                          code = "KONTAKT_ART"
                          display = "Kontaktart"
                        }
                      }

                      // put the right catalog Oid here
                      valueCodeableConcept {
                        coding {
                          system = "urn:centraxx:CodeSystem/CustomCatalog-1470"
                          code = kontaktArt.getCodingFirstRep().getCode()
                        }
                      }
                    }
                  }
                }
              }
            }

          }
        }
  }
}

static List<Extension> createCatalogSelectOneExtension() {
  return [
      new Extension(
          FhirUrls.Extension.LaborValue.LABORVALUETYPE,
          new StringType("CATALOG")
      ),
      new Extension(
          FhirUrls.Extension.LaborValue.CHOICE_TYPE,
          new StringType("SELECTONE")
      ),
      new Extension(
          FhirUrls.Extension.LaborValue.VALUE_INDEX,
          new IntegerType(0)
      )
  ]
}

