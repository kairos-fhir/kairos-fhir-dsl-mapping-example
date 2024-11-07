package projects.patientfinder

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.OrganisationUnit
import de.kairos.fhir.centraxx.metamodel.ValueReference
import de.kairos.fhir.centraxx.metamodel.enums.MedicationKind
import de.kairos.fhir.centraxx.metamodel.enums.MedicationServiceType
import org.apache.commons.lang3.StringUtils
import org.hl7.fhir.r4.model.MedicationRequest

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

/**
 * Represents a CXX Medication
 *
 * @author Mike Wähnert, Jonas Küttner
 * @since v.1.41.0, CXX.v.2024.2.0
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

  medicationReference {
    reference = "Medication/" + context.source[medication().id()]
  }

  if (!isFakeEpisode(context.source[medication().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[medication().episode().id()]
    }
  }

  authoredOn {
    date = context.source[medication().transcriptionDate()]
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


    if (context.source[medication().dosis()] != null || context.source[medication().quantity()] != null) {
      doseAndRate {

        doseQuantity {
          value = sanitizeScale(context.source[medication().dosis()] as String)
          unit = context.source[medication().unit().code()]
        }

        rateQuantity {
          value = sanitizeScale(context.source[medication().quantity()] as String)
        }
      }
    }

    if (context.source[medication().observationBegin()] && context.source[medication().observationBegin().date()])
    timing {
      event {
        date = context.source[medication().observationBegin().date()]
      }
    }

    asNeededBoolean = createAsNeededFromType(context.source[medication().resultStatus()] as String)

    route {
      coding {
        system = FhirUrls.System.Medication.APPLICATION_FORM
        code = context.source[medication().applicationForm()]
      }
    }
  }

  final def mapping = context.source[medication().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "MedicationRequest_profile"
  }

  if (mapping) {
    final def lflvSpecialism = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == "requester"
    }

    if (lflvSpecialism) {
      final def valueRef = lflvSpecialism[LaborFindingLaborValue.MULTI_VALUE_REFERENCES].find()
      if (valueRef && valueRef[ValueReference.ORGANIZATION_VALUE]) {
        requester {
          reference = "Organization/" + valueRef[ValueReference.ORGANIZATION_VALUE][OrganisationUnit.ID]
        }
      }
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
  return numeric == null || !StringUtils.isNumeric(numeric) ? null : new BigDecimal(numeric).stripTrailingZeros()
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
