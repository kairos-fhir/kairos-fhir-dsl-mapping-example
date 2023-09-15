package projects.nhs

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import org.hl7.fhir.r4.model.Procedure

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

  if (!["SACT", "COSD"].contains(context.source[medProcedure().episode().entitySource()])) {
    encounter {
      reference = "Encounter/" + context.source[medProcedure().episode().id()]
    }
  }
}
