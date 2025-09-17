package customexport.dktk.v2

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.SurgeryComponent
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.RootEntities.surgery

/**
 * Represented by a HDRP Surgery
 * OPS code for surgeries are not available in HDRP
 * @author Mike Wähnert
 * @since v.1.52.0
 * @since HDRP v.2025.3.0
 *
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
        code = context.source[surgery().rClassificationDict().multilinguals()]?.find { final def ml ->
          ml[Multilingual.LANGUAGE] == "en" && ml[Multilingual.SHORT_NAME] != null
        }?.getAt(Multilingual.SHORT_NAME) as String
      }
    }
    if (context.source[surgery().rClassificationLocalDict().code()]) {
      coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS"
        version = context.source[surgery().rClassificationLocalDict().version()]
        code = context.source[surgery().rClassificationLocalDict().multilinguals()]?.find { final def ml ->
          ml[Multilingual.LANGUAGE] == "en" && ml[Multilingual.SHORT_NAME] != null
        }?.getAt(Multilingual.SHORT_NAME) as String
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
          display = context.source[surgery().intentionDict().multilinguals()]?.find { final def ml ->
            ml[Multilingual.LANGUAGE] == "en" && ml[Multilingual.SHORT_NAME] != null
          }?.getAt(Multilingual.SHORT_NAME) as String
        }
      }
    }
  }

}

/**
 * removes time zone and time.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}

static boolean hasRelevantCode(final String icdCode) {
  return icdCode != null && (icdCode.toUpperCase().startsWith('C') || icdCode.toUpperCase().startsWith('D'))
}

static String getYear(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 4) : null
}
