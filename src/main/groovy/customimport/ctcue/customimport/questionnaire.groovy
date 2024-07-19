package customimport.ctcue.customimport


import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.enums.CrfFieldType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.ResourceType

/**
 * TODO: WORK IN PROGRESS
 */
bundle {

  context.bundles.each { final def bundle ->
    bundle.getEntry().findAll { it.getResource().getResourceType() == ResourceType.Questionnaire }.each {

      final Questionnaire sourceQR = it.getResource() as Questionnaire
      entry {
        fullUrl = "Questionnaire/unknown"

        request {
          method = "POST"
          url = "Questionnaire/unknown" // unknown is possible, because the matching is done by identifier.
        }

        resource {
          questionnaire {
            id = sourceQR.getId()
            title = sourceQR.getDescription()
            status = sourceQR.getStatus()
            code {
              system = FhirUrls.System.CrfTemplate.BASE_URL
              code = sourceQR.getDescription()
              version = "1"
            }


            // root section
            item {
              linkId = "Section-1"
              code {
                system = FhirUrls.System.CrfTemplate.Section.BASE_URL
                code = "Section-1"
                display = "Section-1"
              }

              for (sourceItem in sourceQR.getItem()) {
                item {
                  linkId = sourceItem.getLinkId()
                  code {
                    system = FhirUrls.System.CrfTemplate.Section.Field.BASE_URL
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
    case Questionnaire.QuestionnaireItemType.QUESTION: return LaborValueDType.STRING.name()
    case Questionnaire.QuestionnaireItemType.BOOLEAN: return LaborValueDType.BOOLEAN.name()
    case Questionnaire.QuestionnaireItemType.DECIMAL: return LaborValueDType.DECIMAL.name()
    case Questionnaire.QuestionnaireItemType.INTEGER: return LaborValueDType.INTEGER.name()
    case Questionnaire.QuestionnaireItemType.DATE: return LaborValueDType.DATE.name()
    case Questionnaire.QuestionnaireItemType.DATETIME: return LaborValueDType.DATE.name()
    case Questionnaire.QuestionnaireItemType.TIME: return LaborValueDType.TIME.name()
    case Questionnaire.QuestionnaireItemType.STRING: return LaborValueDType.STRING.name()
    case Questionnaire.QuestionnaireItemType.TEXT: return LaborValueDType.LONGSTRING.name()
    case Questionnaire.QuestionnaireItemType.URL: return LaborValueDType.LINK.name()
    case Questionnaire.QuestionnaireItemType.CHOICE: return LaborValueDType.ENUMERATION.name()
    case Questionnaire.QuestionnaireItemType.OPENCHOICE: return LaborValueDType.ENUMERATION.name()
    case Questionnaire.QuestionnaireItemType.ATTACHMENT: return LaborValueDType.FILE.name()
    case Questionnaire.QuestionnaireItemType.QUANTITY: LaborValueDType.DECIMAL.name()
  }
}