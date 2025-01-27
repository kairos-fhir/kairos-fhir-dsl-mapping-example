package customimport.ctcue.customimport

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.DateType
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Quantity
import org.hl7.fhir.r4.model.QuestionnaireResponse
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.StringType
import org.hl7.fhir.r4.model.Type

bundle {

  context.bundles.each { final def bundle ->
    bundle.getEntry().findAll { it.getResource().getResourceType() == ResourceType.QuestionnaireResponse }.each {

      final QuestionnaireResponse sourceQR = it.getResource() as QuestionnaireResponse
      entry {
        fullUrl = "Observation/unknown"

        request {
          method = "POST"
          url = "Observation/unknown" // unknown is possible, because the matching is done by identifier.
        }

        resource {
          observation {
            id = sourceQR.getId()

            meta {
              tag {
                system = FhirUrls.System.CXX_ENTITY
                code = "LaborFinding"
              }
            }

            identifier {
              system = FhirUrls.System.Finding.LABOR_FINDING_ID
              value = sourceQR.getIdentifier().getValue()
            }

            // status is mapped later to a separate form parameter
            status = Observation.ObservationStatus.UNKNOWN.toCode()

            category {
              coding {
                system = FhirUrls.System.LaborMethod.Category
                code = "GENERAL"
              }
            }

            code {
              coding {
                system = FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME
                code = sourceQR.getIdentifier().getValue()
              }
            }

            subject = sourceQR.getSubject()
            effectiveDateTime = sourceQR.getAuthored()

            method {
              coding {
                system = FhirUrls.System.LaborMethod.BASE_URL
                // Original example references by a logical FHIR ID, which is not FHIR conform.
                // It is necessary to reference by a canonical URL which contains the HDRP/CXX labor method code.
                code = sourceQR.getQuestionnaireElement().value.replaceFirst("#Questionnaire/", "")
              }
            }

            sourceQR.item.each { def sourceItem ->
              component {
                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = sourceItem.getLinkId()
                    display = sourceItem.getText()
                  }
                }

                Type sourceValue = sourceItem.getAnswerFirstRep().getValue()
                if (sourceValue instanceof StringType) {
                  valueString = (sourceValue as StringType).getValue()
                } else if (sourceValue instanceof DateType) {
                  valueDateTime {
                    date = (sourceValue as DateType).getValue()
                    precision = TemporalPrecisionEnum.DAY.name()
                  }
                } else if (sourceValue instanceof Quantity) {
                  valueQuantity = sourceValue
                } else if (sourceValue instanceof Coding) {
                  valueString {
                    value = (sourceValue as Coding).getCode()
                  }
                } else {
                  println("AnswerType '" + sourceValue.getClass().getSimpleName() + " ' not supported yet.")
                }
              }
            }
          }
        }
      }
    }
  }
}