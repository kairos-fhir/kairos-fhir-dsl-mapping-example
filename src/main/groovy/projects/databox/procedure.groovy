package projects.databox

import de.kairos.fhir.centraxx.metamodel.enums.Localization
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.RootEntities.medProcedure

/**
 * Represented by CXX MedProcedure
 * @author Marvin Schmidtke
 * TODO: bodysite: is it actually monitored in CXX?
 */
procedure {
  id = "Procedure/" + context.source[medProcedure().id()]

  status = Procedure.ProcedureStatus.UNKNOWN

  final String opsCode = context.source[medProcedure().opsEntry().code()] as String


  code {
    coding {
      system = "urn:centraxx:CodeSystem/OpsCatalog-" + context.source[medProcedure().opsEntry().catalogue().id()]
      version = context.source[medProcedure().opsEntry().catalogue().catalogueVersion()]
      code = context.source[medProcedure().opsEntry().code()] as String
    }
  }


  performedDateTime {
    date = normalizeDate(context.source[medProcedure().procedureDate().date()] as String)
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

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String mapCategory(final String opsCode) {
  final char firstChar = opsCode.charAt(0)
  switch (firstChar) {
    case "1": return "103693007"
    case "3": return "363679005"
    case "5": return "387713003"
    case "6": return "18629005"
    case "8": return "277132007"
    case "9": return "394841004"
    default: return null
  }
}

static String mapLocalisation(final Localization cxxLocalization) {
  switch (cxxLocalization) {
    case Localization.LEFT: return "L"
    case Localization.RIGHT: return "R"
    case Localization.BOTH: return "B"
    default: return null
  }
}

