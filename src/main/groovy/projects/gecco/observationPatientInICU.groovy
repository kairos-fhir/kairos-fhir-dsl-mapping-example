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

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "PATIENT_IN_ICU_PROFILE_CODE") {
    return // no export
  }

  id = "PatientInICU/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/patient-in-icu"
  }

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      system = "http://loinc.org"
      code = "95420-6"
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

  final def icuLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "PATIENT_IN_ICU_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (icuLfLv) {
    valueCodeableConcept {
      icuLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
        coding {
          system = "http://snomed.info/sct"
          code = entry[CatalogEntry.CODE] as String
        }
      }
    }
  }
}
