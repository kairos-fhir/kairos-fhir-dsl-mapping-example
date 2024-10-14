package projects.patientfinder

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import org.hl7.fhir.r4.model.AllergyIntolerance

import static de.kairos.fhir.centraxx.metamodel.RecordedValue.DATE_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.STRING_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Jonas Küttner
 * @since CXX.v.2024.3.7, CXX.v.2024.4.0, kairos-fhir-dsl-1.35.0
 */
allergyIntolerance {

  if (!(context.source[laborMapping().laborFinding().laborMethod().code()] as String).equals("Allergen")) {
    return
  }

  id = "AllergyIntolerance/" + context.source[laborMapping().laborFinding().id()]

  final List labFinLabVals = context.source[laborMapping().laborFinding().laborFindingLaborValues()] as List

  final def lvClinicalStatus = findLabFindLabVal(labFinLabVals, "Allergen.Status")
  if (lvClinicalStatus) {
    clinicalStatus {
      coding {
        code = lvClinicalStatus[STRING_VALUE] as String
      }
    }
  }

  final def lflvCategory = findLabFindLabVal(labFinLabVals, "Allergen.Category")

  if (lflvCategory) {
    category {
      value = lflvCategory[STRING_VALUE] as String
    }
  }

  patient {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  final def lflvOnsetStart = findLabFindLabVal(labFinLabVals, "Start")
  final def lflvOnsetEnd = findLabFindLabVal(labFinLabVals, "Finish")

  if ((lflvOnsetStart && lflvOnsetStart[DATE_VALUE]) && (!lflvOnsetEnd || !lflvOnsetEnd[DATE_VALUE])) {
    onsetDateTime {
      date = lflvOnsetStart[DATE_VALUE][PrecisionDate.DATE]
    }
  } else if ((lflvOnsetStart && lflvOnsetStart[DATE_VALUE]) && (lflvOnsetEnd && lflvOnsetEnd[DATE_VALUE])) {
    onsetPeriod {
      start {
        date = normalizeDate(lflvOnsetStart[DATE_VALUE][PrecisionDate.DATE] as String)
      }
      end {
        date = normalizeDate(lflvOnsetEnd[DATE_VALUE][PrecisionDate.DATE] as String)
      }
    }
  }

  final def lflvRecordedDate = findLabFindLabVal(labFinLabVals, "Record date")

  if (lflvRecordedDate && lflvRecordedDate[DATE_VALUE]) {
    recordedDate = normalizeDate(lflvRecordedDate[DATE_VALUE][PrecisionDate.DATE] as String)
  }

  reaction {
    final def lflvAllergen = findLabFindLabVal(labFinLabVals, "Allergen.Allergen")
    if (lflvAllergen) {
      substance {
        coding {
          code = lflvAllergen[STRING_VALUE] as String
        }
      }
    }

    final def lflvRecation = findLabFindLabVal(labFinLabVals, "Allergen.Reaction")

    if (lflvRecation) {

      manifestation {
        coding {
          code = lflvRecation[STRING_VALUE] as String
        }
      }
    }

    final def lflvSeverity = findLabFindLabVal(labFinLabVals, "Allergen.Severity")

    if (lflvSeverity) {
      severity(filterSeverity(lflvSeverity[STRING_VALUE] as String))
    }
  }


}

static def findLabFindLabVal(final List labFinLabVals, final String code) {
  return labFinLabVals.find {
    it[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE].equals(code)
  }
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String filterSeverity(final String severity) {
  if (!severity) {
    return null
  } else if (severity.equalsIgnoreCase(AllergyIntolerance.AllergyIntoleranceSeverity.MILD.toCode())) {
    return AllergyIntolerance.AllergyIntoleranceSeverity.MILD.toCode()
  } else if (severity.equalsIgnoreCase(AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE.toCode())) {
    return AllergyIntolerance.AllergyIntoleranceSeverity.MODERATE.toCode()
  } else if (severity.equalsIgnoreCase(AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE.toCode())) {
    return AllergyIntolerance.AllergyIntoleranceSeverity.SEVERE.toCode()
  }
  return null
}
