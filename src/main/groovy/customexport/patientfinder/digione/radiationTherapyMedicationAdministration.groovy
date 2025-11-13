package customexport.patientfinder.digione

import de.kairos.fhir.centraxx.metamodel.Multilingual
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

  medication {
    medicationCodeableConcept {
      coding {
        code = context.source[radiationTherapy().therapyKindDict().code()] as String
        display = context.source[radiationTherapy().therapyKindDict().multilinguals()]
            ?.find { final mle -> mle[Multilingual.LANGUAGE] == "en" && mle[Multilingual.SHORT_NAME] != null }
            ?.getAt(Multilingual.SHORT_NAME) as String
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

  final def radComp = context.source[radiationTherapy().radiationComponents()].find({it})
  if (radComp != null) {
    dosage {
      dose {
        value = radComp[RadiationComponent.COMPLETE_DOSE]
        unit = "Gy"
      }
    }
  }

}