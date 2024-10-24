package projects.patientfinder

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.CrfTemplateSection
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import de.kairos.fhir.centraxx.metamodel.enums.CrfFieldType

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMethod

questionnaire {

  if (context.source[laborMethod().code()] != "SACT_Profile") {
    return
  }

  id = "Questionnaire/" + context.source[laborMethod().id()]

  identifier {
    value = context.source[laborMethod().code()]
  }

  context.source[laborMethod().crfTemplate().sections()].each {final def section ->
    section[CrfTemplateSection.FIELDS].findAll { final def field ->
      field[CrfTemplateField.TYPE] == CrfFieldType.LABORVALUE as String
    }.each {final def field ->
      item {
        linkId  = field[CrfTemplateField.LABOR_VALUE][LaborValue.CODE]
        text = field[CrfTemplateField.LABOR_VALUE][LaborValue.NAME_MULTILINGUAL_ENTRIES].find {
          final def me -> me[MultilingualEntry.LANG] == "en"
        }?.getAt(MultilingualEntry.VALUE) as String
      }
    }
  }
}