package projects.gecco


import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since CXX.v.3.18.0
 */
observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "DISCHARGE_DISPOSITION_PROFILE_CODE") {
    return // no export
  }

  id = "DischargeDisposition/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/discharge-disposition"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "social-history"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "55128-3"
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

  final def intTrialLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "DISCHARGE_DISPOSITION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (intTrialLfLv) {
    valueCodeableConcept {
      intTrialLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
        coding {
          system = "http://snomed.info/sct"
          code = entry[CatalogEntry.CODE] as String
        }
      }
    }
  }
}
