package customimport.ctcue.customimport

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueType
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.IntegerType
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Procedure
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.StringType

// TODO: performer, location
// TODO: needs to be tested with example data
bundle {

  context.bundles.each { final def bundle ->
    bundle.getEntry().findAll { it.getResource().getResourceType() == ResourceType.Procedure }.each {

      final Procedure sourceP = it.getResource() as Procedure
      entry {
        fullUrl = "Procedure/unknown"

        request {
          method = "POST"
          url = "Procedure/unknown" // unknown is possible, because the matching is done by identifier.
        }

        resource {
          procedure {
            id = "unknown"

            meta {
              tag {
                system = FhirUrls.System.CXX_ENTITY
                code = "MedProcedure"
              }
            }

            identifier.add(sourceP.getIdentifierFirstRep());

            setStatus(sourceP.getStatus())

            // Code is mandatory in CXX and must be chosen from a CodeSystem
            // catalogs have to be created
            code {
              coding.add(sourceP.getCode().getCodingFirstRep())
            }

            //reference by identifier
            subject = sourceP.getSubject()

            //mandatory in CXX
            performedDateTime = sourceP.getPerformed()

            //mandatory in CXX -> create Fake encounter
            encounter = sourceP.getEncounter()
          }
        }
      }

      // create labor mapping with additional data
      entry {
        fullUrl = "Observation/ProcedureLaborMapping-unknown"

        request {
          method = "POST"
          url = "Observation/ProcedureLaborMapping-unknown"
        }

        resource {
          observation {

            id = "ProcedureLaborMapping-" + sourceP.getId()
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
                valueString = LaborMappingType.MEDPROCEDURE.toString()
              }
              extension {
                url = FhirUrls.Extension.LaborMapping.PATIENT
                value = sourceP.getSubject()
              }
              extension {
                url = FhirUrls.Extension.LaborMapping.RELATED_REFERENCE
                valueReference {
                  identifier = sourceP.getIdentifierFirstRep()
                }
              }
            }
            status = Observation.ObservationStatus.FINAL

            identifier {
              system = FhirUrls.System.Finding.LABOR_FINDING_ID
              value = "AdditionalProcedureData_" + sourceP.getIdentifierFirstRep().getValue()
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
                code = "AdditionalProcedureData_" + sourceP.getIdentifierFirstRep().getValue()
                display = "AdditionalProcedureData_" + sourceP.getIdentifierFirstRep().getValue()
              }
            }

            subject = sourceP.getSubject()

            method {
              coding {
                system = FhirUrls.System.LaborMethod.BASE_URL
                code = "ADDITIONAL_PROCEDURE_DATA"
                display = "Additional Procedure Data"
              }
            }


            if (sourceP.hasPartOf()) {
              component {
                extension = createLaborValueExtension()

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "PROCEDURE_PARENT"
                    display = "Procedure parent"
                  }
                }

                valueString = sourceP.getPartOfFirstRep().getIdentifier().getValue()
              }
            }

            if (sourceP.hasCategory()) {
              component {
                extension = createLaborValueExtension()

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "PROCEDURE_CATEGORY"
                    display = "Procedure catagegory"
                  }
                }

                valueString = sourceP.getCategory().getCodingFirstRep().getCode()
              }
            }

          }
        }
      }


      sourceP.getBodySite().eachWithIndex { final CodeableConcept bs, final int i ->
        if (bs.getCodingFirstRep()) {
          entry {
            fullUrl = "Observation/ProcedureLaborMappingBodySite-unknown"

            request {
              method = "POST"
              url = "Observation/ProcedureLaborMappingBodySite-unknown"
            }
            resource {
              observation {

                id = "ProcedureLaborMapping-" + sourceP.getId()
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
                    valueString = LaborMappingType.MEDPROCEDURE.toString()
                  }
                  extension {
                    url = FhirUrls.Extension.LaborMapping.PATIENT
                    value = sourceP.getSubject()
                  }
                  extension {
                    url = FhirUrls.Extension.LaborMapping.RELATED_REFERENCE
                    valueReference {
                      identifier = sourceP.getIdentifierFirstRep()
                    }
                  }
                }
                status = Observation.ObservationStatus.FINAL

                identifier {
                  system = FhirUrls.System.Finding.LABOR_FINDING_ID
                  value = "AdditionalProcedureData_bodysite_" + sourceP.getIdentifierFirstRep().getValue() + "_" + i
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
                    code = "AdditionalProcedureData_bodysite_" + sourceP.getIdentifierFirstRep().getValue() + "_" + i
                  }
                }

                subject = sourceP.getSubject()

                method {
                  coding {
                    system = FhirUrls.System.LaborMethod.BASE_URL
                    code = "ADDITIONAL_PROCEDURE_BODYSITE_DATA"
                    display = "Additional procedure data bodysite"
                  }
                }


                if (bs.getCodingFirstRep().hasCode()) {
                  component {
                    extension = createLaborValueExtension()

                    code {
                      coding {
                        system = FhirUrls.System.LaborValue.BASE_URL
                        code = "PROCEDURE_BODY_SITE_CODE"
                        display = "Procedure body site code"
                      }
                    }

                    valueString = bs.getCodingFirstRep().getCode()
                  }
                }

                if (bs.getCodingFirstRep().hasDisplay()) {
                  component {
                    extension = createLaborValueExtension()

                    code {
                      coding {
                        system = FhirUrls.System.LaborValue.BASE_URL
                        code = "PROCEDURE_BODY_SITE_DISPLAY"
                        display = "Procedure body site display"
                      }
                    }

                    valueString = bs.getCodingFirstRep().getCode()
                  }
                }
              }
            }
          }
        }
      }

      // create fake encounter if not given
      if (!sourceP.hasEncounter()) {
        entry {
          fullUrl = "Encounter/unknown"

          request {
            method = "POST"
            url = "Encounter/unknown"
          }

          resource {
            encounter {
              id = "unknown"


              identifier {
                system = FhirUrls.System.Episode.CXX_EPISODE_ID
                value = "FakeEncounter-" + sourceP.getIdentifierFirstRep().getValue()
              }

              // this encounter class needs to be created; can be used to filter on export
              class_ {
                system = FhirUrls.System.Episode.StayType
                code = "FAKE"
              }

            }
          }
        }
      }
    }
  }
}


static List<Extension> createLaborValueExtension() {
  return [
      new Extension(
          FhirUrls.Extension.LaborValue.LABORVALUETYPE,
          new StringType(LaborValueType.STRING.toString())
      ),
      new Extension(
          FhirUrls.Extension.LaborValue.VALUE_INDEX,
          new IntegerType(0)
      )
  ]
}


