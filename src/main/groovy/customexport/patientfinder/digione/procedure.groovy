package customexport.patientfinder.digione

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.enums.ProcedureStatus
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.Multilingual.LANGUAGE
import static de.kairos.fhir.centraxx.metamodel.Multilingual.NAME
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medProcedure

/**
 * Represented by HDRP MedProcedure
 * @since v.1.43.0, HDRP.v.2024.5.2
 */
procedure {
  id = "Procedure/" + context.source[medProcedure().id()]


  status = mapStatus(context.source[medProcedure().status()] as ProcedureStatus)

  code {
    if (context.source[medProcedure().opsEntry()]) {
      coding {
        system = "https://fhir.centraxx.de/system/" + context.source[medProcedure().opsEntry().catalogue().name()]
        version = context.source[medProcedure().opsEntry().catalogue().catalogueVersion()]
        code = context.source[medProcedure().opsEntry().code()] as String
        display = context.source[medProcedure().opsEntry().preferredLong()] as String
      }
    }

    if (context.source[medProcedure().userDefinedCatalogEntry()]) {
      coding {
        system = "https://fhir.centraxx.de/system/" + context.source[medProcedure().userDefinedCatalogEntry().catalog().code()]
        version = context.source[medProcedure().userDefinedCatalogEntry().catalog().version()]
        code = context.source[medProcedure().userDefinedCatalogEntry().code()] as String
        display = context.source[medProcedure().userDefinedCatalogEntry().multilinguals()]?.find { it[LANGUAGE] == "en" }?.getAt(NAME)
      }
    }

    if (context.source[medProcedure().procedureCode()]) {
      coding {
        code = context.source[medProcedure().procedureCode()] as String
        display = context.source[medProcedure().procedureText()] as String
      }
    }
  }

  if (context.source[medProcedure().parent()]) {
    partOf {
      reference = "Procedure/" + context.source[medProcedure().parent().id()]
    }
  }

  if (context.source[medProcedure().procedureDate().date()]) {
    performedDateTime {
      date = normalizeDate(context.source[medProcedure().procedureDate().date()] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }

  final String opsNote = context.source[medProcedure().comments()] as String
  if (opsNote) {
    note {
      text = opsNote
    }
  }

  if ("Surgical Procedure".equalsIgnoreCase(opsNote)) {
    category {
      coding {
        system = "http://hl7.org/fhir/ValueSet/procedure-category"
        code = "387713003"
        display = "Surgical procedure (procedure)"
      }
    }
  }

  subject {
    reference = "Patient/" + context.source[medProcedure().patientContainer().id()]
  }


  encounter {
    reference = "Encounter/" + context.source[medProcedure().episode().id()]
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

static Procedure.ProcedureStatus mapStatus(final ProcedureStatus procedureStatus) {
  if (procedureStatus.equals(ProcedureStatus.COMPLETED)) {
    return Procedure.ProcedureStatus.COMPLETED
  }
  if (procedureStatus.equals(ProcedureStatus.PREPARATION)) {
    return Procedure.ProcedureStatus.PREPARATION
  }
  if (procedureStatus.equals(ProcedureStatus.IN_PROGRESS)) {
    return Procedure.ProcedureStatus.INPROGRESS
  }
  if (procedureStatus.equals(ProcedureStatus.NOT_DONE)) {
    return Procedure.ProcedureStatus.NOTDONE
  }
  if (procedureStatus.equals(ProcedureStatus.ON_HOLD)) {
    return Procedure.ProcedureStatus.ONHOLD
  }
  if (procedureStatus.equals(ProcedureStatus.COMPLETED)) {
    return Procedure.ProcedureStatus.COMPLETED
  }
  if (procedureStatus.equals(ProcedureStatus.ENTERED_IN_ERROR)) {
    return Procedure.ProcedureStatus.ENTEREDINERROR
  }
  if (procedureStatus.equals(ProcedureStatus.UNKNOWN)) {
    return Procedure.ProcedureStatus.UNKNOWN
  }
  return Procedure.ProcedureStatus.UNKNOWN
}

static Map<String, Object> getLflvMap(final def mapping, final Map<String, String> types) {
  final Map<String, Object> lflvMap = [:]
  if (!mapping) {
    return lflvMap
  }

  types.each { final String lvCode, final String lvType ->
    final def lflvForLv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == lvCode
    }

    if (lflvForLv && lflvForLv[lvType]) {
      lflvMap[(lvCode)] = lflvForLv[lvType]
    }
  }
  return lflvMap
}
