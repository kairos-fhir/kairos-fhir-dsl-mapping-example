package projects.cxx.ctcue

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
      setLinkId(field[CrfTemplateField.LABOR_VALUE][LaborValue.CODE] as String)

      // assuming the question is stored as description
      setText(field[CrfTemplateField.LABOR_VALUE][LaborValue.DESC_MULTILINGUAL_ENTRIES].find { it[MultilingualEntry.LANG] == "en" }[MultilingualEntry.VALUE] as String)
      setType(mapToType(field[CrfTemplateField.LABOR_VALUE][LaborValue.D_TYPE] as LaborValueDType))
    }
  }

}

// more possible, focusing on main types
static mapToType(final LaborValueDType laborValueDType) {
  switch (laborValueDType) {
    case LaborValueDType.BOOLEAN:
      return Questionnaire.QuestionnaireItemType.BOOLEAN
    case LaborValueDType.INTEGER:
      return Questionnaire.QuestionnaireItemType.INTEGER
    case LaborValueDType.DECIMAL:
      return Questionnaire.QuestionnaireItemType.QUANTITY
    case LaborValueDType.DATE || LaborValueDType.LONGDATE:
      return Questionnaire.QuestionnaireItemType.DATETIME
    case LaborValueDType.TIME:
      return Questionnaire.QuestionnaireItemType.TIME
    case LaborValueDType.CATALOG:
      return Questionnaire.QuestionnaireItemType.CHOICE
    case LaborValueDType.STRING || LaborValueDType.LONGSTRING:
      return Questionnaire.QuestionnaireItemType.TEXT
  }
}
