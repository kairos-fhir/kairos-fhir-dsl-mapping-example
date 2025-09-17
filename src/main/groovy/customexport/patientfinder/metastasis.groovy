package customexport.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.Multilingual
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.metastasis

/**
 * Represented by a HDRP Metastasis
 * @author Mike Wähnert
 * @since v.1.52.0
 * @since HDRP.v.2025.3.0
 */
observation {

  id = "Observation/Metastasis-" + context.source[metastasis().id()]

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://hl7.org/fhir/observation-category"
      code = "laboratory"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "21907-1"
    }
  }

  subject {
    reference = "Patient/" + context.source[metastasis().patientContainer().id()]
  }

  if (!isFakeEpisode(context.source[metastasis().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[metastasis().episode().id()]
    }
  }

  if (context.source[metastasis().date()]) {
    effectiveDateTime {
      date = context.source[metastasis().date()]
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }

  if (context.source[metastasis().localisationCodeDict()]) {
    bodySite {
      coding {
        system = "cosd:localisation"
        code = (context.source[metastasis().localisationCodeDict().code()] as String).toUpperCase()
        display = context.source[metastasis().localisationCodeDict().multilinguals()].find { final def ml ->
          ml[Multilingual.LANGUAGE] == "de" && ml[Multilingual.SHORT_NAME] != null
        }?.getAt(Multilingual.SHORT_NAME) as String
      }
    }
  }

  if (context.source[metastasis().tumour()]) {
    focus {
      reference = "Condition/" + context.source[metastasis().tumour().centraxxDiagnosis().id()]
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
