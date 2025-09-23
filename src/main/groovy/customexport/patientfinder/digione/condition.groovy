package customexport.patientfinder.digione


import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.Multilingual.LANGUAGE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a HDRP Diagnosis
 * @author Mike Wähnert
 * @since v.1.43.0, HDRP.v.2024.5.2
 */
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
        display = context.source[diagnosis().icdEntry().preferredLong()]
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
