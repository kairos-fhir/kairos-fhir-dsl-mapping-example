package customexport.patientfinder.digione

import de.kairos.fhir.centraxx.metamodel.RadiationComponent

import static de.kairos.fhir.centraxx.metamodel.RootEntities.radiationTherapy

/**
 * @since HDRP v.2025.3.2 fhir-dsl v.1.52.0
 */
medication {
  id = "Medication/RadiationTherapy-" + context.source[radiationTherapy().id()]

  code {
    coding {
      code = "radiation_comp_" + context.source[radiationTherapy().radiationTherapyId()]
      display = "Radiation Components for RadiationTherapy " + context.source[radiationTherapy().radiationTherapyId()]
    }
  }

  (context.source[radiationTherapy().radiationComponents()] as List)
      .sort { final c -> c[RadiationComponent.ID] }
      .eachWithIndex { final def i,
                       final def radComp ->
        ingredient {
          itemCodeableConcept {
            coding {
              code = "radiation_comp_" + context.source[radiationTherapy().radiationTherapyId()] + "_" + i
              display = "Radiation Component " + i + "for Radiation Therapy" + context.source[radiationTherapy().radiationTherapyId()]
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

