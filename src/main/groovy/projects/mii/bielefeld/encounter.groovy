package projects.mii.bielefeld

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
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
import org.hl7.fhir.r4.model.codesystems.DataAbsentReason

import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode
/**
 * @author Jonas Küttner
 * @since kairos-fhir-dsl v.1.39.0, CXX v.2024.4.1, v.2024.5.0
 *
 * Requirements:
 * Custom Catalog for Encounter.status codes in CXX featuring the FHIR valueset (http://fhir.de/ValueSet/EncounterClassDE)
 * Catalog for StayType with codes in CXX for FHIR valueset (http://fhir.de/ValueSet/EncounterClassDE)
 * CXX MeasurementProfile for called "EncounterProfile" with parameters:
 * "Encounter.status" (SingleSelection from Encounter.status catalog)
 */

encounter {

  id = "Encounter/" + context.source[episode().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-fall/StructureDefinition/KontaktGesundheitseinrichtung"
  }

  //Create Identifier for all episode Ids
  context.source[episode().idContainer()]?.each { final def episodeIdContainer ->
    identifier {
      type {
        coding {
          system = "http://terminology.hl7.org/CodeSystem/v2-0203"
          code = "VN"
        }
        coding {
          system = FhirUrls.System.IdContainerType.BASE_URL
          code = episodeIdContainer[EpisodeIdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE] as String
        }
      }
      value = episodeIdContainer[EpisodeIdContainer.PSN]
      system = "https://fhir.centraxx.de/system/idContainer/psn"// Haus-spezifisch // could be coded by the IdContainerType
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

  if (mapping == null){
    status {
      value = Encounter.EncounterStatus.UNKNOWN
      extension {
        url = "http://hl7.org/fhir/StructureDefinition/data-absent-reason"
        valueCode = DataAbsentReason.ASKEDUNKNOWN.toCode()
      }
    }
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

