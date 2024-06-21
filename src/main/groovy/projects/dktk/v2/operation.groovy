package projects.dktk.v2

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Complications
import de.kairos.fhir.centraxx.metamodel.SurgeryComponent
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.surgery

/**
 * Represented by a CXX Surgery
 * OPS code for surgeries are not available in CXX
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 * @since CXX.v.3.18.3.17, CXX.v.3.18.4,CXX.v.2023.3.9, CXX.v.2023.4.2, CXX.v.2023.5.1, CXX.v.2023.6.0: SurgeryComponent export is available since
 * @since CXX.v.3.18.3.21, CXX.v.2024.2.5, CXX.v.2024.3.0, FHIR-DSL-v.1.33.0 complication evidence export available
 */
procedure {
  id = "Procedure/Surgery-" + context.source[surgery().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Procedure-Operation"
  }

  status = Procedure.ProcedureStatus.UNKNOWN

  category {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTTherapieartCS"
      code = "OP"
      display = "Operation"
    }
  }

  code {
    context.source[surgery().surgeryComponents()]?.each { final surgeryComponent ->
      if (surgeryComponent[SurgeryComponent.CODE]) {
        coding {
          code = surgeryComponent[SurgeryComponent.CODE] as String
          system = "http://fhir.de/CodeSystem/bfarm/ops"
          if (surgeryComponent[SurgeryComponent.DATE]) {
            version = getYear(surgeryComponent[SurgeryComponent.DATE] as String)
          }
        }
      }
    }
  }

  subject {
    reference = "Patient/" + context.source[surgery().patientContainer().id()]
  }

  performedDateTime {
    if (context.source[surgery().therapyStart()]) {
      date = normalizeDate(context.source[surgery().therapyStart()] as String)
    } else if (context.source[surgery().therapyEnd()]) {
      date = normalizeDate(context.source[surgery().therapyEnd()] as String)
    }
    precision = TemporalPrecisionEnum.DAY.name()
  }

  if (context.source[surgery().tumour()] && hasRelevantCode(context.source[surgery().tumour().centraxxDiagnosis().diagnosisCode()] as String)) {
    reasonReference {
      reference = "Condition/" + context.source[surgery().tumour().centraxxDiagnosis().id()]
    }
  }

  outcome {
    if (context.source[surgery().rClassificationDict()]) {
      coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS"
        version = context.source[surgery().rClassificationDict().version()]
        code = context.source[surgery().rClassificationDict().nameMultilingualEntries()]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
      }
    }
    if (context.source[surgery().rClassificationLocalDict().code()]) {
      coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS"
        version = context.source[surgery().rClassificationLocalDict().version()]
        code = context.source[surgery().rClassificationLocalDict().nameMultilingualEntries()]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
      }
    }
  }

  if (context.source[surgery().intentionDict()]) {
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-OPIntention"
      valueCodeableConcept {
        coding {
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/OPIntentionCS"
          code = (context.source[surgery().intentionDict().code()] as String).toUpperCase()
          display = context.source[surgery().intentionDict().nameMultilingualEntries()]?.find { it[LANG] == "de" }?.getAt(VALUE) as String
        }
      }
    }
  }

  context.source[surgery().complications()]?.each { final cxxComp ->
    def complicationCode = cxxComp[Complications.CODE]
    complication {
      coding {
        system = getComplicationsSystem(complicationCode as String)
        code = complicationCode as String
      }
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

static String getYear(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 4) : null
}

static String getComplicationsSystem(final String code) {
  if (code == null) {
    return null
  }
  else if (["N", "U", "ABD", "ABS", "ASF", "ANI", "AEP", "ALR", "ANS", "AEE", "API", "BIF", "BOG", "BOE", "BSI", "CHI", "DAI", "DPS", "DIC", "DEP",
       "DLU", "DSI", "ENF", "GER", "HEM", "HUR", "HAE", "HFI", "HNK", "HZI", "HRS", "HNA", "HOP", "HYB", "HYF", "IFV", "KAS", "KES", "KIM",
       "KRA", "KDS", "LEV", "LOE", "LYF", "LYE", "MES", "MIL", "MED", "MAT", "MYI", "RNB", "NAB", "NIN", "OES", "OSM", "PAF", "PIT", "PAB",
       "PPA", "PAV", "PER", "PLB", "PEY", "PLE", "PMN", "PNT", "PDA", "PAE", "RPA", "RIN", "SKI", "SES", "SFH", "STK", "TZP", "TIA", "TRZ",
       "WUH", "WSS",].contains(code.toUpperCase())) {
    return "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/OPKomplikationenCS"
  } else {
    return "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
  }
}
