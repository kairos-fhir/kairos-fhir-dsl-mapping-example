package projects.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Episode
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

  status = Procedure.ProcedureStatus.UNKNOWN

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
      date = context.source[medProcedure().procedureDate().date()]
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }

  final String opsNote = context.source[medProcedure().comments()] as String
  if (opsNote) {
    note {
      text = opsNote
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
