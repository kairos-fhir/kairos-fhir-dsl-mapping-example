package projects.mii_bielefeld.modul.fall

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.EpisodeIdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.StayType
import org.hl7.fhir.r4.model.Encounter

import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode

/**
 * @author Jonas Küttner
 * @since kairos-fhir-dsl v.1.39.0, CXX v.2024.4.1, v.2024.5.0
 *
 * Requirements:
 * Custom Catalog for Encounter.status codes in CXX featuring the FHIR valueset (http://fhir.de/ValueSet/EncounterClassDE)
 * Catalog for StayType with codes in CXX for FHIR valueset (http://fhir.de/ValueSet/EncounterClassDE)
 */

encounter {

  id = "Encounter/" + context.source[episode().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-fall/StructureDefinition/KontaktGesundheitseinrichtung"
  }

  //Created Episode Id in CXX called visit number
  final def encounterId = context.source[episode().idContainer()]?.find { final idc ->
    "Encounter.id" == idc[EpisodeIdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE]
  }

  if (encounterId) {
    identifier {
      type {
        coding {
          system = "http://terminology.hl7.org/CodeSystem/v2-0203"
          code = "VN"
        }
      }
      value = encounterId[EpisodeIdContainer.PSN]
      system = "urn:centraxx" // Haus-spezifisch
    }
  }

  // the required system needs to be created as CXX MasterData
  final def stayType = context.source[episode().stayType()]
  if (stayType) {
    class_ {
      system = "http://terminology.hl7.org/CodeSystem/v3-ActCode"
      code = stayType[StayType.CODE]
      display = stayType[StayType.MULTILINGUALS].find { final def ml -> ml[Multilingual.LANGUAGE] == "de" }?.getAt(Multilingual.SHORT_NAME)
    }
  }


  subject {
    reference = "Patient/" + context.source[episode().patientContainer().id()]
  }

  period {
    start {
      date = context.source[episode().validFrom()]
    }
    end {
      date = context.source[episode().validUntil()]
    }
  }


  // Data from LaborMapping
  final def mapping = context.source[episode().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "EncounterProfile"
  }

  if (mapping) {
    final def encounterStatus = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == "Encounter.status"
    }

    if (encounterStatus && encounterStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE]) {
      status = encounterStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].find()?.getAt(CatalogEntry.CODE)
    } else {
      status = Encounter.EncounterStatus.UNKNOWN
    }
  }
}

