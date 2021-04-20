package projects.gecco


import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueInteger
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since v.1.7.0, CXX.v.3.17.2
 */
observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "BLOODGASPANEL_PROFILE") {
    return // no export
  }

  id = "OxygenSaturation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/oxygen-saturation"
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
      code = "2708-6"
    }
    coding {
      system = "http://snomed.info/sct"
      code = "431314004"
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

  final def oxySatLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "O2SATURATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (oxySatLfLv) {
    valueQuantity {
      value = oxySatLfLv[LaborFindingLaborValue.NUMERIC_VALUE]
      unit = "%"
      system = "http://unitsofmeasure.org"
      code = "%"
    }
  }

  //If the measurement profile contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
  final def pHAnnotation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "ANNOTATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (pHAnnotation) {
    note {
      text = pHAnnotation[LaborFindingLaborValue.STRING_VALUE]
    }
  }


  if (context.source[laborMapping().mappingType()] == LaborMappingType.SAMPLELABORMAPPING as String) {
    specimen {
      reference = "Specimen/" + context.source[laborMapping().relatedOid()]
    }
  }

  if (oxySatLfLv) {
    referenceRange {
      low {
        value = oxySatLfLv[LaborFindingLaborValue.LABOR_VALUE][LaborValueInteger.LOWER_VALUE]
      }
      high {
        value = oxySatLfLv[LaborFindingLaborValue.LABOR_VALUE][LaborValueInteger.UPPER_VALUE]
      }
    }
  }

}



