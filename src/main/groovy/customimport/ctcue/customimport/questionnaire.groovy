package customimport.ctcue.customimport

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.enums.CrfFieldType
import de.kairos.fhir.centraxx.metamodel.enums.CrfSectionType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueType
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.ResourceType

/**
 * TODO: WORK IN PROGRESS
 */
bundle {

  context.bundles.each { final def bundle ->
    bundle.getEntry().findAll { it.getResource().getResourceType() == ResourceType.Questionnaire }.each {

      final Questionnaire sourceQ = it.getResource() as Questionnaire
      entry {
        fullUrl = "Questionnaire/LaborMethod-unknown"

        request {
          method = "POST"
          url = "Questionnaire/LaborMethod-unknown" // unknown is possible, because the matching is done by identifier.
        }

        resource {
          questionnaire {
            id = "LaborMethod-unknown"

            meta {
              tag {
                system = FhirUrls.System.CXX_ENTITY
                code = "LaborMethod"
              }
            }

            title = sourceQ.getDescription()
            status = Enumerations.PublicationStatus.ACTIVE

            code {
              system = FhirUrls.System.CrfTemplate.BASE_URL
              code = sourceQ.getDescription()
              version = "1"
            }

            identifier {
              system = FhirUrls.System.LaborMethod.BASE_URL
              value = sourceQ.getIdentifierFirstRep().getValue()
            }

            setName(sourceQ.getIdentifierFirstRep().getValue())

            // root section
            item {
              extension {
                url = FhirUrls.Extension.CrfTemplate.Section.TYPE
                valueCoding {
                  system = FhirUrls.System.CrfTemplate.Section.Type.BASE_URL
                  setCode(CrfSectionType.PLAIN.toString())
                }
              }

              linkId = "Section-1"
              code {
                system = FhirUrls.System.CrfTemplate.Section.BASE_URL
                code = "Section-1"
                display = "Section-1"
              }

              for (final sourceItem in sourceQ.getItem()) {
                //filtering choice objects. For them to be mapped, they need to reference a catalog, or have some answeroptions.
                if (sourceItem.getType() in [Questionnaire.QuestionnaireItemType.CHOICE, Questionnaire.QuestionnaireItemType.OPENCHOICE]) {
                  continue
                }
                item {
                  linkId = sourceItem.getLinkId()
                  code {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = sourceItem.getLinkId()
                    display = sourceItem.getText()
                  }
                  type = sourceItem.getType()
                  extension {
                    url = FhirUrls.Extension.LaborValue.LABORVALUETYPE
                    valueString = getLaborValueType(sourceItem.getType())
                  }
                  extension {
                    url = FhirUrls.Extension.CrfTemplate.Section.Field.CRFFIELDTYPE
                    valueString = CrfFieldType.LABORVALUE
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

static String getLaborValueType(final Questionnaire.QuestionnaireItemType questionnaireItemType) {
  if (questionnaireItemType == null) {
    return null;
  }
  switch (questionnaireItemType) {
    case Questionnaire.QuestionnaireItemType.GROUP: return null// TODO create sub section
    case Questionnaire.QuestionnaireItemType.DISPLAY: return null; // TODO create a label
    case Questionnaire.QuestionnaireItemType.REFERENCE: return null // not mapped
    case Questionnaire.QuestionnaireItemType.QUESTION: return LaborValueType.STRING.name()
    case Questionnaire.QuestionnaireItemType.BOOLEAN: return LaborValueType.BOOLEAN.name()
    case Questionnaire.QuestionnaireItemType.DECIMAL: return LaborValueType.DECIMAL.name()
    case Questionnaire.QuestionnaireItemType.INTEGER: return LaborValueType.INTEGER.name()
    case Questionnaire.QuestionnaireItemType.DATE: return LaborValueType.DATE.name()
    case Questionnaire.QuestionnaireItemType.DATETIME: return LaborValueType.DATE.name()
    case Questionnaire.QuestionnaireItemType.TIME: return LaborValueType.TIME.name()
    case Questionnaire.QuestionnaireItemType.STRING: return LaborValueType.STRING.name()
    case Questionnaire.QuestionnaireItemType.TEXT: return LaborValueType.LONGSTRING.name()
    case Questionnaire.QuestionnaireItemType.URL: return LaborValueType.LINK.name()
    case Questionnaire.QuestionnaireItemType.CHOICE: return LaborValueType.CHECKOPTIONGROUP.name()
    case Questionnaire.QuestionnaireItemType.OPENCHOICE: return LaborValueType.CHECKOPTIONGROUP.name()
    case Questionnaire.QuestionnaireItemType.ATTACHMENT: return LaborValueType.FILE.name()
    case Questionnaire.QuestionnaireItemType.QUANTITY: LaborValueType.DECIMAL.name()
  }
}