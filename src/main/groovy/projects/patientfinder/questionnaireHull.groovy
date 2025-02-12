package projects.patientfinder

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.CrfTemplateSection
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import de.kairos.fhir.centraxx.metamodel.enums.LaborMethodCategory
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Questionnaire

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMethod

/**
 * transforms a CXX LaborMethod to a Questionnaire.
 */
questionnaire {

  if (context.source[laborMethod().category()] as LaborMethodCategory != LaborMethodCategory.LABOR){
    return
  }

  id = "Questionnaire/" + context.source[laborMethod().id()]

  identifier {
    value = context.source[laborMethod().code()]
  }

  description(
      context.source[laborMethod().nameMultilingualEntries()].find { final def me ->
        "en" == me[MultilingualEntry.LANG]
      }?.getAt(MultilingualEntry.VALUE) as String
  )

  // using first section only
  final def firstSection = context.source[laborMethod().crfTemplate().sections()].find()

  firstSection[CrfTemplateSection.FIELDS].each { final def field ->
    item {
      setLinkId(field[CrfTemplateField.LABOR_VALUE]?.getAt(LaborValue.CODE) as String)

      setText(field[CrfTemplateField.LABOR_VALUE]
          ?.getAt(LaborValue.NAME_MULTILINGUAL_ENTRIES)
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
