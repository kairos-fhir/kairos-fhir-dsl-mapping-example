package customexport.mii.bielefeld

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.EpisodeIdContainer
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.StayType
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.codesystems.DataAbsentReason

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode

/**
 * @author Jonas Küttner
 * @since kairos-fhir-dsl v.1.39.0, HDRP v.2024.4.1, v.2024.5.0
 *
 * Requirements:
 * Custom Catalog for Encounter.status codes in HDRP featuring the FHIR valueset (http://fhir.de/ValueSet/EncounterClassDE)
 * Catalog for StayType with codes in HDRP for FHIR valueset (http://fhir.de/ValueSet/EncounterClassDE)
 * HDRP MeasurementProfile for called "EncounterProfile" with parameters:
 * "Encounter.status" (SingleSelection from Encounter.status catalog)
 */

final String STATUS = "Encounter.status"
final String AUFNAHME_GRUND_12 = "Encounter.extension:Aufnahmegrund.extension:ErsteUndZweiteStelle.value[x]"
final String AUFNAHME_GRUND_3 = "Encounter.extension:Aufnahmegrund.extension:DritteStelle.value[x]"
final String AUFNAHME_GRUND_4 = "Encounter.extension:Aufnahmegrund.extension:VierteStelle.value[x]"
final String ADMIT_SOURCE = "Encounter.hospitalization.admitSource"
final String ENT_GRUND_12 = "Encounter.hospitalization.dischargeDisposition.extension:Entlassungsgrund.extension:ErsteUndZweiteStelle.value[x]"
final String ENT_GRUND_3 = "Encounter.hospitalization.dischargeDisposition.extension:Entlassungsgrund.extension:DritteStelle.value[x]"
final String KONTAKT_EBENE = "Encounter.type:KontaktEbene"
final String KONTAKT_ART = "Versorgungsstellenkontakt_Encounter.type:KontaktArt"


final Map PROFILE_TYPES = [
    (STATUS)           : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (AUFNAHME_GRUND_12): LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (AUFNAHME_GRUND_3) : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (AUFNAHME_GRUND_4) : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (ENT_GRUND_12)     : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (ENT_GRUND_3)      : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (KONTAKT_EBENE)    : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (KONTAKT_ART)      : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (ADMIT_SOURCE)     : LaborFindingLaborValue.CATALOG_ENTRY_VALUE
]


