package projects.gecco


import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueInteger
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since v.1.7.0, CXX.v.3.17.2
 */
observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "BODYMEASUREMENT_CODE") {
    return // no export
  }

  id = "BodyHeight/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-height"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "vital-signs"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "8302-2"
    }
    coding {
      system = "http://snomed.info/sct"
      code = "248334005"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }
  encounter {
    reference = "Episode/" + context.source[laborMapping().episode().id()]
  }


  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }


  final def bodyHeightLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "BODYHEIGHT_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (bodyHeightLfLv) {
    valueQuantity {
      value = bodyHeightLfLv[LaborFindingLaborValue.NUMERIC_VALUE]
      unit = "cm"
      system = "http://unitsofmeasure.org"
      code = "cm"
    }
  }

  //If the measurement profile contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
  final def bodyHeightAnnotation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "ANNOTATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (bodyHeightAnnotation) {
    note {
      text = bodyHeightAnnotation[LaborFindingLaborValue.STRING_VALUE]
    }
  }

  if (bodyHeightLfLv) {
    referenceRange {
      low {
        value = bodyHeightLfLv[LaborFindingLaborValue.LABOR_VALUE][LaborValueInteger.LOWER_VALUE]
      }
      high {
        value = bodyHeightLfLv[LaborFindingLaborValue.LABOR_VALUE][LaborValueInteger.UPPER_VALUE]
      }
    }
  }

}



