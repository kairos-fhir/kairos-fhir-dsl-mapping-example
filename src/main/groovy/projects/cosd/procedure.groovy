package projects.cosd

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import org.hl7.fhir.r4.model.Procedure

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
        system = context.source[medProcedure().opsEntry().catalogue().name()]
        version = context.source[medProcedure().opsEntry().catalogue().catalogueVersion()]
        code = context.source[medProcedure().opsEntry().code()] as String
      }
    }

    if (context.source[medProcedure().procedureCode()]) {
      coding {
        code = context.source[medProcedure().procedureCode()] as String
      }
    }
  }

  if (context.source[medProcedure().procedureDate().date()]) {
    performedDateTime {
      date  = context.source[medProcedure().procedureDate().date()]
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

  encounter {
    reference = "Encounter/" + context.source[medProcedure().episode().id()]
  }
}
