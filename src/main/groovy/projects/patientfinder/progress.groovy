package projects.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Episode
import org.hl7.fhir.r4.model.ClinicalImpression

import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ID
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.progress

/**
 * Represented by a CXX Progress
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, missing References added since CXX.v.3.17.2
 */
clinicalImpression {

  id = "ClinicalImpression/" + context.source[progress().id()]

  status = ClinicalImpression.ClinicalImpressionStatus.COMPLETED

  subject {
    reference = "Patient/" + context.source[progress().patientContainer().id()]
  }

  if (!isFakeEpisode(context.source[progress().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[progress().episode().id()]
    }
  }

  if (context.source[progress().examinationDate()]) {
    effectiveDateTime {
      date = context.source[progress().examinationDate()]
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }

  if (context.source[progress().tumour()]) {
    problem {
      reference = "Condition/" + context.source[progress().tumour().centraxxDiagnosis().id()]
    }
  }

  //Reference Metastasis
  context.source[progress().metastases()]?.each { final def m ->
    finding {
      itemReference {
        reference = "Observation/Metastasis-" + m[ID]
      }
    }
  }

  context.source[progress().histologies()]?.each { final def h ->
    finding {
      itemReference {
        reference = "Observation/Histology-" + h[ID]
      }
    }
  }

  context.source[progress().tumour().tnms()]?.each { final def tnm ->
    finding {
      itemReference {
        reference = "Observation/Tnm-" + tnm[ID]
      }
    }
  }

  // duplicated references, because of problem reference to condition and reference condition -> sample
  context.source[progress().tumour().centraxxDiagnosis().samples()]?.each { final def s ->
    finding {
      itemReference {
        reference = "Specimen/" + s[ID]
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
