package customexport.uscore

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.Unity

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.NUMERIC_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a HDRP LaborMapping for the US Core Vital Sign Observation Respiratory Rate.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-respiratory-rate.html
 *
 * hints:
 * - Observation are specified by LOINC codes.
 * - Units are specified by UCUM codes.
 *
 * Note: The mapping requires labor methods, labor values and units defined in HDRP that correspond to the specification of the
 * profile! For more information, see project README.md
 *
 * @author Jonas Küttner
 * @since v.1.52.0, HDRP.v.2025.3.0
 */
observation {
  if ("US_CORE_RESPIRATORY_RATE" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-respiratory-rate")
  }

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "vital-signs"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "9279-1"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  final def laborFindingLaborValue = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == "RESPIRATORY_RATE" }

  valueQuantity {
    value = laborFindingLaborValue[NUMERIC_VALUE]
    unit = laborFindingLaborValue[CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValueNumeric.UNIT][Unity.MULTILINGUALS]
        .find { final def ml ->
          ml[Multilingual.LANGUAGE] == "de" && ml[Multilingual.SHORT_NAME] != null
        }?.getAt(Multilingual.SHORT_NAME) as String
    system = "http://unitsofmeasure.org"
    code = "/min"
  }
}


