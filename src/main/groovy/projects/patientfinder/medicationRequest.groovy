package projects.patientfinder

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.enums.FhirDoseTypeEnum
import de.kairos.fhir.centraxx.metamodel.enums.MedicationKind
import de.kairos.fhir.centraxx.metamodel.enums.MedicationServiceType
import org.hl7.fhir.r4.model.MedicationRequest

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

/**
 * Represents a CXX Medication
 *
 * @author Mike Wähnert
 * @since v.1.26.0, CXX.v.2023.5.0
 */
medicationRequest {

  if (context.source[medication().serviceType()] != MedicationServiceType.VER.name()) {
    return
  }

  id = "MedicationRequest/" + context.source[medication().id()]

  status = MedicationRequest.MedicationRequestStatus.COMPLETED

  subject {
    reference = "Patient/" + context.source[medication().patientContainer().id()]
  }

  if (!isFakeEpisode(context.source[medication().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[medication().episode().id()]
    }
  }

  medicationCodeableConcept {
    coding {
      system = FhirUrls.System.Medication.BASE_URL
      code = context.source[medication().code()] as String
      display = context.source[medication().name()] as String
    }

    if (context.source[medication().agent()]) {
      coding {
        system = FhirUrls.System.Medication.AGENT
        code = context.source[medication().agent()] as String
      }
    }
    if (context.source[medication().agentGroup()]) {
      coding {
        system = FhirUrls.System.Medication.AGENT_GROUP
        code = context.source[medication().agentGroup()] as String
      }
    }
    if (context.source[medication().methodOfApplication()]) {
      coding {
        system = FhirUrls.System.Medication.APPLICATION_METHOD
        code = context.source[medication().methodOfApplication()] as String
      }
    }
  }

  authoredOn {
    date = context.source[medication().transcriptionDate()]
  }

  requester {
    display = context.source[medication().prescribedBy()]
  }

  if (context.source[medication().attendingDoctor()]) {
    performer {
      reference = "Practitioner/" + context.source[medication().attendingDoctor().id()]
    }
  }

  dosageInstruction {
    text = context.source[medication().dosisSchema()] as String
    additionalInstruction {
      text = context.source[medication().ordinanceReleaseForm()] as String
    }

    // is dose
    if (context.source[medication().isDose()]) {
      doseAndRate {
        type {
          coding {
            system = FhirUrls.System.Medication.DOSE_TYPE
            code = FhirDoseTypeEnum.IS.name()
          }
        }
        extension {
          url = FhirUrls.Extension.Medication.DOSE_VALUE
          valueString = context.source[medication().isDose()]
        }
      }
    }

    // target dose
    if (context.source[medication().trgDose()]) {
      doseAndRate {
        type {
          coding {
            system = FhirUrls.System.Medication.DOSE_TYPE
            code = FhirDoseTypeEnum.TRG.name()
          }
        }
        extension {
          url = FhirUrls.Extension.Medication.DOSE_VALUE
          valueString = context.source[medication().trgDose()]
        }
      }
    }

    //deviation dose
    if (context.source[medication().deviationDose()]) {
      doseAndRate {
        type {
          coding {
            system = FhirUrls.System.Medication.DOSE_TYPE
            code = FhirDoseTypeEnum.DEV.name()
          }
        }
        extension {
          url = FhirUrls.Extension.Medication.DOSE_VALUE
          valueString = context.source[medication().deviationDose()]
        }
      }
    }

    if (context.source[medication().dosis()] != null || context.source[medication().quantity()] != null) {
      doseAndRate {
        type {
          coding {
            system = FhirUrls.System.Medication.DOSE_TYPE
            code = FhirDoseTypeEnum.PRESCRIPTION.name()
          }
        }

        extension {
          url = FhirUrls.Extension.Medication.DOSE_VALUE
          valueString = context.source[medication().dosis()]
        }

        doseQuantity {
          value = sanitizeScale(context.source[medication().dosis()] as String)
          unit = context.source[medication().unit().code()]
        }

        rateQuantity {
          value = sanitizeScale(context.source[medication().quantity()] as String)
        }
      }
    }

    timing {
      event {
        date = context.source[medication().trgDate()]
      }
    }

    asNeededBoolean = createAsNeededFromType(context.source[medication().resultStatus()] as String)

    route {
      coding {
        system = FhirUrls.System.Medication.APPLICATION_FORM
        code = context.source[medication().applicationForm()]
      }
    }

    method {
      coding {
        system = FhirUrls.System.Medication.APPLICATION_MEDIUM
        code = context.source[medication().applicationMedium()]
      }
    }
  }

  reasonCode {
    text = context.source[medication().notes()] as String
  }

  if (context.source[medication().fillerOrderNumber()]) {
    extension {
      url = FhirUrls.Extension.Medication.FON
      valueString = context.source[medication().fillerOrderNumber()]
    }
  }

  if (context.source[medication().placerOrderNumber()]) {
    extension {
      url = FhirUrls.Extension.Medication.PON
      valueString = context.source[medication().placerOrderNumber()]
    }
  }

  if (context.source[medication().serviceType()]) {
    extension {
      url = FhirUrls.Extension.Medication.TYPE
      valueCoding {
        system = FhirUrls.System.Medication.ServiceType.BASE_URL
        code = context.source[medication().serviceType()]
      }
    }
  }

  if (context.source[medication().ordinanceReleaseMethod()]) {
    extension {
      url = FhirUrls.Extension.Medication.ORDINANCE_RELEASE_METHOD
      valueString = context.source[medication().ordinanceReleaseMethod()]
    }
  }

  if (context.source[medication().transcriptionist()]) {
    extension {
      url = FhirUrls.Extension.Medication.TRANSCRIPTIONIST
      valueString = context.source[medication().transcriptionist()]
    }
  }

  if (context.source[medication().prescribedBy()]) {
    extension {
      url = FhirUrls.Extension.Medication.PRESCRIBER
      valueString = context.source[medication().prescribedBy()]
    }
  }

  if (context.source[medication().prescription()]) {
    extension {
      url = FhirUrls.Extension.Medication.IS_PRESCRIPTION
      valueBoolean = context.source[medication().prescription()]
    }
  }

  if (context.source[medication().resultDate()]) {
    extension {
      url = FhirUrls.Extension.Medication.RESULTDATE
      valueBoolean = context.source[medication().resultDate()]
    }
  }

}

static Boolean createAsNeededFromType(final String resultStatus) {
  if (MedicationKind.BM.name() == resultStatus) {
    return true
  } else if (MedicationKind.EM.name() == resultStatus) {
    return false
  } else {
    return null
  }
}

static BigDecimal sanitizeScale(final String numeric) {
  return numeric == null ? null : new BigDecimal(numeric).stripTrailingZeros()
}

static boolean isFakeEpisode(final def episode) {
  if (episode == null) {
    return true
  }

  if (["SACT", "COSD"].contains(episode[Episode.ENTITY_SOURCE])) {
    return true
  }

  final def fakeId = episode[Episode.ID_CONTAINER]?.find { (it[PSN] as String).toUpperCase().startsWith("FAKE") }
  return fakeId != null
}
