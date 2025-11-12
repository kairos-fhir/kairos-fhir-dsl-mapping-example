package customexport.patientfinder.fnusa

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.PatientTransfer
import org.hl7.fhir.r4.model.Encounter

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractCodeSyncIdMultilingual.MULTILINGUALS
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.IdContainerType.DECISIVE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode

/**
 * Represents a HDRP Episode.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-encounter.html
 *
 * hints:
 * - Mapping uses SNOMED-CT concepts.
 * - There is no participant, reasonCode/reference, hospitalization, location in HDRP
 *
 *
 * @author Mike Wähnert
 * @since v.1.13.0, HDRP.v.2023.3.0
 */

final String DISCHARGE_DISPOSITION = "dischargeDisposition"
final String REASON_CODE = "reasonCode"

final Map PROFILE_TYPES = [
    (DISCHARGE_DISPOSITION): LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (REASON_CODE)          : LaborFindingLaborValue.CATALOG_ENTRY_VALUE
]

encounter {

  if (isFakeEpisode(context.source)) {
    return //filters Encounters with EncounterOID prefix FAKE_
  }

  id = "Encounter/" + context.source[episode().id()]

  final def mapping = context.source[episode().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == "Encounter_profile"
  }

  final Map<String, Object> lflvMap = getLflvMap(mapping, PROFILE_TYPES)

  meta {
    profile "http://hl7.org/fhir/us/core/StructureDefinition/us-core-encounter"
  }

  context.source[episode().idContainer()].each { final idContainer ->
    final boolean isDecisive = idContainer[ID_CONTAINER_TYPE]?.getAt(DECISIVE)
    if (isDecisive) {
      identifier {
        value = idContainer[PSN]
        type {
          coding {
            system = FhirUrls.System.IdContainerType.BASE_URL
            code = idContainer[ID_CONTAINER_TYPE]?.getAt(CODE)
          }
        }
      }
    }
  }

  status = Encounter.EncounterStatus.UNKNOWN

  if (context.source[episode().stayType().code()]) {
    class_ {
      system = FhirUrls.System.Episode.StayType.BASE_URL
      code = context.source[episode().stayType().code()]
    }
  }

  type {
    coding {
      system = "http://snomed.info/sct"
      code = "308335008"
      display = "Patient encounter procedure"
    }
  }

  subject {
    reference = "Patient/" + context.source[episode().patientContainer().id()]
  }

  period {

    if (context.source[episode().validFrom()]) {
      start {
        date = context.source[episode().validFrom()]
        precision = TemporalPrecisionEnum.DAY.toString()
      }
    }

    if (context.source[episode().validUntil()]) {
      end {
        date = context.source[episode().validUntil()]
        precision = TemporalPrecisionEnum.DAY.toString()
      }
    }

  }

  if (context.source[episode().habitation()]) {
    serviceProvider {
      reference = "Organization/" + context.source[episode().habitation().id()]
    }
  }

  for (final pt in context.source[episode().patientTransfers()]) {
    location {
      location {
        reference = "Location/PT-" + pt[PatientTransfer.ID]
      }
    }
  }

  if (lflvMap.containsKey(REASON_CODE)) {
    reasonCode {
      lflvMap.get(REASON_CODE).each { final def entry ->
        coding {
          code = entry[CODE] as String
          display = entry[MULTILINGUALS].find { final def ml ->
            ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
          }?.getAt(Multilingual.SHORT_NAME)
        }
      }
    }
  }

  if (lflvMap.containsKey(DISCHARGE_DISPOSITION)) {
    hospitalization {
      dischargeDisposition {
        lflvMap.get(DISCHARGE_DISPOSITION).each { final def entry ->
          coding {
            code = entry[CODE] as String
            display = entry[MULTILINGUALS].find { final def ml ->
              ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
            }?.getAt(Multilingual.SHORT_NAME)
          }
        }
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
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == lvCode
    }

    if (lflvForLv && lflvForLv[lvType]) {
      lflvMap[(lvCode)] = lflvForLv[lvType]
    }
  }
  return lflvMap
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
