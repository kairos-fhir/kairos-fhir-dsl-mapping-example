package projects.dktk.v2

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.progress

/**
 * Represented by a CXX Progress
 * Specified by https://simplifier.net/oncology/ecog
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.2
 */
observation {

  if (context.source[progress().ecogDict().code()] == null) {
    return
  }

  id = "Observation/Ecog-" + context.source[progress().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-Ecog"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://hl7.org/fhir/observation-category"
      code = "survey"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "89247-1"
    }
  }

  subject {
    reference = "Patient/" + context.source[progress().patientContainer().id()]
  }

  if (context.source[progress().tumour()] && hasRelevantCode(context.source[progress().tumour().centraxxDiagnosis().diagnosisCode()] as String)) {
    focus {
      reference = "Condition/" + context.source[progress().tumour().centraxxDiagnosis().id()]
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source[progress().examinationDate()] as String)
  }

  def dktkEcog = toDktkEcog(context.source[progress().ecogDict().code()] as String)
  valueCodeableConcept {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/EcogCS"
      code = dktkEcog[0]
      version = "0.9.0"
      display = dktkEcog[1]
    }
    coding {
      system = FhirUrls.System.GtdsDict.EcogDictionary.BASE_URL
      code = context.source[progress().ecogDict().code()] as String
      version = context.source[progress().ecogDict().version()] as String
      display = context.source[progress().ecogDict().nameMultilingualEntries()]?.find { it[LANG] == "de" }?.getAt(VALUE)
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

/**
 * 90 – 100 % nach Karnofsky --> 0 nach ECOG
 * 70 – 80 % nach Karnofsky --> 1 nach ECOG
 * 50 – 60 % nach Karnofsky --> 2 nach ECOG
 * 30 – 40 % nach Karnofsky --> 3 nach ECOG
 * 10 – 20 % nach Karnofsky --> 4 nach ECOG
 * 0% nach Karnofsky --> 5 nach ECOG
 */
static String[] toDktkEcog(final String cxxCode) {
  if (["5", "0%"].contains(cxxCode)) return ["5", "Tod (0% nach Karnofsky)"]
  if (["4", "10%", "20%"].contains(cxxCode)) return ["4", "Völlig pflegebedürftig, keinerlei Selbstversorgung möglich; völlig an Bett oder Stuhl gebunden (10 - 20% nach Karnofsky)"]
  if (["3", "30%", "40%"].contains(cxxCode)) return ["3", "Nur begrenzte Selbstversorgung möglich; ist 50% oder mehr der Wachzeit an Bett oder Stuhl gebunden (30 - 40 % nach Karnofsky)"]
  if (["2", "50%", "60%"].contains(cxxCode)) return ["2", "Gehfähig, Selbstversorgung möglich, aber nicht arbeitsfähig; kann mehr als 50% der Wachzeit aufstehen (50 - 60 % nach Karnofsky)"]
  if (["1", "70%", "80%"].contains(cxxCode)) return ["1", "Einschränkung bei körperlicher Anstrengung, aber gehfähig; leichte körperliche Arbeit bzw. Arbeit im Sitzen (z.B. leichte Hausarbeit oder Büroarbeit) möglich (70 - 80 % nach Karnofsky)"]
  if (["0", "90%", "100%"].contains(cxxCode)) return ["0", "Normale, uneingeschränkte Aktivität wie vor der Erkrankung (90 - 100 % nach Karnofsky)"]
  else return ["U", "Unbekannt"]
}
