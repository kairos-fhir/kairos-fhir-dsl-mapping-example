package projects.uscore

import de.kairos.fhir.centraxx.metamodel.*
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.RootEntities.medProcedure
/**
 * Represents a CXX MedProcedure.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-procedure.html
 *
 * CentraXX MedProcedures are coded by either an OPS or a CustomCatalog code.
 *
 * @author Jonas Küttner
 * @since v.1.13.0, CXX.v.2022.1.0
 *
 * TODO: Implement export of MasterDataEntries
 */
final def lang = "de"

procedure {
  id = "Procedure/" + context.source[medProcedure().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-procedure")
  }

  status = Procedure.ProcedureStatus.UNKNOWN

  final def opsEntry = context.source[medProcedure().opsEntry()]
  final def customCatalogEntry = context.source[medProcedure().userDefinedCatalogEntry()]
  if (opsEntry) {
    code {
      coding {
        system = "http://fhir.de/CodeSystem/dimdi/ops"
        version = opsEntry[OpsEntry.CATALOGUE][OpsCatalog.CATALOGUE_VERSION]
        code = opsEntry[OpsEntry.CODE] as String
        display = opsEntry[OpsEntry.PREFERRED]
      }
    }
  }

  if (customCatalogEntry) {
    code {
      coding {
        system = "urn:centraxx:CodeSystem/CustomCatalog-" + customCatalogEntry[CatalogEntry.CATALOG][AbstractCustomCatalog.ID]
        version = customCatalogEntry[CatalogEntry.CATALOG][AbstractCustomCatalog.VERSION]
        code = customCatalogEntry[CatalogEntry.CODE] as String
        display = customCatalogEntry[CatalogEntry.NAME_MULTILINGUAL_ENTRIES].find {
          final def ml -> ml[MultilingualEntry.LANG] == lang
        }?.getAt(MultilingualEntry.VALUE)
      }
    }
  }

  subject {
    reference = "Patient/" + context.source[medProcedure().patientContainer().id()]
  }

  performedDateTime {
    date = context.source[medProcedure().procedureDate().date()]
  }
}
