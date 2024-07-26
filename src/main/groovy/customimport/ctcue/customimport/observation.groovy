package customimport.ctcue.customimport


import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborMethodCategory
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueType
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.ResourceType

// TODO: wait for example to figure out what category, method, and code actually mean in the ctcue context

bundle {

  context.bundles.each { final def bundle ->
    bundle.getEntry().findAll { it.getResource().getResourceType() == ResourceType.Observation }.each {

      final Observation sourceO = it.getResource() as Observation
      entry {
        fullUrl = "Observation/unknown"

        request {
          method = "POST"
          url = "Observation/unknown" // unknown is possible, because the matching is done by identifier.
        }

        resource {
          observation {
            id = sourceO.getId()

            meta {
              tag {
                system = FhirUrls.System.CXX_ENTITY
                code = "LaborFinding"
              }
            }

            extension {
              url = FhirUrls.Extension.LaborMapping.CREATE_PROFILE
              valueBoolean = true
            }

            extension {
              url = FhirUrls.Extension.LaborMapping.INCREMENT_LABORMETHOD_VERSION
              valueBoolean = true
            }

            extension {
              url = FhirUrls.Extension.LABOR_MAPPING
              extension {
                url = FhirUrls.Extension.LaborMapping.LABOR_MAPPING_TYPE
                valueString = sourceO.hasSpecimen() ? LaborMappingType.SAMPLELABORMAPPING as String : LaborMappingType.PATIENTLABORMAPPING as String
              }

              extension {
                url = FhirUrls.Extension.LaborMapping.PATIENT
                valueReference = sourceO.getSubject()
              }

              if (sourceO.hasEncounter()) {
                extension {
                  url = FhirUrls.Extension.LaborMapping.ENCOUNTER
                  valueReference = sourceO.getEncounter()
                }
              }

              extension {
                url = FhirUrls.Extension.LaborMapping.RELATED_REFERENCE
                valueReference = sourceO.hasSpecimen() ? sourceO.getSpecimen() : sourceO.getSubject()
              }
            }

            identifier {
              system = FhirUrls.System.Finding.LABOR_FINDING_ID
              value = sourceO.getIdentifierFirstRep().getValue()
            }

            // status is mapped later to a separate form parameter
            status = Observation.ObservationStatus.FINAL.toCode()

            category {
              coding {
                system = FhirUrls.System.LaborMethod.Category
                code = LaborMethodCategory.VITALSIGN as String
              }
            }

            code {
              coding {
                system = FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME
                code = sourceO.getIdentifier().getValue()
              }
            }

            subject = sourceO.getSubject()
            effectiveDateTime = sourceO.getEffectiveDateTimeType()


            component {


              code {
                coding {
                  system = FhirUrls.System.LaborValue.BASE_URL
                  code = "Questionnaire-Response-Status"
                }
              }
              valueString = sourceO.getStatus().toCode()
            }

            method {
              coding {
                system = FhirUrls.System.LaborMethod.BASE_URL
                version = 1
                code = sourceO.getMethod().getCodingFirstRep().getCode()
                display = sourceO.getMethod().getCodingFirstRep().getDisplay()
              }
            }

            if (sourceO.hasBodySite()) {

              if (sourceO.getBodySite().getCodingFirstRep().hasCode()) {
                component {
                  extension {
                    url = FhirUrls.Extension.LaborValue.LABORVALUETYPE
                    valueString = LaborValueType.STRING as String
                  }

                  // this is required to make some sense of the data in CXX
                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = "Observation.bodySite.coding.code"
                    }
                  }

                  valueString = sourceO.getBodySite().getCodingFirstRep().getCode()

                }

                if (sourceO.getBodySite().getCodingFirstRep().hasDisplay()) {
                  component {
                    extension {
                      url = FhirUrls.Extension.LaborValue.LABORVALUETYPE
                      valueString = LaborValueType.STRING as String
                    }

                    // this is required to make some sense of the data in CXX
                    code {
                      coding {
                        system = FhirUrls.System.LaborValue.BASE_URL
                        code = "Observation.bodySite.coding.display"
                      }
                    }

                    valueString = sourceO.getBodySite().getCodingFirstRep().getSystem()

                  }
                }
              }
            }


            // patient finder profile only specifies cases for string and quantity values.
            sourceO.getComponent().each { final def sourceComponent ->

              if (sourceComponent.hasValueStringType()) {
                component {
                  extension {
                    url = FhirUrls.Extension.LaborValue.LABORVALUETYPE
                    valueString = LaborValueType.STRING as String
                  }

                  // this is required to make some sense of the data in CXX
                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = sourceComponent.getCode().getCodingFirstRep().getCode()
                    }
                  }

                  valueString = sourceComponent.getValueStringType().getValue()
                }
              }

              if (sourceComponent.hasValueQuantity()) {
                component {
                  extension {
                    url = FhirUrls.Extension.LaborValue.LABORVALUETYPE
                    valueString = LaborValueType.DECIMAL as String
                  }

                  // this is required to make some sense of the data in CXX
                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = sourceComponent.getCode().getCodingFirstRep().getCode()
                    }
                  }

                  valueQuantity {
                    value = sourceComponent.getValueQuantity().getValue()
                    unit = sourceComponent.getValueQuantity().getUnit()
                    system = FhirUrls.System.LaborValue.Unit.BASE_URL
                    code = sourceComponent.getValueQuantity().getUnit()
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