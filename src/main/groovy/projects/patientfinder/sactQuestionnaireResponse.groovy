package projects.patientfinder


import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.QuestionnaireResponse

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractCodeName.NAME_MULTILINGUAL_ENTRIES
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

questionnaireResponse {

  if (!(context.source[laborMapping().laborFinding().laborMethod().code()] == "SACT_Profile")) {
    return
  }

  id = "QuestionnaireResponse/" + context.source[laborMapping().laborFinding().id()]

  identifier {
    value = context.source[laborMapping().laborFinding().shortName()]
  }

  questionnaire { value = "Questionnaire/" + context.source[laborMapping().laborFinding().laborMethod().id()] }

  status = QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  authored { date = context.source[laborMapping().laborFinding().findingDate().date()] }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final def lflv ->
    final def laborValue = lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE]
    item {
      linkId = laborValue[CODE] as String
      text = laborValue[NAME_MULTILINGUAL_ENTRIES].find { final def me -> me[LANG] == "en"}?.getAt(VALUE) as String

      if (isNumeric(laborValue)) {
        answer {
          valueQuantity {
            value = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
            unit = laborValue?.getAt(LaborValueNumeric.UNIT)?.getAt(CODE) as String
          }
        }
      } else if (isBoolean(laborValue)) {
        answer {
          setValueBoolean(lflv[LaborFindingLaborValue.BOOLEAN_VALUE] as Boolean)
        }
      } else if (isDate(laborValue)) {
        answer {
          valueDateTime {
            date = lflv[LaborFindingLaborValue.DATE_VALUE]?.getAt(PrecisionDate.DATE)
          }
        }
      } else if (isTime(laborValue)) {
        answer {
          setValueTime(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
        }
      } else if (isString(laborValue)) {
        answer {
          setValueString(lflv[LaborFindingLaborValue.STRING_VALUE] as String)
        }
      } else if (isEnumeration(laborValue)) {

        lflv[LaborFindingLaborValue.MULTI_VALUE].each { final entry ->
          answer {
            valueCoding {
              code = entry[CODE] as String
              display = entry[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
            }
          }
        }
        lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          answer {
            valueCoding {
              code = entry[CODE] as String
              display = entry[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
            }
          }
        }

      } else if (isOptionGroup(laborValue)) {

        lflv[LaborFindingLaborValue.MULTI_VALUE].each { final entry ->
          answer {
            valueCoding {
              code = entry[CODE] as String
              display = entry[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
            }
          }
        }
        lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          answer {
            valueCoding {
              code = entry[CODE] as String
              display = entry[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
            }
          }
        }

      } else if (isCatalog(laborValue)) {

        lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          answer {
            valueCoding {
              code = entry[CODE] as String
              display = entry[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
            }
          }
        }
        lflv[LaborFindingLaborValue.ICD_ENTRY_VALUE].each { final entry ->
          answer {
            valueCoding {
              code = entry[CODE] as String
              display = entry[IcdEntry.PREFERRED_LONG] as String
            }
          }
        }

      } else {
        final String msg = laborValue?.getAt(LaborValue.D_TYPE) + " not implemented yet."
        System.out.println(msg)
      }
    }

  }

}

static boolean isDTypeOf(final Object laborValue, final List<LaborValueDType> types) {
  return types.contains(laborValue?.getAt(LaborValue.D_TYPE) as LaborValueDType)
}

static boolean isBoolean(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.BOOLEAN])
}

static boolean isNumeric(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.INTEGER, LaborValueDType.DECIMAL, LaborValueDType.SLIDER])
}

static boolean isDate(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.DATE, LaborValueDType.LONGDATE])
}

static boolean isTime(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.TIME])
}

static boolean isEnumeration(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.ENUMERATION])
}

static boolean isString(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.STRING, LaborValueDType.LONGSTRING])
}

static boolean isCatalog(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.CATALOG])
}

static boolean isOptionGroup(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.OPTIONGROUP])
}