encounter {

  id = "Encounter/" + context.source[episode().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-fall/StructureDefinition/KontaktGesundheitseinrichtung"
  }

  final def mpEncounter = context.source[episode().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == "MP_Encounter"
  }


  final Map<String, Object> lflvMpEncounter = getLflvMap(mpEncounter, PROFILE_TYPES)


  //Create Identifier for first episode Id
  final def firstEpisodeIdContainer = context.source[episode().idContainer()]?.find { true }
  if (firstEpisodeIdContainer) {
    identifier {
      type {
        coding {
          system = "http://terminology.hl7.org/CodeSystem/v2-0203"
          code = "VN"
        }
        coding {
          system = FhirUrls.System.IdContainerType.BASE_URL
          code = firstEpisodeIdContainer[EpisodeIdContainer.ID_CONTAINER_TYPE][CODE] as String
        }
      }
      value = firstEpisodeIdContainer[EpisodeIdContainer.PSN]
      system = "https://fhir.centraxx.de/system/idContainer/psn"
      // Haus-spezifisch // could be coded by the IdContainerType
    }
  }


  // the required system needs to be created as HDRP MasterData
  final def stayType = context.source[episode().stayType()]
  if (stayType) {
    class_ {
      system = "http://terminology.hl7.org/CodeSystem/v3-ActCode"
      code = stayType[CODE]
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
  if (mpEncounter == null) {
    status {
      value = Encounter.EncounterStatus.UNKNOWN
      extension {
        url = "http://hl7.org/fhir/StructureDefinition/data-absent-reason"
        valueCode = DataAbsentReason.ASKEDUNKNOWN.toCode()
      }
    }
  }

  // MP_Encounter
  final def encounterStatus = lflvMpEncounter.get(STATUS)?.find()

  if (encounterStatus) {
    status = encounterStatus[CODE]
  } else {
    status = Encounter.EncounterStatus.UNKNOWN
  }

  final def kontaktEbene = lflvMpEncounter.get(KONTAKT_EBENE)?.find()?.getAt(CODE)

  type {
    coding {
      system = "http://fhir.de/CodeSystem/Kontaktebene"
      code = kontaktEbene != null ? kontaktEbene : "einrichtungskontakt"
    }
  }

  final def kontaktArt = lflvMpEncounter.get(KONTAKT_ART)?.find()?.getAt(CODE)
  if (kontaktArt) {
    type {
      coding {
        system = "http://fhir.de/CodeSystem/kontaktart-de"
        code = kontaktArt
      }
    }
  }


  extension {
    url = "http://fhir.de/StructureDefinition/Aufnahmegrund"

    final def afg_12 = lflvMpEncounter.get(AUFNAHME_GRUND_12)?.find()?.getAt(CODE)
    if (afg_12) {
      extension {
        url = "ErsteUndZweiteStelle"
        valueCoding {
          system = "http://fhir.de/CodeSystem/dkgev/AufnahmegrundErsteUndZweiteStelle"
          code = afg_12
        }
      }
    }

    final def afg_3 = lflvMpEncounter.get(AUFNAHME_GRUND_3)?.find()?.getAt(CODE)
    if (afg_3) {
      extension {
        url = "DritteStelle"
        valueCoding {
          system = "http://fhir.de/CodeSystem/dkgev/AufnahmegrundDritteStelle"
          code = afg_3
        }
      }
    }

    final def afg_4 = lflvMpEncounter.get(AUFNAHME_GRUND_4)?.find()?.getAt(CODE)
    if (afg_4) {
      extension {
        url = "VierteStelle"
        valueCoding {
          system = "http://fhir.de/CodeSystem/dkgev/AufnahmegrundVierteStelle"
          code = afg_4
        }
      }
    }
  }

  hospitalization {
    final def admitSourceValue = lflvMpEncounter.get(ADMIT_SOURCE)?.find()?.getAt(CODE)
    if (admitSource) {
      admitSource {
        coding {
          system = "http://fhir.de/CodeSystem/dgkev/Aufnahmeanlass"
          code = admitSourceValue
        }
      }
    }
  }

  final def eg12 = lflvMpEncounter.get(ENT_GRUND_12)?.find()?.getAt(CODE)
  final def eg3 = lflvMpEncounter.get(ENT_GRUND_3)?.find()?.getAt(CODE)
  final def admitSourceValue = lflvMpEncounter.get(ADMIT_SOURCE)?.find()?.getAt(CODE)


  if ([eg12, eg3, admitSourceValue].any { it != null }) {
    hospitalization {

      if (admitSourceValue) {
        admitSource {
          coding {
            system = "http://fhir.de/CodeSystem/dgkev/Aufnahmeanlass"
            code = admitSourceValue
          }
        }
      }

      if ([eg12, eg3].any { it != null }) {
        dischargeDisposition {
          extension {
            url = "http://fhir.de/StructureDefinition/Entlassungsgrund"

            if (eg12) {
              extension {
                url = "ErsteUndZweiteStelle"
                valueCoding {
                  system = "http://fhir.de/CodeSystem/dkgev/EntlassungsgrundErsteUndZweiteStelle"
                  code = eg12
                }
              }
            }

            if (eg3) {
              extension {
                url = "DritteStelle"
                valueCoding {
                  system = "http://fhir.de/CodeSystem/dkgev/EntlassungsgrundDritteStelle"
                  code = eg3
                }
              }
            }
          }
        }
      }
    }
  }

  if (context.source[episode().parent()] != null) {
    partOf {
      reference = "Encounter/" + context.source[episode().parent().id()]
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

