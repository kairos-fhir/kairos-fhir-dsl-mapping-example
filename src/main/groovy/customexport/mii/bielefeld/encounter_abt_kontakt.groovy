package customexport.mii.bielefeld

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.StayType
import org.hl7.fhir.r4.model.Encounter

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * @author Jonas Küttner
 * @since kairos-fhir-dsl v.1.39.0, HDRP v.2024.4.1, v.2024.5.0
 *
 * Exports the LaborFinding with LaborMethod code MP_Encounter_Abteilungskontakt
 */

final String STATUS = "Encounter.status"

final String FAS_CODE = "Encounter.serviceType.coding:fachabteilungsschluessel.code"
final String FAS_CODE2 = "Encounter.serviceType.coding:fachabteilungsschluessel.code_2"
final String FAS_CODE3 = "Encounter.serviceType.coding:fachabteilungsschluessel.code_3"

final String KONTAKT_EBENE = "Encounter.type:KontaktEbene"
final String KONTAKT_ART = "Versorgungsstellenkontakt_Encounter.type:KontaktArt"
final String PERIOD_START = "Encounter.period.start"
final String PERIOD_END = "Encounter.period.end"
final String ENCOUNTER_ID = "Encounter_Fachabteilung_KIS_Bezeichnung"


final Map PROFILE_TYPES = [
    (STATUS)       : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,

    (FAS_CODE)     : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (FAS_CODE2)    : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (FAS_CODE3)    : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,

    (KONTAKT_EBENE): LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (KONTAKT_ART)  : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (PERIOD_START) : LaborFindingLaborValue.DATE_VALUE,
    (PERIOD_END)   : LaborFindingLaborValue.DATE_VALUE,
    (ENCOUNTER_ID) : LaborFindingLaborValue.STRING_VALUE
]


encounter {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "MP_Encounter_Abteilungskontakt") {
    return
  }

  final Map<String, Object> lflvMpEncounterAk = getLflvMap(context.source[laborMapping().laborFinding().laborFindingLaborValues()] as List,
      PROFILE_TYPES)


  final def relatedPatient = context.source[laborMapping().relatedPatient()]

  if (context.source[laborMapping().relatedPatient()] == null || context.source[laborMapping().episode()] == null) {
    return
  }

  id = "Encounter/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-fall/StructureDefinition/KontaktGesundheitseinrichtung"
  }

  //Create Identifier for first episode Id
  final def episodeId = lflvMpEncounterAk.get(ENCOUNTER_ID)
  if (episodeId) {
    identifier {
      type {
        coding {
          system = "http://terminology.hl7.org/CodeSystem/v2-0203"
          code = "VN"
        }
      }
      value = episodeId
      system = "https://fhir.centraxx.de/system/idContainer/psn"
    }
  }

  // setting staytype of parent, otherwise validation will fail.
  final def stayType = context.source[laborMapping().episode().stayType()]
  if (stayType) {
    class_ {
      system = "http://terminology.hl7.org/CodeSystem/v3-ActCode"
      code = stayType[CODE]
      display = stayType[StayType.MULTILINGUALS].find { final def ml -> ml[Multilingual.LANGUAGE] == "de" }?.getAt(Multilingual.SHORT_NAME)
    }
  }

  subject {
    reference = "Patient/" + relatedPatient[PatientContainer.ID]
  }

  final def encounterStart = lflvMpEncounterAk.get(PERIOD_START)
  final def encounterEnd = lflvMpEncounterAk.get(PERIOD_END)

  if ((encounterStart && encounterStart[PrecisionDate.DATE]) || (encounterEnd && encounterEnd[PrecisionDate.DATE])) {
    period {
      start = encounterStart[PrecisionDate.DATE]
      end = encounterEnd[PrecisionDate.DATE]
    }
  }

  // MP_Encounter
  final def encounterStatus = lflvMpEncounterAk.get(STATUS)?.find()?.getAt(CODE)

  if (encounterStatus != null) {
    status = encounterStatus
  } else {
    status = Encounter.EncounterStatus.UNKNOWN
  }

  type {
    coding {
      system = "http://fhir.de/CodeSystem/Kontaktebene"
      code = "abteilungskontakt"
    }
  }


  final def kontaktArt = lflvMpEncounterAk.get(KONTAKT_ART)?.find()?.getAt(CODE)

  if (kontaktArt) {
    type {
      coding {
        system = "http://fhir.de/CodeSystem/kontaktart-de"
        code = kontaktArt
      }
    }
  }

  serviceType {
    final def fas_code = lflvMpEncounterAk.get(FAS_CODE)?.find()?.getAt(CODE)
    if (fas_code) {
      coding {
        system = "http://fhir.de/CodeSystem/dkgev/Fachabteilungsschluessel"
        code = fas_code
      }
    }

    final def fas_code2 = lflvMpEncounterAk.get(FAS_CODE2)?.find()?.getAt(CODE)
    final def fas_code3 = lflvMpEncounterAk.get(FAS_CODE2)?.find()?.getAt(CODE)

    if (fas_code) {
      coding {
        system = "http://fhir.de/CodeSystem/dkgev/Fachabteilungsschluessel-erweitert"
        code = fas_code2
      }
    } else if (fas_code3) {
      coding {
        system = "http://fhir.de/CodeSystem/dkgev/Fachabteilungsschluessel-erweitert"
        code = fas_code3
      }
    }
  }


  partOf {
    reference = "Encounter/" + context.source[laborMapping().episode().id()]
  }

}

static Map<String, Object> getLflvMap(final List lflvs, final Map<String, String> types) {
  final Map<String, Object> lflvMap = [:]

  types.each { final String lvCode, final String lvType ->
    final def lflvForLv = lflvs.find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == lvCode
    }

    if (lflvForLv && lflvForLv[lvType]) {
      lflvMap[(lvCode)] = lflvForLv[lvType]
    }
  }
  return lflvMap
}

