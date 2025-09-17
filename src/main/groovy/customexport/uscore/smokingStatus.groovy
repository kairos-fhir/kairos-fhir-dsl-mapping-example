package customexport.uscore

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ID
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.CATALOG_ENTRY_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a HDRP LaborMapping for the US Core Smoking Status .
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-smokingstatus.html
 *
 * hints:
 * - Observation are specified by LOINC codes.
 * The LaborMapping must therefore use a custom catalog with the required SNOMED CT Codes.
 * The HDRP Master data that must be defined in HDRP for this script to work is provided
 * in /xml/smokingStatus.xml. The file can be imported over the xml import interface.
 *
 * @author Jonas Küttner
 * @since v.1.52.0, HDRP.v.2025.3.0
 */

final def lang = "de"

observation {
  if ("US_CORE_SMOKING_STATUS" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-smokingstatus")
  }

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "social-history"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "72166-2"
    }
  }

  final def relatedPatient = context.source[laborMapping().relatedPatient()]

  if (relatedPatient) {
    subject {
      reference = "Patient/" + relatedPatient[ID]
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  final def lflv = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == "US_CORE_SMOKING_STATUS" }

  final def catalogEntries = lflv[CATALOG_ENTRY_VALUE]
  catalogEntries.each { final def entry ->
    valueCodeableConcept {
      coding {
        system = "http://snomed.info/sct"
        code = entry[CODE] as String
        display = entry[CatalogEntry.MULTILINGUALS].find { final def ml ->
          ml[Multilingual.LANGUAGE] == lang && ml[Multilingual.SHORT_NAME] != null
        }?.getAt(Multilingual.SHORT_NAME) as String
      }
    }
  }
}


