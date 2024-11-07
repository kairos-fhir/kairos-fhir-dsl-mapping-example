package projects.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.Unity
import de.kairos.fhir.centraxx.metamodel.enums.DatePrecision
import de.kairos.fhir.centraxx.metamodel.enums.MedicationServiceType
import org.apache.commons.lang3.StringUtils
import org.hl7.fhir.r4.model.MedicationAdministration

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

final String DOSAGE_SITE = "dosage.site"
final String DOSAGE_DOSEANDRATE_RATEQUANTITY_UNIT = "dosage.doseAndRate.rateQuantity.unit"
final String VOLUMEINFUSED = "volumeinfused"
final String VOLUMEINFUSED_UOM = "volumeinfused_uom"
final String CONCENTRATION_STRENGTH = "concentration_strength"
final String CONCENTRATION_STRENGTH_UNIT = "concentration_strength_unit"

final Map PROFILE_TYPES = [
  DOSAGE_SITE : LaborFindingLaborValue.STRING_VALUE,
  DOSAGE_DOSEANDRATE_RATEQUANTITY_UNIT : LaborFindingLaborValue.STRING_VALUE,
  VOLUMEINFUSED : LaborFindingLaborValue.NUMERIC_VALUE,
  VOLUMEINFUSED_UOM : LaborFindingLaborValue.STRING_VALUE,
  CONCENTRATION_STRENGTH : LaborFindingLaborValue.NUMERIC_VALUE,
  CONCENTRATION_STRENGTH_UNIT : LaborFindingLaborValue.STRING_VALUE
]


/**
 * Represents a CXX Medication
 *
 * @author Mike Wähnert
 * @since v.1.41.0, CXX.v.2024.4.1
 */
medicationAdministration {

  id = "MedicationAdministration/" + context.source[medication().id()]

  if (context.source[medication().serviceType()] != MedicationServiceType.GAB.name()) {
    return
  }

  final def mapping = context.source[medication().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "MedicationAdministration_profile"
  }

  final Map<String, Map> lflvMap = getLflvMap(mapping, PROFILE_TYPES)

  status = MedicationAdministration.MedicationAdministrationStatus.COMPLETED

  medicationReference {
    reference = "Medication/" + context.source[medication().id()]
  }

  subject {
    reference = "Patient/" + context.source[medication().patientContainer().id()]
  }

  if (!isFakeEpisode(context.source[medication().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[medication().episode().id()]
    }
  }

  identifier {
    value = context.source[medication().fillerOrderNumber()]
  }

  effectivePeriod {
    if (context.source[medication().observationBegin()] && context.source[medication().observationBegin().date()]){
      start = context.source[medication().observationBegin().date()]
    }

    if (context.source[medication().observationEnd()] && context.source[medication().observationEnd().date()]){
      end = context.source[medication().observationEnd().date()]
    }
  }

  if (context.source[medication().attendingDoctor()]) {
    performer {
      actor {
        reference = "Practitioner/" + context.source[medication().attendingDoctor().id()]
      }
    }
  }

  dosage {
    text = context.source[medication().dosisSchema()] as String


    if (lflvMap.containsKey(DOSAGE_SITE)){
      site {
        text = lflvMap.get(DOSAGE_SITE) as String
      }
    }

    route {
      coding {
        system = FhirUrls.System.Medication.APPLICATION_FORM
        display = context.source[medication().applicationForm()]
      }
    }

    dose {
      value = sanitizeScale(context.source[medication().dosis()] as String)
      unit = context.source[medication().unit()]?.getAt(Unity.CODE)
    }

    rateQuantity {
      value = sanitizeScale(context.source[medication().quantity()] as String)
      if (lflvMap.containsKey(DOSAGE_DOSEANDRATE_RATEQUANTITY_UNIT)){
        unit = lflvMap.get(DOSAGE_DOSEANDRATE_RATEQUANTITY_UNIT) as String
      }
    }
  }

  reasonCode {
    text = context.source[medication().notes()] as String
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

static String convertPrecision(final String cxxPrecision) {
  if (DatePrecision.EXACT.name() == cxxPrecision) {
    return TemporalPrecisionEnum.MILLI.name()
  } else if (DatePrecision.DAY.name() == cxxPrecision) {
    return TemporalPrecisionEnum.DAY.name()
  } else if (DatePrecision.MONTH.name() == cxxPrecision) {
    return TemporalPrecisionEnum.MONTH.name()
  } else if (DatePrecision.YEAR.name() == cxxPrecision) {
    return TemporalPrecisionEnum.YEAR.name()
  } else {
    return DatePrecision.DAY
  }
}

static BigDecimal sanitizeScale(final String numeric) {
  return numeric == null || !StringUtils.isNumeric(numeric) ? null : new BigDecimal(numeric).stripTrailingZeros()
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

static Map<String, Map> getLflvMap(final def mapping, final Map<String, String> types){
  final Map<String, Map> lflvMap = [:]
  if (!mapping) {
    return lflvMap
  }

  types.each { final String lvCode, final String lvType ->
    final def lflv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == lvCode
    }
    if (lflv && lflv[lvType]) {
      lflvMap[lvCode] = [key: lflv[lvType]]
    }
  }
  return lflvMap
}

