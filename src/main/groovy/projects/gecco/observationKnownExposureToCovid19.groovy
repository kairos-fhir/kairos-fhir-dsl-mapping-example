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

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "KNOWN_EXPOSURE_PROFILE_CODE") {
    return // no export
  }

  id = "KnownExposureToCovid/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/known-exposure"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "social history"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "88636-6"
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


  final def knownExpoLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "KNOWN_EXPOSURE_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (knownExpoLfLv) {
    valueCodeableConcept {
      knownExpoLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
        coding {
          system = "https://www.netzwerk-universitaetsmedizin.de/fhir/ValueSet/known-exposure"
          code = entry[CatalogEntry.CODE] as String
        }
      }
    }
  }
}
