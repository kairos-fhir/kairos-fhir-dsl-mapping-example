package projects.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Episode
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis
import static de.kairos.fhir.centraxx.metamodel.RootEntities.histology

/**
 * Represented by a CXX Histology
 * @author Mike Wähnert
 * @since CXX.v.3.18.1.21, CXX.v.3.18.2
 */
observation {
  id = "Observation/Histology-" + context.source[histology().id()]

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
      code = "59847-4"
    }
  }

  subject {
    reference = "Patient/" + context.source[histology().patientContainer().id()]
  }

  if (!isFakeEpisode(context.source[histology().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[histology().episode().id()]
    }
  }

  if (context.source[histology().date()]) {
    effectiveDateTime {
      date = context.source[histology().date()]
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }

  if (context.source[histology().icdEntry()]) {
    valueCodeableConcept {
      coding {
        system = context.source[diagnosis().icdEntry().catalogue().name()]
        version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()]
        code = context.source[histology().icdEntry().code()] as String
      }
    }
  }

  if (context.source[histology().tumour()]) {
    focus {
      reference = "Condition/" + context.source[histology().tumour().centraxxDiagnosis().id()]
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
