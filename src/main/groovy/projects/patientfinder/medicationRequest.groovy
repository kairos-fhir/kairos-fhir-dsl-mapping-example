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
import org.hl7.fhir.r4.model.MedicationRequest

import javax.annotation.Nullable

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

/**
 * Represents a CXX Medication
 *
 * @author Mike Wähnert, Jonas Küttner
 * @since v.1.41.0, CXX.v.2024.2.0
 */

final String DOSAGE_SITE = "dosage.site"
final String DOSAGE_DOSEANDRATE_RATEQUANTITY_UNIT = "dosage.doseAndRate.rateQuantity.unit"
final String VOLUMEINFUSED = "volumeinfused"
final String VOLUMEINFUSED_UOM = "volumeinfused_uom"
final String CONCENTRATION_STRENGTH = "concentration_strength"
final String CONCENTRATION_STRENGTH_UNIT = "concentration_strength_unit"
final String FREQUENCY = "frequency"
final String REQUESTER = "requester"
final String STRENGTHTEXT = "strengthtext"

final Map PROFILE_TYPES = [
    (DOSAGE_SITE)                         : LaborFindingLaborValue.STRING_VALUE,
    (DOSAGE_DOSEANDRATE_RATEQUANTITY_UNIT): LaborFindingLaborValue.STRING_VALUE,
    (VOLUMEINFUSED)                       : LaborFindingLaborValue.NUMERIC_VALUE,
    (VOLUMEINFUSED_UOM)                   : LaborFindingLaborValue.STRING_VALUE,
    (CONCENTRATION_STRENGTH)              : LaborFindingLaborValue.NUMERIC_VALUE,
    (CONCENTRATION_STRENGTH_UNIT)         : LaborFindingLaborValue.STRING_VALUE,
    (FREQUENCY)                           : LaborFindingLaborValue.STRING_VALUE,
    (REQUESTER)                           : LaborFindingLaborValue.MULTI_VALUE_REFERENCES,
    (STRENGTHTEXT)                        : LaborFindingLaborValue.STRING_VALUE
]


medicationRequest {

  if (context.source[medication().serviceType()] != MedicationServiceType.VER.name()) {
    return
  }

  id = "MedicationRequest/" + context.source[medication().id()]

  final def mapping = context.source[medication().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "MedicationRequest_profile"
  }

  final Map<String, Object> lflvMap = getLflvMap(mapping, PROFILE_TYPES)

  identifier {
    value = context.source[medication().fillerOrderNumber()]
  }

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

    if (context.source[medication().observationBegin()] && context.source[medication().observationBegin().date()]) {
      timing {
        event {
          date = context.source[medication().observationBegin().date()]
        }
      }
    }

    if (context.source[medication().observationEnd()] && context.source[medication().observationEnd().date()]) {
      timing {
        event {
          date = context.source[medication().observationEnd().date()]
        }
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


  if (lflvMap.containsKey(REQUESTER)) {
    final def valueRef = lflvMap.get(REQUESTER).find()
    if (valueRef && valueRef[ValueReference.ORGANIZATION_VALUE]) {
      requester {
        reference = "Organization/" + valueRef[ValueReference.ORGANIZATION_VALUE][OrganisationUnit.ID]
      }
    }
  }
}

static Map<String, Object> getLflvMap(final def mapping, final Map<String, String> types) {
  final Map<String, Object> lflvMap = [:]
  if (!mapping) {
    return lflvMap
  }

  types.each { final String lvCode, final String lvType ->
    final def lflvForLv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == lvCode
    }

    if (lflvForLv && lflvForLv[lvType]) {
      lflvMap[(lvCode)] = lflvForLv[lvType]
    }
  }
  return lflvMap
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

@Nullable
static BigDecimal sanitizeScale(final String numeric) {
  try {
    return BigDecimal.valueOf(Double.parseDouble(numeric))
  } catch (final NumberFormatException | NullPointerException ignored) {
    return null
  }
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
