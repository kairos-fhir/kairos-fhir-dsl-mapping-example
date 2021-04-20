package projects.gecco


import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since v.1.7.0, CXX.v.3.17.2
 */
observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "SOFA_SCORE_PROFILE_CODE") {
    return // no export
  }

  id = "SofaScore/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sofa-score"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "survey"
    }
  }

  code {
    coding {
      system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes"
      code = "06"
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


  final def SofaScoLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "SOFA_SCORE_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (SofaScoLfLv) {
    valueQuantity {
      value = SofaScoLfLv[LaborFindingLaborValue.NUMERIC_VALUE]
    }
  }


  //If the measurement profile contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
  final def SofaScoAnnotation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "ANNOTATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (SofaScoAnnotation) {
    note {
      text = SofaScoAnnotation[LaborFindingLaborValue.STRING_VALUE] as String
    }
  }


  //Respiration
  final def RespFraScoLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "SOFA_SCORE_RESP_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (RespFraScoLfLv) {
    component {
      code {
        coding {
          system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
          code = "resp"
        }
      }
      valueCodeableConcept {
        RespFraScoLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          coding {
            system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
            code = entry[CatalogEntry.CODE] as String
          }
        }
      }
    }
  }

  //Nervous system
  final def nsFraScoLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "SOFA_SCORE_NS_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (nsFraScoLfLv) {
    component {
      code {
        coding {
          system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
          code = "ns"
        }
      }
      valueCodeableConcept {
        nsFraScoLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          coding {
            system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
            code = entry[CatalogEntry.CODE] as String
          }
        }
      }
    }
  }

  //Cardiovascular
  final def cvsFraScoLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "SOFA_SCORE_CVS_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (cvsFraScoLfLv) {
    component {
      code {
        coding {
          system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
          code = "cvs"
        }
      }
      valueCodeableConcept {
        cvsFraScoLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          coding {
            system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
            code = entry[CatalogEntry.CODE] as String
          }
        }
      }
    }
  }

  //Liver
  final def livFraScoLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "SOFA_SCORE_LIVER_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (livFraScoLfLv) {
    component {
      code {
        coding {
          system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
          code = "liv"
        }
      }
      valueCodeableConcept {
        livFraScoLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          coding {
            system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
            code = entry[CatalogEntry.CODE] as String
          }
        }
      }
    }
  }

  //Coagulation
  final def coaFraScoLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "SOFA_SCORE_COA_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (coaFraScoLfLv) {
    component {
      code {
        coding {
          system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
          code = "coa"
        }
      }
      valueCodeableConcept {
        coaFraScoLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          coding {
            system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
            code = entry[CatalogEntry.CODE] as String
          }
        }
      }
    }
  }

  //Kidney
  final def kidFraScoLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "SOFA_SCORE_KID_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (kidFraScoLfLv) {
    component {
      code {
        coding {
          system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
          code = "kid"
        }
      }
      valueCodeableConcept {
        kidFraScoLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          coding {
            system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/sofa-score"
            code = entry[CatalogEntry.CODE] as String
          }
        }
      }
    }
  }
}
