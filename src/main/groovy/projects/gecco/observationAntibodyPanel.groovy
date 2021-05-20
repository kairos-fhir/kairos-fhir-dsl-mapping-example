package projects.gecco


import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since CXX.v.3.18.0*
 * Maps the following profile:
 *  - SARS-CoV-2 (COVID-19) Ab panel - Serum or Plasma by Immunoassay
 */


observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "COVID_IMMUNOASSAY_PROFILE_CODE") {
    return // no export
  }

  final def ID = context.source[laborMapping().laborFinding().id()]
  id = "AbPanel/" + ID
  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sars-cov-2-ab-pnl-ser-pl-ia"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://loinc.org"
      code = "26436-6"
    }
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "laboratory"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "94504-8"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }
  final def episodeID = context.source[laborMapping().episode().id()]
  if (episodeID) {
    encounter {
      reference = "Episode/" + episodeID
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }


  //If the measurement profile contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
  final def antibodyAnnotation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "ANNOTATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (antibodyAnnotation) {
    note {
      text = antibodyAnnotation[LaborFindingLaborValue.STRING_VALUE]
    }
  }

  if (context.source[laborMapping().mappingType()] == LaborMappingType.SAMPLELABORMAPPING as String) {
    specimen {
      reference = "Specimen/" + context.source[laborMapping().relatedOid()]
    }
  }


  //References to sub-profiles of the panel
  final def abPresence = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_AB_PRESENCE_BY_IMMUNOASSAY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (abPresence) {
    hasMember {
      reference = "AbPresence/" + ID
    }
  }
  final def abConcentration = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_AB_CONCENTRATION_IMMUNOASSAY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (abConcentration) {
    hasMember {
      reference = "AbConcentration/" + ID
    }
  }
  final def IgAabPresence = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_IGA_AB_PRESENCE_IMMUNOASSAY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (IgAabPresence) {
    hasMember {
      reference = "AbIgAPresence/" + ID
    }
  }
  final def IgAabConcentration = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_IGA_AB_CONCENTRATION_IMMUNOASSAY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (IgAabConcentration) {
    hasMember {
      reference = "AbIgAConcentration/" + ID
    }
  }
  final def IgGabPresence = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_IGG_AB_PRESENCE_IMMUNOASSAY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (IgGabPresence) {
    hasMember {
      reference = "AbIgGPresence/" + ID
    }
  }
  final def IgGabConcentration = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_IGG_AB_CONCENTRATION_IMMUNOASSAY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (IgGabConcentration) {
    hasMember {
      reference = "AbIgGConcentration/" + ID
    }
  }
  final def IgMabPresence = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_IGM_AB_PRESENCE_IMMUNOASSAY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (IgMabPresence) {
    hasMember {
      reference = "AbIgMPresence/" + ID
    }
  }
  final def IgMabConcentration = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_IGM_AB_CONCENTRATION_IMMUNOASSAY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (IgMabConcentration) {
    hasMember {
      reference = "AbIgMConcentration/" + ID
    }
  }

}
