package customexport.patientfinder.digione


import static de.kairos.fhir.centraxx.metamodel.RootEntities.radiationTherapy

/**
 * @since HDRP v.2025.3.2 fhir-dsl v.1.52.0
 */
medicationAdministration {
  id = "MedicationAdministration/RadiationTherapy-" + context.source[radiationTherapy().id()]

  identifier {
    value = context.source[radiationTherapy().radiationTherapyId()]
  }

  medication {
    medicationReference {
      reference = "Medication/RadiationTherapy-" + context.source[radiationTherapy().id()]
    }
  }

  subject {
    reference = "Patient/" + context.source[radiationTherapy().patientContainer().id()]
  }


  effectivePeriod {
    start = context.source[radiationTherapy().therapyStart()]
    end = context.source[radiationTherapy().therapyEnd()]
  }

}