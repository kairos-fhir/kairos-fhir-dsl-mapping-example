package customexport.patientfinder.digione.mmci

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding
import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * Represented by a HDRP LaborMapping
 * @author Jonas Küttner
 * @since HDRP v.2025.1.0, v.2024.5.2
 */

final String ECOG = "ecog_index" // done

final Map PROFILE_TYPES = [
    (ECOG): LaborFindingLaborValue.STRING_VALUE,
]

observation {

  if (context.source[laborFinding().laborMethod().code()] != "ECOG") {
    return
  }

  final Map<String, Object> lflvMap = getLflvMap(context.source[laborFinding().laborFindingLaborValues()] as List, PROFILE_TYPES)

  final String ecog = getValueOrNull(lflvMap.get(ECOG) as String)

  if (isBlank(ecog)) {
    return
  }

  final String[] split = ecog.split(";")

  if (split.size() != 2) {
    return
  }

  final String karnofskyVal = split[0]
  final String ecogVal = split[1]

  id = "Observation/EcogIndex-" + context.source[laborFinding().id()]

  status = Observation.ObservationStatus.UNKNOWN

  if (context.source[laborFinding().laborFindingId()] != null) {
    identifier {
      system = FhirUrls.System.Finding.LABOR_FINDING_ID
      value = context.source[laborFinding().laborFindingId()]
    }
  }

  code {
    coding {
      code = context.source[laborFinding().laborMethod().code()] as String
      display = context.source[laborFinding().laborMethod().multilinguals()].find { final def ml ->
        ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
      }?.getAt(Multilingual.SHORT_NAME)
    }
  }

  effectiveDateTime {
    date = context.source[laborFinding().findingDate().date()]
  }

  if (karnofskyVal != null && "NA" != karnofskyVal) {
    component {
      code {
        coding {
          code = "karnofsky"
          display = "Karnofsky"
        }
      }
      valueString(karnofskyVal)
    }
  } else if (ecogVal != null && "NA" != ecogVal) {
    component {
      code {
        coding {
          code = "ecog"
          display = "Ecog"
        }
      }
      valueString(ecogVal)
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

static String getValueOrNull(final String value) {
  if (value == null) {
    return null
  }

  if (value == "N/A") {
    return null
  }

  return value
}


static Float parseResult(final String result) {
  if (result == null) {
    return null
  }

  try {
    return Float.parseFloat(result)
  } catch (final NumberFormatException ignored) {
    return null
  }
}

static String removeBackSlashes(final String s) {
  return s.replace("/", "-")
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



