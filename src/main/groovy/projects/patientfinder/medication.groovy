package projects.patientfinder

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.enums.MedicationServiceType
import de.kairos.fhir.dsl.r4.context.Context

import javax.annotation.Nullable

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication


/**
 * Represents a CXX MedicationAdministration -> extracts the Medication data
 *
 * @author Jonas Küttner
 * v.1.43.0, CXX.v.2024.5.4
 */


final String CONCENTRATION_STRENTH = "concentration_strenth"
final String CONCENTRATION_STRENTH_UNIT = "concentration_strenth_unit"
final String STRENGTHTEXT = "strengthtext"

final Map PROFILE_TYPES = [
    (CONCENTRATION_STRENTH)     : LaborFindingLaborValue.NUMERIC_VALUE,
    (CONCENTRATION_STRENTH_UNIT): LaborFindingLaborValue.STRING_VALUE,
    (STRENGTHTEXT)              : LaborFindingLaborValue.STRING_VALUE,
]

medication {

  if (context.source[medication().serviceType()] != MedicationServiceType.MED.name()) {
    return
  }

  id = "Medication/" + context.source[medication().fillerOrderNumber()]

  final def mapping = context.source[medication().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "MedicationCatalog_profile"
  }

  final Map<String, Object> lflvMap = getLflvMap(mapping, PROFILE_TYPES)

  code {
    coding {
      system = FhirUrls.System.Medication.BASE_URL
      code = context.source[medication().code()] as String
      display = context.source[medication().name()] as String
    }
  }

  if (medication().applicationMedium()) {
    form {
      coding {
        code = context.source[medication().applicationMedium()] as String
      }
    }
  }

  ingredient {
    itemCodeableConcept {
      if (context.source[medication().code()]) {
        coding {
          code = context.source[medication().code()] as String
        }
      }
    }

    if (lflvMap.containsKey(CONCENTRATION_STRENTH)) {
      strength {
        numerator {
          value = lflvMap[CONCENTRATION_STRENTH]
          if (lflvMap.containsKey(CONCENTRATION_STRENTH_UNIT)) {
            unit = lflvMap[CONCENTRATION_STRENTH_UNIT] as String
          }
        }
      }
    } else if (lflvMap.containsKey(STRENGTHTEXT)) {
      strength {
        numerator {
          value = sanitizeScale(lflvMap[STRENGTHTEXT] as String)
        }
      }
    }
  }
}

@Nullable
static BigDecimal sanitizeScale(final String numeric) {
  try {
    return BigDecimal.valueOf(Double.parseDouble(numeric))
  } catch (final NumberFormatException | NullPointerException ignored) {
    return null
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

static boolean isFakeEpisode(final def episode) {
  if (episode == null) {
    return true
  }

  if (["SACT", "COSD"].contains(episode[Episode.ENTITY_SOURCE])) {
    return true
  }

  final def fakeId = episode[Episode.ID_CONTAINER]?.find { (it[PSN] as String).toUpperCase().startsWith("FAKE") }
  return fakeId != null
}

static Map<String, Object> getLflvMap(final def mapping, final Map<String, String> types) {
  final Map<String, Object> lflvMap = [:]
  if (!mapping) {
    return lflvMap
  }

  types.each { final String lvCode, final String lvType ->
    final def lflvForLv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == lvCode
    }

    if (lflvForLv && lflvForLv[lvType]) {
      lflvMap[(lvCode)] = lflvForLv[lvType]
    }
  }
  return lflvMap
}


