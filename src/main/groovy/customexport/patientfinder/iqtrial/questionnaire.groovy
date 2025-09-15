package customexport.patientfinder.iqtrial

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.CrfTemplateSection
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.enums.LaborMethodCategory
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Questionnaire

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMethod

/**
 * transforms a HDRP LaborMethod to a Questionnaire.
 * @since v.1.52.0
 * @since HDRP.v.2025.3.0
 */
questionnaire {

  if (context.source[laborMethod().category()] != LaborMethodCategory.NURSING.toString()){
    return
  }

  id = "Questionnaire/" + context.source[laborMethod().id()]

  identifier {
    value = context.source[laborMethod().code()]
  }

  description(
      context.source[laborMethod().multilinguals()].find { final def ml ->
        ml[Multilingual.LANGUAGE] == "en" && ml[Multilingual.SHORT_NAME] != null
      }?.getAt(Multilingual.SHORT_NAME) as String
  )

  // using first section only
  final def firstSection = context.source[laborMethod().crfTemplate().sections()].find()

  firstSection[CrfTemplateSection.FIELDS].each { final def field ->
    item {
      setLinkId(field[CrfTemplateField.LABOR_VALUE]?.getAt(LaborValue.CODE) as String)

      setText(field[CrfTemplateField.LABOR_VALUE]
          ?.getAt(LaborValue.MULTILINGUALS)
          ?.find { it[Multilingual.LANGUAGE] == "en" }?.getAt(Multilingual.SHORT_NAME) as String
      )
      if (field[CrfTemplateField.LABOR_VALUE]?.getAt(LaborValue.D_TYPE)) {
        setType(mapToType(field[CrfTemplateField.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as LaborValueDType))
      }
    }
  }
}

// more possible, focusing on main types
static mapToType(final LaborValueDType laborValueDType) {
  if (laborValueDType == null) {
    return null
  }

  switch (laborValueDType) {
    case LaborValueDType.BOOLEAN: return Questionnaire.QuestionnaireItemType.BOOLEAN.toCode()
    case LaborValueDType.INTEGER: return Questionnaire.QuestionnaireItemType.INTEGER.toCode()
    case LaborValueDType.DECIMAL: return Questionnaire.QuestionnaireItemType.QUANTITY.toCode()
    case LaborValueDType.SLIDER: return Questionnaire.QuestionnaireItemType.QUANTITY.toCode()
    case LaborValueDType.DATE: return Questionnaire.QuestionnaireItemType.DATETIME.toCode()
    case LaborValueDType.LONGDATE: return Questionnaire.QuestionnaireItemType.DATETIME.toCode()
    case LaborValueDType.TIME: return Questionnaire.QuestionnaireItemType.TIME.toCode()
    case LaborValueDType.CATALOG: return Questionnaire.QuestionnaireItemType.CHOICE.toCode()
    case LaborValueDType.STRING: return Questionnaire.QuestionnaireItemType.TEXT.toCode()
    case LaborValueDType.LONGSTRING: return Questionnaire.QuestionnaireItemType.TEXT.toCode()
    default: println(laborValueDType.toString() + " not implemented")
  }
}
