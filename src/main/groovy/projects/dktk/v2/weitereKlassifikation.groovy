package projects.dktk.v2

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Episode
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.otherClassification
import static de.kairos.fhir.centraxx.metamodel.RootEntities.progress
import static de.kairos.fhir.centraxx.metamodel.RootEntities.progress
import static de.kairos.fhir.centraxx.metamodel.RootEntities.progress

/**
 * Represented by a CXX OtherClassification
 * Specified by https://simplifier.net/oncology/weitereklassifikation
 *
 * @author Mike Wähnert
 * @since CXX.v.3.18.3.18, CXX.v.2023.2.0, kairos-fhir-dsl-1.21.0
 */
observation {

  id = "Observation/OtherClassification-" + context.source[otherClassification().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-WeitereKlassifikation"
  }

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      system = "http://loinc.org"
      code = "LP248771-0"
    }
  }

  subject {
    reference = "Patient/" + context.source[otherClassification().patientContainer().id()]
  }

  if (context.source[progress().tumour()] && hasRelevantCode(context.source[progress().tumour().centraxxDiagnosis().diagnosisCode()] as String)) {
    focus {
      reference = "Condition/" + context.source[otherClassification().tumour().centraxxDiagnosis().id()]
    }
  }

  if (context.source[otherClassification().date()]) {
    effectiveDateTime {
      date = context.source[otherClassification().date()]
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }

  category {
    coding {
      system = "https://fhir.centraxx.de/system/otherClassification/classificationName"
      code = context.source[otherClassification().classificationName()] as String
    }
  }

  if (context.source[otherClassification().stadium()]) {
    valueCodeableConcept {
      coding {
        system = "https://fhir.centraxx.de/system/otherClassification/stadium"
        code = context.source[otherClassification().stadium()] as String
        display = context.source[otherClassification().stadium()] as String
      }
    }
  }

  if (context.source[otherClassification().description()]) {
    note {
      text = context.source[otherClassification().description()] as String
    }
  }

  if (context.source[otherClassification().comments()]) {
    note {
      text = context.source[otherClassification().comments()] as String
    }
  }
}

static boolean hasRelevantCode(final String icdCode) {
  return icdCode != null && (icdCode.toUpperCase().startsWith('C') || icdCode.toUpperCase().startsWith('D'))
}
