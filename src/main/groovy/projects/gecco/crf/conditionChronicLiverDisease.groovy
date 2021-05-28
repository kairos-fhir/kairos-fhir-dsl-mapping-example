package projects.gecco.crf


import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

//import javax.xml.catalog.Catalog

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/chronicliverdiseases
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
  final def crfItemLiver = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_LEBERERKRANKUNG" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemLiver[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "ChronicLiverDisease/" + context.source[studyVisitItem().crf().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-liver-diseases"
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
        code = "408472002"
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    code {
      crfItemLiver[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = matchResponseToICD(item[CatalogEntry.CODE] as String)
        if (ICDcode) {
          coding {
            system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
            version = "2020"
            code = ICDcode
          }
        }
      }
      crfItemLiver[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
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
      date = normalizeDate(crfItemLiver[CrfItem.CREATIONDATE] as String)
    }
  }
}


static String matchResponseToICD(final String resp) {
  switch (resp) {
    case ("COV_FETTLEBER"):
      return "K76.0"
    case ("COV_LEBERZIRRHOSE"):
      return "K74.6"
    case ("COV_CHRO_HEPATITIS"):
      return "B18.9"
    case ("COV_AUTO_LEBER"):
      return null
    case ("COV_UNBEKANNT"):
      return "Unknown"
    default: null
  }
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_FETTLEBER"):
      return "197321007"
    case ("COV_LEBERZIRRHOSE"):
      return "19943007"
    case ("COV_CHRO_HEPATITIS"):
      return "10295004"
    case ("COV_AUTO_LEBER"):
      return "235890007"
    case ("COV_UNBEKANNT"):
      return "261665006"
    default: null
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}