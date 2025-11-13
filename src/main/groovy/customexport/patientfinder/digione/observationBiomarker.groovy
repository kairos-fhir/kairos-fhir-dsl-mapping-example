package customexport.patientfinder.digione

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractCodeSyncIdMultilingual.MULTILINGUALS
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding

/**
 * Represented by a HDRP LaborMapping
 * @author Jonas Küttner
 * @since HDRP v.2025.1.0, v.2024.5.2
 */

final String BIOMARKER_CODE = "biomarker_code" // done
final String BIOMARKER_RESULT = "biomarker_result" // done
final String BIOMARKER_TYPE_VOCABULARY = "biomarker_type_vocabulary" //done
final String SAMPLE_ID = "sample_id" //done
final String BIOMARKER_NAME = "biomarker_name" // done
final String BIOMARKER_UNIT = "biomarker_unit" // done
final String SAMPLE_DATE = "sample_date" // not relevant here

final Map PROFILE_TYPES = [
    (BIOMARKER_CODE)           : LaborFindingLaborValue.STRING_VALUE,
    (BIOMARKER_RESULT)         : LaborFindingLaborValue.STRING_VALUE,
    (BIOMARKER_TYPE_VOCABULARY): LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (SAMPLE_ID)                : LaborFindingLaborValue.STRING_VALUE,
    (BIOMARKER_NAME)           : LaborFindingLaborValue.STRING_VALUE,
    (BIOMARKER_UNIT)           : LaborFindingLaborValue.STRING_VALUE,
    (SAMPLE_DATE)              : LaborFindingLaborValue.DATE_VALUE
]

observation {

  if (context.source[laborFinding().laborMethod().code()] != "BIOMARKERS") {
    return
  }

  final Map<String, Object> lflvMap = getLflvMap(context.source[laborFinding().laborFindingLaborValues()] as List, PROFILE_TYPES)

  final String bioMarkerName = getValueOrNull(lflvMap.get(BIOMARKER_NAME) as String)
  final String bioMarkerResult = getValueOrNull(lflvMap.get(BIOMARKER_RESULT) as String)
  final String bioMarkerCode = getValueOrNull(lflvMap.get(BIOMARKER_CODE) as String) != null ? lflvMap.get(BIOMARKER_CODE) : bioMarkerName

  if (bioMarkerName == null || bioMarkerResult == null){
    return
  }

  id = "Observation/" + context.source[laborFinding().id()]

  status = Observation.ObservationStatus.UNKNOWN

  identifier {
    value = context.source[laborFinding().laborFindingId()]
  }

  code {
    coding {
      code = bioMarkerCode
      display = bioMarkerName
    }
  }

  effectiveDateTime {
    date = context.source[laborFinding().findingDate().date()]
  }

  final def bioMarkerType = lflvMap.get(BIOMARKER_TYPE_VOCABULARY) as List
  if (bioMarkerType != null && !bioMarkerType.isEmpty()){
    category {
      bioMarkerType.each { final def entry ->
        coding {
          code = entry[CODE] as String
          display = entry[MULTILINGUALS].find { final def ml ->
            ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
          }?.getAt(Multilingual.SHORT_NAME)
        }
      }
    }
  }


  final Float numericResult = parseResult(bioMarkerResult)

  if (numericResult != null){
    valueQuantity {
      value = numericResult
      unit = getValueOrNull(lflvMap.get(BIOMARKER_UNIT) as String)
    }
  } else {
    valueString(bioMarkerResult)
  }

  final def labMap = context.source[laborFinding().laborMappings()].find { final def lm ->
    lm[LaborMapping.RELATED_PATIENT] != null
  }

  if (labMap != null) {
    patient {
      reference = "Patient/" + labMap[LaborMapping.RELATED_PATIENT][PatientContainer.ID]
    }
  }

  final String sampleId = getValueOrNull(lflvMap.get(SAMPLE_ID) as String)
  if (sampleId != null) {
    specimen {
      reference = "Specimen/" + sampleId
    }
  }

  method {
    coding {
      code = context.source[laborFinding().laborMethod().code()] as String
      display = context.source[laborFinding().laborMethod().multilinguals()].find { final def ml ->
        ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
      }?.getAt(Multilingual.SHORT_NAME)
    }
  }

}

static String getValueOrNull(final String value){
  if (value == null){
    return null
  }

  if (value == "N/A"){
    return null
  }

  return value
}


static Float parseResult(final String result){
  if (result == null) {
    return  null
  }

  try {
    return Float.parseFloat(result)
  } catch (final NumberFormatException ignored) {
    return null
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


