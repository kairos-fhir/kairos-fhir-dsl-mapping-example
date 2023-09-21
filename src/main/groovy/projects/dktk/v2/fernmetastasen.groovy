package projects.dktk.v2

import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.metastasis

/**
 * Represented by a CXX Metastasis
 * Specified by https://simplifier.net/oncology/fernmetastasen-duplicate-2
 * hints:
 *  Reference to focus condition has been added additionally, because a reverse reference is not possible yet.
 *
 * @author Mike Wähnert
 * @since CXX.v.3.18.1.21, CXX.v.3.18.2
 */
observation {

  id = "Observation/Metastasis-" + context.source[metastasis().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-Fernmetastasen"
  }

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

  effectiveDateTime {
    date = normalizeDate(context.source[metastasis().date()] as String)
  }

  valueCodeableConcept {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/JNUCS"
      code = "J" // if a metastasis exists, this code is always J
    }
  }

  if (context.source[metastasis().localisationCodeDict()]) {
    bodySite {
      coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/FMLokalisationCS"
        code = (context.source[metastasis().localisationCodeDict().code()] as String).toUpperCase()
      }
    }
  }

  if (context.source[metastasis().tumour()] && hasRelevantCode(context.source[metastasis().tumour().centraxxDiagnosis().diagnosisCode()] as String)) {
    focus {
      reference = "Condition/" + context.source[metastasis().tumour().centraxxDiagnosis().id()]
    }
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

static boolean hasRelevantCode(final String icdCode) {
  return icdCode != null && (icdCode.toUpperCase().startsWith('C') || icdCode.toUpperCase().startsWith('D'))
}
