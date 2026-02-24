package customexport.patientfinder.digione.mmci

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.centraxx.metamodel.PrecisionDate

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding

final String HEIGHT_AT_IMMUNO_START = "Height_at_immuno_start"
final String END_OF_THERAPY = "end_of_therapy"
final String COMPLETE_DOSE = "complete_dose"
final String ESMO_TREATMENT = "esmo_treatment"
final String START_OF_THERAPY = "start_of_therapy"
final String FRACTIONS = "fractions"
final String WEIGHT_AT_START = "weight_at_start"
final String TREATMENT_RADIO_METHOD = "treatment_radio_method"

final Map PROFILE_TYPES = [
    (HEIGHT_AT_IMMUNO_START): LaborFindingLaborValue.DATE_VALUE,
    (END_OF_THERAPY)        : LaborFindingLaborValue.NUMERIC_VALUE,
    (COMPLETE_DOSE)         : LaborFindingLaborValue.NUMERIC_VALUE,
    (ESMO_TREATMENT)        : LaborFindingLaborValue.STRING_VALUE,
    (START_OF_THERAPY)      : LaborFindingLaborValue.DATE_VALUE,
    (FRACTIONS)             : LaborFindingLaborValue.NUMERIC_VALUE,
    (WEIGHT_AT_START)       : LaborFindingLaborValue.NUMERIC_VALUE,
    (TREATMENT_RADIO_METHOD): LaborFindingLaborValue.STRING_VALUE
]

medicationAdministration {


  if (context.source[laborFinding().laborMethod().code()] != "TREATMENT") {
    return
  }

  final Map<String, Object> lflvMap = getLflvMap(context.source[laborFinding().laborFindingLaborValues()] as List, PROFILE_TYPES)

  final def treatmentRadioMethod = lflvMap.get(TREATMENT_RADIO_METHOD)

  if (treatmentRadioMethod == null){
    return
  }

  id = "MedicationAdministration/RadiationTherapy-" + context.source[laborFinding().id()]

  final def beginDate = lflvMap.get(START_OF_THERAPY)?.getAt(PrecisionDate.DATE)
  final def endDate = lflvMap.get(END_OF_THERAPY)?.getAt(PrecisionDate.DATE)

  if (beginDate != null || endDate != null) {
    effectivePeriod {
      start = beginDate
      end = endDate
    }
  }

  if (treatmentRadioMethod != null) {
    medication {
      medicationCodeableConcept {
        coding {
          code = treatmentRadioMethod as String
          display = treatmentRadioMethod as String
        }
      }
    }

  }

  final def completeDose = lflvMap.get(COMPLETE_DOSE)

  if (completeDose != null) {
    dosage {
      dose {
        value = completeDose
        unit = "Gy"
      }
    }
  }

  final def labMap = context.source[laborFinding().laborMappings()].find { final def lm ->
    lm[LaborMapping.RELATED_PATIENT] != null
  }

  if (labMap != null) {
    patient {
      reference = "Patient/" + labMap[LaborMapping.RELATED_PATIENT][PatientContainer.ID]
    }
  }

}

static Map<String, Object> getLflvMap(final List lflvs, final Map<String, String> types) {
  final Map<String, Object> lflvMap = [:]
  if (lflvs.isEmpty()) {
    return lflvMap
  }

  types.each { final String lvCode, final String lvType ->
    final def lflvForLv = lflvs.find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == lvCode
    }

    if (lflvForLv && lflvForLv[lvType]) {
      lflvMap[(lvCode)] = lflvForLv[lvType]
    }
  }
  return lflvMap
}

