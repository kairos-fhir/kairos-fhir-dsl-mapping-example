package customexport.patientfinder.digione.mmci

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.Multilingual.LANGUAGE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis
import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode

/**
 * Represented by a HDRP Diagnosis
 * @author Mike Wähnert
 * @since v.1.43.0, HDRP.v.2024.5.2
 */

final String ECOG_INDEX = "ecog_index"


final Map PROFILE_TYPES = [
    (ECOG_INDEX)    : LaborFindingLaborValue.STRING_VALUE
]

condition {

  id = "Condition/" + context.source[diagnosis().id()]

  subject {
    reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
  }

  if (!isFakeEpisode(context.source[diagnosis().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[diagnosis().episode().id()]
    }
  }

  recordedDate {
    date = context.source[diagnosis().creationDate()]
  }

  if (context.source[diagnosis().diagnosisDate()] != null && context.source[diagnosis().diagnosisDate().date()] != null){
    onsetDateTime {
      date = context.source[diagnosis().diagnosisDate().date()]
    }
  }

  final def diagnosisId = context.source[diagnosis().diagnosisId()]
  if (diagnosisId) {
    identifier {
      value = diagnosisId
    }
  }

  code {
    if (context.source[diagnosis().icdEntry()]) {
      coding {
        system = "https://fhir.centraxx.de/system/" + context.source[diagnosis().icdEntry().catalogue().name()]
        code = context.source[diagnosis().icdEntry().code()] as String
        version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()]
        display = truncate(context.source[diagnosis().icdEntry().preferred()] as String)
      }
    } else if (context.source[diagnosis().userDefinedCatalogEntry()]) {
      coding {
        system = "https://fhir.centraxx.de/system/" + context.source[diagnosis().userDefinedCatalogEntry().catalog().code()]
        version = context.source[diagnosis().userDefinedCatalogEntry().catalog().version()]
        code = context.source[diagnosis().userDefinedCatalogEntry().code()] as String
        display = context.source[diagnosis().userDefinedCatalogEntry().multilinguals()]
            ?.find { it[LANGUAGE] == "en" && it[Multilingual.SHORT_NAME] != null }?.getAt(Multilingual.SHORT_NAME)
      }
    } else if (context.source[diagnosis().diagnosisCode()]) {
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

  final def mapping = context.source[episode().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == "ECOG"
  }

  final Map<String, Object> lflvMap = getLflvMap(mapping, PROFILE_TYPES)

  if (lflvMap.containsKey(ECOG_INDEX)) {
    clinicalStatus {
      coding {
        code = lflvMap.get(ECOG_INDEX) as String
      }
    }
  }
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


static String truncate(final String desc){
  if (desc == null){
    null
  }
  return desc.substring(0, Math.min(500, desc.length()))
}

static Map<String, Object> getLflvMap(final def mapping, final Map<String, String> types) {
  final Map<String, Object> lflvMap = [:]
  if (!mapping) {
    return lflvMap
  }

  types.each { final String lvCode, final String lvType ->
    final def lflvForLv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == lvCode
    }

    if (lflvForLv && lflvForLv[lvType]) {
      lflvMap[(lvCode)] = lflvForLv[lvType]
    }
  }
  return lflvMap
}