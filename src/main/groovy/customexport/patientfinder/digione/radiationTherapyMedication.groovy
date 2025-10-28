package customexport.patientfinder.digione

import de.kairos.fhir.centraxx.metamodel.RadiationComponent

import static de.kairos.fhir.centraxx.metamodel.RootEntities.radiationTherapy

/**
 * @since HDRP v.2025.3.2 fhir-dsl v.1.52.0
 */
medication {
  id = "Medication/RadiationTherapy-" + context.source[radiationTherapy().id()]

  context.source[radiationTherapy().radiationComponents()].each { final def radComp ->

    ingredient {
      itemCodeableConcept {
        coding {
          code = "radiation_component"
          display = "radiation component"
        }
      }
      strength {
        numerator {
          value = radComp[RadiationComponent.COMPLETE_DOSE]
        }
      }
    }
  }
}

