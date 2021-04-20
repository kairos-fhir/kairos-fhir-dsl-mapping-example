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

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "VITALSIGNS_CODE") {
    return // no export
  }

  id = "BodyTemperature/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-temperature"
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
      code = "8310-5"
    }
    coding {
      system = "http://snomed.info/sct"
      code = "386725007"
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


  final def bodyTempLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "BODYTEMPERATURE_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (bodyTempLfLv) {
    valueQuantity {
      value = bodyTempLfLv[LaborFindingLaborValue.NUMERIC_VALUE]
      unit = "Cel"
      system = "http://unitsofmeasure.org"
      code = "Cel"
    }
  }

  //If the measurement profile contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
  final def bodyTempAnnotation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "ANNOTATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (bodyTempAnnotation) {
    note {
      text = bodyTempAnnotation[LaborFindingLaborValue.STRING_VALUE]
    }
  }

  if (bodyTempLfLv) {
    referenceRange {
      low {
        value = bodyTempLfLv[LaborFindingLaborValue.LABOR_VALUE][LaborValueInteger.LOWER_VALUE]
      }
      high {
        value = bodyTempLfLv[LaborFindingLaborValue.LABOR_VALUE][LaborValueInteger.UPPER_VALUE]
      }
    }
  }

}



