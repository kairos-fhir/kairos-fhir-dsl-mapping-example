package customexport.patientfinder.digione

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding

/**
 * map the sample_id and sample_date from the biomarkers finding to a Specimen
 */

final String SAMPLE_ID = "sample_id" //done
final String SAMPLE_DATE = "sample_date" // not relevant here

final Map PROFILE_TYPES = [
    (SAMPLE_ID)  : LaborFindingLaborValue.STRING_VALUE,
    (SAMPLE_DATE): LaborFindingLaborValue.DATE_VALUE
]

specimen {
  if (context.source[laborFinding().laborMethod().code()] != "BIOMARKERS") {
    return
  }

  final Map<String, Object> lflvMap = getLflvMap(context.source[laborFinding().laborFindingLaborValues()] as List, PROFILE_TYPES)

  final String bioMarkerSampleId = getValueOrNull(lflvMap.get(SAMPLE_ID) as String)

  if (bioMarkerSampleId == null) {
    return
  }

  id = "Specimen/" + removeBackSlashes(bioMarkerSampleId)
}

static String getValueOrNull(final String value) {
  if (value == null) {
    return null
  }

  if (value == "N/A") {
    return null
  }

  return value
}

static String removeBackSlashes(final String s){
  s.replace("/", "-")
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
