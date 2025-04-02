package projects.patientfinder.digione

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.OrganisationUnit
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.ValueReference

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.Multilingual.LANGUAGE
import static de.kairos.fhir.centraxx.metamodel.Multilingual.NAME
import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Diagnosis
 * @author Jonas Küttner
 * @since v.1.43.0, CXX.v.2024.5.2
 */

final String SPECIALISM = "specialism"
final String ONSET_PERIOD_END = "onsetPeriod.start"
final String ONSET_PERIOD_START = "onsetPeriod.start"
final String CCISCORE = "CCISCORE"
final String ECOG = "ECOG"


final Map PROFILE_TYPES = [

    (SPECIALISM)        : LaborFindingLaborValue.MULTI_VALUE_REFERENCES,
    (ONSET_PERIOD_END)  : LaborFindingLaborValue.DATE_VALUE,
    (ONSET_PERIOD_START): LaborFindingLaborValue.DATE_VALUE,
    (CCISCORE)          : LaborFindingLaborValue.NUMERIC_VALUE,
    (ECOG)              : LaborFindingLaborValue.NUMERIC_VALUE
]


condition {

  id = "Condition/" + context.source[diagnosis().id()]

  final Map<String, Object> lflvMap = getLflvMap(context.source[diagnosis().laborMappings()] as List, PROFILE_TYPES)

  subject {
    reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
  }

  if (context.source[diagnosis().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[diagnosis().episode().id()]
    }
  }

  recordedDate {
    date = context.source[diagnosis().creationDate()]
  }

  final def diagnosisId = context.source[diagnosis().diagnosisId()]
  if (diagnosisId) {
    identifier {
      value = context.source[diagnosis().diagnosisId()]
    }
  }

  code {
    if (context.source[diagnosis().icdEntry()]) {
      coding {
        system = "https://fhir.centraxx.de/system/" + context.source[diagnosis().icdEntry().catalogue().name()]
        code = context.source[diagnosis().icdEntry().code()] as String
        version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()]
        display = context.source[diagnosis().icdEntry().preferredLong()]
      }
    }

    if (context.source[diagnosis().userDefinedCatalogEntry()]) {
      coding {
        system = "https://fhir.centraxx.de/system/" + context.source[diagnosis().userDefinedCatalogEntry().catalog().code()]
        version = context.source[diagnosis().userDefinedCatalogEntry().catalog().version()]
        code = context.source[diagnosis().userDefinedCatalogEntry().code()] as String
        display = context.source[diagnosis().userDefinedCatalogEntry().multilinguals()]
            ?.find { it[LANGUAGE] == "en" }?.getAt(NAME)
      }
    }

    if (context.source[diagnosis().diagnosisCode()]) {
      coding {
        code = context.source[diagnosis().diagnosisCode()] as String
        display = context.source[diagnosis().diagnosisText()]
      }
    }
  }

  final String diagNote = context.source[diagnosis().comments()] as String
  if (diagNote) {
    note {
      text = diagNote
    }
  }

  onsetDateTime {
    date = context.source[diagnosis().diagnosisDate().date()]
    precision = TemporalPrecisionEnum.DAY.toString()
  }

}

static Map<String, Object> getLflvMap(final List mappings, final Map<String, String> types) {

  final Map<String, Object> lflvMap = [:]

  mappings.each { final def mapping ->
    types.each { final String lvCode, final String lvType ->
      final def lflvForLv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
        lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == lvCode
      }

      if (lflvForLv && lflvForLv[lvType]) {
        lflvMap[(lvCode)] = lflvForLv[lvType]
      }
    }
  }
  return lflvMap

}
