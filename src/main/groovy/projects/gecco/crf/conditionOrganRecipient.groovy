package projects.gecco.crf


import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

//import javax.xml.catalog.Catalog

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/organrecipient
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
  final def crfItemOrgan = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_ORGANTRANSPLANIERT" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemOrgan[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "HistoryOfOrganTransplant/" + context.source[studyVisitItem().crf().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/organ-recipient"
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
        code = "788415003"
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    code {
      crfItemOrgan[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = matchResponseToICD(item[CatalogEntry.CODE] as String)
        if (ICDcode) {
          coding {
            system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
            version = "2020"
            code = ICDcode
          }
        }
      }
      /*
      crfItemOrgan[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = matchResponseToSNOMED(item[CatalogEntry.CODE] as String)
        if (SNOMEDcode) {
          coding {
            system = "http://snomed.info/sct"
            code = SNOMEDcode
          }
        }
      }
      */
    }
    recordedDate {
      recordedDate = crfItemOrgan[CrfItem.CREATIONDATE] as String
    }
  }
}

static String matchResponseToICD(final String resp) {
  switch (resp) {
    case ("COV_HERZ"):
      return "Z94.1"
    case ("COV_LUNGE"):
      return "Z94.2"
    case ("COV_LEBER"):
      return "Z94.4"
    case ("COV_NIEREN"):
      return "Z94.0"
    case ("COV_DARM"):
      return "Z94.88"
    case ("COV_HAUT"):
      return "Z94.5"
    case ("COV_HORNHAUT"):
      return "Z94.7"
    case ("COV_GEHOERKNOECHELCHEN"):
      return ""
    case ("COV_HERZKLAPPEN"):
      return "Z95.5"
    case ("COV_BLUTGEFAESSE"):
      return "Z95.88"
    case ("COV_HIRNHAUT"):
      return ""
    case ("COV_KNOCHENGEWEBE"):
      return "Z94.6"
    case ("COV_KNORPELGEWEBE"):
      return ""
    case ("COV_SEHNE"):
      return ""
    default: null
  }
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_HERZ"):
      return ""
    case ("COV_LUNGE"):
      return ""
    case ("COV_LEBER"):
      return ""
    case ("COV_NIEREN"):
      return ""
    case ("COV_DARM"):
      return ""
    case ("COV_HAUT"):
      return ""
    case ("COV_HORNHAUT"):
      return ""
    case ("COV_GEHOERKNOECHELCHEN"):
      return ""
    case ("COV_HERZKLAPPEN"):
      return ""
    case ("COV_BLUTGEFAESSE"):
      return ""
    case ("COV_HIRNHAUT"):
      return ""
    case ("COV_KNOCHENGEWEBE"):
      return ""
    case ("COV_KNORPELGEWEBE"):
      return ""
    case ("COV_SEHNE"):
      return ""
    default: null
  }
}