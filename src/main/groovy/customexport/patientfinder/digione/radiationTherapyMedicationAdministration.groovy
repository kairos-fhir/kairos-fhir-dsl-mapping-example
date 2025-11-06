package customexport.patientfinder.digione

import de.kairos.fhir.centraxx.metamodel.RadiationComponent

import static de.kairos.fhir.centraxx.metamodel.RootEntities.radiationTherapy

/**
 * @since HDRP v.2025.3.2 fhir-dsl v.1.52.0
 */
medicationAdministration {
  id = "MedicationAdministration/RadiationTherapy-" + context.source[radiationTherapy().id()]

  identifier {
    value = "radiation_therapy_" + context.source[radiationTherapy().radiationTherapyId()]
  }

  //TODO: read out the therapy type

  medication {
    medicationCodeableConcept {
      coding {
        code = "radiation_therapy_" + context.source[radiationTherapy().radiationTherapyId()]
        display = "RadiationTherapy " + context.source[radiationTherapy().radiationTherapyId()]
      }
    }
  }

  subject {
    reference = "Patient/" + context.source[radiationTherapy().patientContainer().id()]
  }

  effectivePeriod {
    start = context.source[radiationTherapy().therapyStart()]
    end = context.source[radiationTherapy().therapyEnd()]
  }

  final def radComp = context.source[radiationTherapy().radiationComponents()].find {}
  if (radComp != null) {
    dosage {
      dose {
        value = radComp[RadiationComponent.COMPLETE_DOSE]
        unit = "Gy"
      }
    }
  }

}