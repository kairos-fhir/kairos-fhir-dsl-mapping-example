package projects.patientfinder

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate

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
        date = lflvOnsetStart[DATE_VALUE][PrecisionDate.DATE]
      }
      end {
        date = lflvOnsetEnd[DATE_VALUE][PrecisionDate.DATE]
      }
    }
  }

  final def lflvRecordedDate = findLabFindLabVal(labFinLabVals, "Record date")

  if (lflvRecordedDate && lflvRecordedDate[DATE_VALUE]) {
    recordedDate = lflvRecordedDate[DATE_VALUE][PrecisionDate.DATE]
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
      severity(lflvSeverity[STRING_VALUE] as String)
    }
  }


}

static def findLabFindLabVal(final List labFinLabVals, final String code) {
  return labFinLabVals.find {
    it[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE].equals(code)
  }
}
