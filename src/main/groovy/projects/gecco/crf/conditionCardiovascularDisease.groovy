package projects.gecco.crf


import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

//import javax.xml.catalog.Catalog

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/cardiovasculardiseases
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * NOTE: Due to the Cardinality-restraint (1..1) for "code", multiple selections in CXX for this parameter
 *       will be added as additional codings.
 */


condition {
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemCardio = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_HERZKREISLAUF" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemCardio[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "CardiovascularDisease/" + context.source[studyVisitItem().crf().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/cardiovascular-diseases"
    }

    extension {
      url = "https://simplifier.net/forschungsnetzcovid-19/uncertaintyofpresence"
      valueCodeableConcept {
        coding {
          system = "http://snomed.info/sct"
          code = "261665006"
        }
      }
    }
    category {
      coding {
        system = "http://snomed.info/sct"
        code = "722414000"
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    code {
      crfItemCardio[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = matchResponseToICD(item[CatalogEntry.CODE] as String)
        if (ICDcode) {
          coding {
            system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
            version = "2020"
            code = ICDcode
          }
        }
      }
      crfItemCardio[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = matchResponseToSNOMED(item[CatalogEntry.CODE] as String)
        if (SNOMEDcode) {
          coding {
            system = "http://snomed.info/sct"
            code = SNOMEDcode
          }
        }
      }
    }

    recordedDate {
      recordedDate = crfItemCardio[CrfItem.CREATIONDATE]
    }
  }
}


static String matchResponseToICD(final String resp) {
  switch (resp) {
    case ("COV_BLUTHOCHDRUCK"):
      return "I10.90"
    case ("COV_ZUSTAND_N_HERZINFARKT"):
      return "I25.29"
    case ("COV_HERZRHYTHMUSSTOERUNGEN"):
      return "I49.9"
    case ("COV_HERZINSUFFIZIENZ"):
      return "I50.9"
    case ("COV_PAVK"):
      return "I73.9"
    case ("COV_CARO"):
      return "I65.2"
    case ("COV_UNBEKANNT"):
      return "Unknown"
    default: null
  }
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_BLUTHOCHDRUCK"):
      return "38341003"
    case ("COV_ZUSTAND_N_HERZINFARKT"):
      return "22298006"
    case ("COV_HERZRHYTHMUSSTOERUNGEN"):
      return "698247007"
    case ("COV_HERZINSUFFIZIENZ"):
      return "84114007"
    case ("COV_PAVK"):
      return "399957001"
    case ("COV_REVASKULARISATION"):
      return "81266008"
    case ("COV_KHK"):
      return "53741008"
    case ("COV_CARO"):
      return "64586002"
    case ("COV_UNBEKANNT"):
      return "261665006"
    default: null
  }
}