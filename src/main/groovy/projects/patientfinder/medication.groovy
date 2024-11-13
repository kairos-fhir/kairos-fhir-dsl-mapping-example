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

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

final String DOSAGE_SITE = "dosage.site"
final String DOSAGE_DOSEANDRATE_RATEQUANTITY_UNIT = "dosage.doseAndRate.rateQuantity.unit"
final String VOLUMEINFUSED = "volumeinfused"
final String VOLUMEINFUSED_UOM = "volumeinfused_uom"
final String CONCENTRATION_STRENGTH = "concentration_strength"
final String CONCENTRATION_STRENGTH_UNIT = "concentration_strength_unit"
final String FREQUENCY = "frequency"
final String REQUESTER = "requester"
final String STRENGTHTEXT = "strengthtext"

final Map PROFILE_TYPES = [
    (DOSAGE_SITE)                         : LaborFindingLaborValue.STRING_VALUE,
    (DOSAGE_DOSEANDRATE_RATEQUANTITY_UNIT): LaborFindingLaborValue.STRING_VALUE,
    (VOLUMEINFUSED)                       : LaborFindingLaborValue.NUMERIC_VALUE,
    (VOLUMEINFUSED_UOM)                   : LaborFindingLaborValue.STRING_VALUE,
    (CONCENTRATION_STRENGTH)              : LaborFindingLaborValue.NUMERIC_VALUE,
    (CONCENTRATION_STRENGTH_UNIT)         : LaborFindingLaborValue.STRING_VALUE,
    (FREQUENCY)                           : LaborFindingLaborValue.STRING_VALUE,
    (REQUESTER)                           : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (STRENGTHTEXT)                        : LaborFindingLaborValue.STRING_VALUE
]


/**
 * Represents a CXX MedicationAdministration -> extracts the Medication data
 *
 * @author Jonas Küttner
 * v.1.41.0, CXX.v.2024.4.2
 */
medication {

  id = "Medication/" + context.source[medication().id()]

  final def mapping = getLaborMapping(context)

  // export for administration
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
      if (context.source[medication().agent()]) {
        coding {
          system = FhirUrls.System.Medication.AGENT
          code = context.source[medication().agent()] as String
        }
      }
      if (context.source[medication().agentGroup()]) {
        coding {
          system = FhirUrls.System.Medication.AGENT_GROUP
          code = context.source[medication().agentGroup()] as String
        }
      }
    }

    if (context.source[medication().serviceType()] as MedicationServiceType == MedicationServiceType.GAB) {
      strength {
        if (lflvMap.containsKey(CONCENTRATION_STRENGTH)) {
          numerator {
            value = lflvMap[CONCENTRATION_STRENGTH]
            unit = lflvMap[CONCENTRATION_STRENGTH_UNIT]
          }
        }
      }
    }

    if (context.source[medication().serviceType()] as MedicationServiceType == MedicationServiceType.VER) {
      strength {
        if (lflvMap.containsKey(STRENGTHTEXT)) {
          numerator {
            code = lflvMap[STRENGTHTEXT] as String
          }
        }
      }
    }
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

static def getLaborMapping(final Context context) {
  if (context.source[medication().serviceType()] as MedicationServiceType == MedicationServiceType.GAB) {
    return context.source[medication().laborMappings()].find { final def lm ->
      lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "MedicationAdministration_profile"
    }
  }

  if (context.source[medication().serviceType()] as MedicationServiceType == MedicationServiceType.VER) {
    return context.source[medication().laborMappings()].find { final def lm ->
      lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "MedicationRequest_profile"
    }
  }

  return null
}

