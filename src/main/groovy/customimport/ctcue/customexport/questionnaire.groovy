package customimport.ctcue.customexport

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.CrfTemplateSection
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Questionnaire

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMethod

/**
 * transforms a CXX LaborMethod to a Questionnaire.
 */
questionnaire {

  id = "Questionnaire/" + context.source[laborMethod().id()]

  identifier {
    value = context.source[laborMethod().code()]
  }

  // using first section only
  final def firstSection = context.source[laborMethod().crfTemplate().sections()].find()

  firstSection[CrfTemplateSection.FIELDS].each { final def field ->
    item {
      setLinkId(field[CrfTemplateField.LABOR_VALUE]?.getAt(LaborValue.CODE) as String)

      // assuming the question is stored as description
      setText(field[CrfTemplateField.LABOR_VALUE]
          ?.getAt(LaborValue.DESC_MULTILINGUAL_ENTRIES)
          ?.find { it[MultilingualEntry.LANG] == "en" }?.getAt(MultilingualEntry.VALUE) as String
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
