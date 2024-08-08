package customimport.ctcue.customimport.simple

import ca.uhn.fhir.context.FhirContext
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborMethodCategory
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueType
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.IntegerType
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.StringType

/**
 * if the data doesn't matter for CXX at all, the incoming resource can just be written as a String to a finding
 * and attached to a dummy patient
 */
bundle {
  final FhirContext fhirContext = FhirContext.forR4()

  final Identifier dummyPatient = new Identifier()
      .setType(new CodeableConcept()
          .addCoding(new Coding()
              .setSystem(FhirUrls.System.IdContainerType.BASE_URL)
              .setCode("DUMMY_PATIENT_ID")))
      .setValue("1")

  context.bundles.each { final def bundle ->
    bundle.getEntry().findAll { it.hasResource() }.each {

      final Resource sourceResource = it.getResource()
      entry {
        fullUrl = "Observation/unknown"

        request {
          method = "POST"
          url = "Observation/unknown" // unknown is possible, because the matching is done by identifier.
        }

        resource {
          observation {
            id = "Observation/unknown"

            meta {
              tag {
                system = FhirUrls.System.CXX_ENTITY
                code = "LaborFinding"
              }
            }

            identifier {
              system = FhirUrls.System.Finding.LABOR_FINDING_ID
              value = sourceResource.getId()
            }

            // status is mapped later to a separate form parameter
            status = Observation.ObservationStatus.UNKNOWN.toCode()

            category {
              coding {
                system = FhirUrls.System.LaborMethod.Category
                code = LaborMethodCategory.GENERAL as String
              }
            }

            code {
              coding {
                system = FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME
                code = sourceResource.getId()
              }
            }

            subject {
              identifier = dummyPatient
            }

            effectiveDateTime = new Date()

            method {
              coding {
                system = FhirUrls.System.LaborMethod.BASE_URL
                // Original example references by a logical FHIR ID, which is not FHIR conform.
                // It is necessary to reference by a canonical URL which contains the HDRP/CXX labor method code.
                code = "FHIR_RESOURCE"
                display = "FHIR resource"
              }
            }

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
                valueString = LaborMappingType.PATIENTLABORMAPPING.toString()
              }
              extension {
                url = FhirUrls.Extension.LaborMapping.PATIENT
                valueReference {
                  identifier = dummyPatient
                }
              }
              extension {
                url = FhirUrls.Extension.LaborMapping.RELATED_REFERENCE
                valueReference {
                  identifier = dummyPatient
                }
              }
            }
            status = Observation.ObservationStatus.FINAL

            component {
              extension = createLaborValueExtension()

              code {
                coding {
                  system = FhirUrls.System.LaborValue.BASE_URL
                  code = "FHIR_RESOURCE_TYPE"
                  display = "ResourceType"
                }
              }

              valueString = sourceResource.getResourceType() as String
            }

            component {
              extension = createLaborValueExtension()

              code {
                coding {
                  system = FhirUrls.System.LaborValue.BASE_URL
                  code = "FHIR_RESOURCE"
                  display = "FHIR resource"
                }
              }

              valueString = fhirContext.newJsonParser().encodeResourceToString(sourceResource)
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
          new StringType(LaborValueType.LONGSTRING.toString())
      ),
      new Extension(
          FhirUrls.Extension.LaborValue.VALUE_INDEX,
          new IntegerType(0)
      )
  ]
}