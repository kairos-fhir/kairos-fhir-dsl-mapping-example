package projects.dktk.v2

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.radiationTherapy
/**
 * Represented by a CXX RadiationTherapy
 * Specified by https://simplifier.net/oncology/strahlentherapie
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 */
procedure {
  id = "Procedure/RadiationTherapy-" + context.source[radiationTherapy().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Procedure-Strahlentherapie"
  }

  status = Procedure.ProcedureStatus.UNKNOWN

  category {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTTherapieartCS"
      code = "ST"
      display = "Strahlentherapie"
    }
  }

  subject {
    reference = "Patient/" + context.source[radiationTherapy().patientContainer().id()]
  }

  performedPeriod {
    start {
      date = normalizeDate(context.source[radiationTherapy().therapyStart()] as String)
      precision = TemporalPrecisionEnum.DAY.name()
    }
    end {
      date = normalizeDate(context.source[radiationTherapy().therapyEnd()] as String)
      precision = TemporalPrecisionEnum.DAY.name()
    }
  }

  if (context.source[radiationTherapy().tumour()] && hasRelevantCode(context.source[radiationTherapy().tumour().centraxxDiagnosis().diagnosisCode()] as String)) {
    reasonReference {
      reference = "Condition/" + context.source[radiationTherapy().tumour().centraxxDiagnosis().id()]
    }
  }

  if (context.source[radiationTherapy().intentionDict()]) {
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SYSTIntention"
      valueCodeableConcept {
        coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTIntentionCS"
        code = context.source[radiationTherapy().intentionDict()]?.getAt(CODE)?.toString()?.toUpperCase()
        display = context.source[radiationTherapy().intentionDict().nameMultilingualEntries()]?.find { it[LANG] == "de" }?.getAt(VALUE) as String
      }
      }
    }
  }

  if (context.source[radiationTherapy().therapyKindDict()]) {
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-StellungZurOp"
      valueCodeableConcept {
        coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTStellungOPCS"
        code = context.source[radiationTherapy().therapyKindDict()]?.getAt(CODE)?.toString()?.toUpperCase()
        display = context.source[radiationTherapy().therapyKindDict().nameMultilingualEntries()]?.find { it[LANG] == "de" }?.getAt(VALUE) as String
      }
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
