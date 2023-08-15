package projects.dktk.v2

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.surgery

/**
 * Represented by a CXX Surgery
 * OPS code for surgeries are not available in CXX
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
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
      code = "OP" //Operation
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

  if (context.source[surgery().tumour()]) {
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
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

