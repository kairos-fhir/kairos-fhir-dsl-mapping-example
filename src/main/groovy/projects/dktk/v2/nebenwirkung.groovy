package projects.dktk.v2

import de.kairos.fhir.centraxx.metamodel.RadiationTherapy
import de.kairos.fhir.centraxx.metamodel.SystemTherapy

import static de.kairos.fhir.centraxx.metamodel.RootEntities.adverseEffects

/**
 * Represented by CXX AdverseEffect
 * @since CXX.v.3.18.3.21, CXX.v.2024.2.5, CXX.v.2024.3.0, FHIR-DSL-v.1.33.0
 */
adverseEvent {

  id = "AdverseEvent/" + context.source[adverseEffects().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-AdverseEvent-Nebenwirkung"
  }

  subject {
    reference = "Patient/" + context.source[adverseEffects().patientContainer().id()]
  }

  date {
    date = context.source[adverseEffects().date()]
  }

  recordedDate {
    date = context.source[adverseEffects().creationDate()]
  }

  def therapy = context.source[adverseEffects().therapy()]
  if (therapy != null) {
    event {
      if (isRadiationTherapy(therapy)) {
        coding {
          system = "http://hl7.org/fhir/us/ctcae/ValueSet/ctcae-term-value-set" //TODO:
          code = "ST_Nebenwirkung"
        }
      }
      if (isSystemTherapy(therapy)) {
        coding {
          system = "http://hl7.org/fhir/us/ctcae/ValueSet/ctcae-term-value-set"
          code = "SYST_Nebenwirkung"
        }
      }
    }
  }

  severity {
    coding {
      code = context.source[adverseEffects().code()]
    }
  }

  if (therapy != null) {
    if (isRadiationTherapy(therapy)) {
      suspectEntity {
        instance {
          reference = "Procedure/RadiationTherapy-" + context.source[adverseEffects().therapy().id()]
        }
      }
    }
    if (isSystemTherapy(therapy)) {
      suspectEntity {
        instance {
          reference = "MedicationStatement/SystemTherapy-" + context.source[adverseEffects().therapy().id()]
        }
      }
    }
  }
}

private static boolean isSystemTherapy(final def therapy) {
  return therapy[SystemTherapy.CXX_SYSTEM_THERAPY_ID] != null
}

private static boolean isRadiationTherapy(final def therapy) {
  return therapy[RadiationTherapy.CXX_RADIATION_THERAPY_ID] != null
}
