package customexport.patientfinder.digione.mmci

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.QuestionnaireResponse

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractCodeSyncIdMultilingual.MULTILINGUALS
import static de.kairos.fhir.centraxx.metamodel.LaborValue.D_TYPE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

questionnaireResponse {

  if (["BIOMARKERS", "ECOG"].contains(context.source[laborMapping().laborFinding().laborMethod().code()])) {
    return
  }

  // exclude these
  final radiationTherapyLflv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find { final def lflv ->
    lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == "treatment_radio_method"
  }

  if (radiationTherapyLflv != null) {
    return
  }

  id = "QuestionnaireResponse/General-" + context.source[laborMapping().laborFinding().id()]

  identifier {
    value = context.source[laborMapping().laborFinding().shortName()]
  }

  status = QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  authored {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final def lflv ->
    final def laborValue = lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE]

    item {
      linkId = laborValue[CODE]

      if (lflv[LaborFindingLaborValue.STRING_VALUE] != null) {
        answer {
          valueString = lflv.getAt(LaborFindingLaborValue.STRING_VALUE) as String
        }
      } else if (lflv[LaborFindingLaborValue.NUMERIC_VALUE] != null) {
        answer {
          valueQuantity {
            value = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
            unit = laborValue?.getAt(LaborValueNumeric.UNIT)?.getAt(CODE) as String
          }
        }
      } else if (lflv[LaborFindingLaborValue.DATE_VALUE]) {
        answer {
          valueDateTime {
            date = lflv[LaborFindingLaborValue.DATE_VALUE]?.getAt(PrecisionDate.DATE)
          }
        }
      } else if (lflv[LaborFindingLaborValue.BOOLEAN_VALUE]) {
        answer {
          setValueBoolean(lflv[LaborFindingLaborValue.BOOLEAN_VALUE] as Boolean)
        }
      } else if (lflv[LaborFindingLaborValue.BOOLEAN_VALUE]) {
        answer {
          setValueTime(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
        }
      } else if (!(lflv[LaborFindingLaborValue.MULTI_VALUE] as List).isEmpty()) {
        lflv[LaborFindingLaborValue.MULTI_VALUE].each { final entry ->
          answer {
            valueCoding {
              code = entry[CODE]
              display = entry[MULTILINGUALS].find { final def ml ->
                ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
              }?.getAt(Multilingual.SHORT_NAME)
            }
          }
        }
      } else if (!(lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE] as List).isEmpty()) {
        lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          answer {
            valueCoding {
              code = entry[CODE]
              display = entry[MULTILINGUALS].find { final def ml ->
                ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
              }?.getAt(Multilingual.SHORT_NAME)
            }
          }
        }
      } else if (!(lflv[LaborFindingLaborValue.ICD_ENTRY_VALUE] as List).isEmpty()) {

        lflv[LaborFindingLaborValue.ICD_ENTRY_VALUE].each { final entry ->
          answer {
            valueCoding {
              code = entry[CODE]
              display = entry[IcdEntry.PREFERRED]
            }
          }
        }
      } else if (!(lflv[LaborFindingLaborValue.OPS_ENTRY_VALUE] as List).isEmpty()) {
        lflv[LaborFindingLaborValue.OPS_ENTRY_VALUE].each { final entry ->
          answer {
            valueCoding {
              code = entry[CODE]
              display = entry[IcdEntry.PREFERRED]
            }
          }
        }
      }
    }
  }
}

static boolean isDTypeOf(final Object laborValue, final List<LaborValueDType> types) {
  return types.contains(laborValue?.getAt(D_TYPE) as LaborValueDType)
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