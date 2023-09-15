package projects.nhs

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.otherClassification

/**
 * Represented by a CXX TNM
 * @author Mike Wähnert
 * @since CXX.v.2023.2.0, kairos-fhir-dsl-1.21.0
 */
observation {

  id = "Observation/OtherClassification-" + context.source[otherClassification().id()]

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://hl7.org/fhir/observation-category"
      code = "laboratory"
    }
  }

  subject {
    reference = "Patient/" + context.source[otherClassification().patientContainer().id()]
  }

  if (context.source[otherClassification().episode()] != null && !["SACT", "COSD"].contains(context.source[otherClassification().episode().entitySource()])) {
    encounter {
      reference = "Encounter/" + context.source[otherClassification().episode().id()]
    }
  }

  if (context.source[otherClassification().tumour()]) {
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

  code {
    coding {
      system = "https://fhir.centraxx.de/system/otherClassification/stadium"
      code = context.source[otherClassification().classificationName()] as String
      display = "Stage"
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
