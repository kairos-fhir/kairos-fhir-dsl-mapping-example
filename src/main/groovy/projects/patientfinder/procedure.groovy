package projects.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.enums.ProcedureStatus
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medProcedure
/**
 * Represented by CXX MedProcedure
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
        display = context.source[medProcedure().userDefinedCatalogEntry().nameMultilingualEntries()]?.find { it[LANG] == "en" }?.getAt(VALUE)
      }
    }

    if (context.source[medProcedure().procedureCode()]) {
      coding {
        code = context.source[medProcedure().procedureCode()] as String
        display = context.source[medProcedure().procedureText()] as String
      }
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

  if (!isFakeEpisode(context.source[medProcedure().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[medProcedure().episode().id()]
    }
  }
}

static boolean isFakeEpisode(final def episode) {
  if (episode == null) {
    return true
  }

  if (["SACT", "COSD"].contains(episode[Episode.ENTITY_SOURCE])) {
    return true
  }

  final def fakeId = episode[Episode.ID_CONTAINER]?.find { (it[PSN] as String).toUpperCase().startsWith("FAKE") }
  return fakeId != null
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static Procedure.ProcedureStatus mapStatus(final ProcedureStatus procedureStatus){
  if (procedureStatus.equals(ProcedureStatus.COMPLETED)){
    return Procedure.ProcedureStatus.COMPLETED
  }
  if (procedureStatus.equals(ProcedureStatus.PREPARATION)){
    return Procedure.ProcedureStatus.PREPARATION
  }
  if (procedureStatus.equals(ProcedureStatus.IN_PROGRESS)){
    return Procedure.ProcedureStatus.INPROGRESS
  }
  if (procedureStatus.equals(ProcedureStatus.NOT_DONE)){
    return Procedure.ProcedureStatus.NOTDONE
  }
  if (procedureStatus.equals(ProcedureStatus.ON_HOLD)){
    return Procedure.ProcedureStatus.ONHOLD
  }
  if (procedureStatus.equals(ProcedureStatus.COMPLETED)){
    return Procedure.ProcedureStatus.COMPLETED
  }
  if (procedureStatus.equals(ProcedureStatus.ENTERED_IN_ERROR)){
    return Procedure.ProcedureStatus.ENTEREDINERROR
  }
  if (procedureStatus.equals(ProcedureStatus.UNKNOWN)){
    return Procedure.ProcedureStatus.UNKNOWN
  }
  return Procedure.ProcedureStatus.UNKNOWN
}
