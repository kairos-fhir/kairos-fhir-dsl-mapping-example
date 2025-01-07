package projects.mii.bielefeld

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.Localization
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.CrfTemplateField.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.LaborFinding.LABOR_FINDING_LABOR_VALUES
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.LaborMapping.LABOR_FINDING
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.DATE_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medProcedure

/**
 * Represented by CXX MedProcedure
 * Specified by https://simplifier.net/medizininformatikinitiative-modulprozeduren/prozedur v.2.0.0
 * @author Jonas Küttner
 * @since v.1.40.0, CXX.v.2024.5.0, CXX.v.2024.4.2
 * Requirements:
 * Custom Catalog for Procedure.status codes in CXX featuring the FHIR valueset (http://hl7.org/fhir/ValueSet/event-status)
 * CXX MeasurementProfile for called "ProcedureProfile" with parameters:
 * "Procedure.performedPeriod.end" (Date)
 * "Procedure.status" (SingleSelection from Procedure.status catalog)
 */

procedure {
  id = "Procedure/" + context.source[medProcedure().id()]

  meta {
    source = "urn:centraxx"
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-prozedur/StructureDefinition/Procedure"
  }

  // Data from LaborMapping
  final def mapping = context.source[medProcedure().laborMappings()].find { final def lm ->
    lm[LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "ProcedureProfile"
  }

  if (context.source[medProcedure().opsEntry()]) {
    code {
      coding {
        system = "http://fhir.de/CodeSystem/bfarm/ops"
        version = context.source[medProcedure().opsEntry().catalogue().catalogueVersion()] as String
        code = context.source[medProcedure().opsEntry().code()] as String
      }
    }

    // if ops is used, a snomed-ct category has to be given, statically putting code for surgical procedure here for now
    category {
      coding {
        system = "http://snomed.info/sct"
        code = "387713003"
      }
    }
  }

  subject {
    reference = "Patient/" + context.source[medProcedure().patientContainer().id()]
  }

  // set EPA.procedureDate is mandatory
  // set end date if given in mapping
  performedPeriod {
    start = context.source[medProcedure().procedureDate().date()]
    if (mapping) {
      final def performedPeriodEnd = mapping[LABOR_FINDING][LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
        lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][LaborValue.CODE] == "Procedure.performedPeriod.end"
      }

      if (performedPeriodEnd && performedPeriodEnd[DATE_VALUE]) {
        end = performedPeriodEnd[DATE_VALUE][PrecisionDate.DATE]
      }
    }
  }

  if (mapping) {
    final def procedureStatus = mapping[LABOR_FINDING][LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][LaborValue.CODE] == "Procedure.status"
    }

    if (procedureStatus && procedureStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE]) {
      status = procedureStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].find()?.getAt(CatalogEntry.CODE)
    } else {
      status = Procedure.ProcedureStatus.UNKNOWN
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

static String mapCategory(String opsCode) {
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

static String mapLocalisation(Localization cxxLocalization) {
  switch (cxxLocalization) {
    case Localization.LEFT: return "L"
    case Localization.RIGHT: return "R"
    case Localization.BOTH: return "B"
    default: return null
  }
}

