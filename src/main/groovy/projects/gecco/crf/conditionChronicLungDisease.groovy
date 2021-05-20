package projects.gecco.crf


import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

//import javax.xml.catalog.Catalog

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/chroniclungdiseases
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
  final def crfItemLung = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_LUNGENERKRANKUNG" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemLung[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "ChronicLungDisease/" + context.source[studyVisitItem().crf().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/chronic-lung-diseases"
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
        code = "418112009"
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }


    code {
      crfItemLung[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = matchResponseToICD(item[CatalogEntry.CODE] as String)
        if (ICDcode) {
          coding {
            system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
            version = "2020"
            code = ICDcode
          }
        }
      }
      crfItemLung[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
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
      recordedDate = crfItemLung[CrfItem.CREATIONDATE] as String
    }
  }
}


static String matchResponseToICD(final String resp) {
  switch (resp) {
    case ("COV_ASTHMA"):
      return "J45.9"
    case ("COV_COPD"):
      return "J44.9"
    case ("COV_LUNGENFIBROSE"):
      return "J84.1"
    case ("COV_PUL_HYPERTONII"):
      return "I27.0"
    case ("COV_OHS"):
      return "E66.29"
    case ("COV_SCHLAFAPNOE"):
      return "G47.3"
    case ("COV_OSAS"):
      return "G47.31"
    case ("COV_CYSTISCHE_FIBR"):
      return "E84.9"
    case ("COV_ANDERE"):
      return "J44.9"
    case ("COV_UNBEKANNT"):
      return "Unknown"
    default: null
  }
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_ASTHMA"):
      return "195967001"
    case ("COV_COPD"):
      return "13645005"
    case ("COV_LUNGENFIBROSE"):
      return "51615001"
    case ("COV_PUL_HYPERTONII"):
      return "70995007"
    case ("COV_OHS"):
      return "190966007"
    case ("COV_SCHLAFAPNOE"):
      return "73430006"
    case ("COV_OSAS"):
      return "78275009"
    case ("COV_CYSTISCHE_FIBR"):
      return "190905008"
    case ("COV_UNBEKANNT"):
      return "261665006"
    default: null
  }
}