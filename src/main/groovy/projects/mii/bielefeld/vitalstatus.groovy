package projects.mii.bielefeld

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * represented by a CXX patient
 * @author Jonas Küttner
 * @since v.1.40.0, CXX.v.2024.4.2
 *
 * Requirements:
 * CXX Custom Catalog MiiVitalstatus with code featured in Valueset "https://www.medizininformatik-initiative.de/fhir/core/modul-person/CodeSystem/Vitalstatus"
 * CXX MeasurementProfile for called "MiiVitalstatus" with parameters:
 * Vitalstatus.valueCodeableConcept.coding.code (SingleSelection from Vitalstatus catalog)
 */

observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "MiiVitalstatus") {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    source = "urn:centraxx"
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Vitalstatus"
  }

  // fixed by profile
  status = Observation.ObservationStatus.FINAL

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "survey"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "67162-8"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  final lflvVS = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find { final def lflv ->
    lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == "Vitalstatus.valueCodeableConcept.coding.code"
  }

  // set if documented else "X" for unknown
  valueCodeableConcept {
    coding {
      system = "https://www.medizininformatik-initiative.de/fhir/core/modul-person/CodeSystem/Vitalstatus"
      code = (lflvVS && lflvVS[LaborFindingLaborValue.CATALOG_ENTRY_VALUE]) ?
          lflvVS[LaborFindingLaborValue.CATALOG_ENTRY_VALUE][CatalogEntry.CODE].find() as String :
          "X"
    }
  }
}


