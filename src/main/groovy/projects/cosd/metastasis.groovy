package projects.cosd

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.metastasis
/**
 * Represented by a CXX Metastasis
 * @author Mike Wähnert
 * @since CXX.v.3.18.1.21, CXX.v.3.18.2
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

  if (context.source[metastasis().episode()]) {
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
        display = context.source[metastasis().localisationCodeDict().nameMultilingualEntries()].find { final def me ->
          me[LANG] == "en"
        }?.getAt(VALUE) as String
      }
    }
  }

  if (context.source[metastasis().tumour()]) {
    focus {
      reference = "Condition/" + context.source[metastasis().tumour().centraxxDiagnosis().id()]
    }
  }
}
