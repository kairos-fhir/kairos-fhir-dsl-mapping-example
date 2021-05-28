package projects.gecco.crf


import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/symptomscovid19-profile
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * NOTE: Due to the Cardinality-restraint (1..1) for "code", multiple selections in CXX for this parameter
 *       will be added as additional codings.
 */


condition {
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "SYMPTOME" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemGeruch = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_GERUCH_GESCHMACK" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  final def crfItemBauch = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_BAUSCHSCHMERZEN" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  final def crfItemBewusst = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_BEWUSSTSEIN_VERWIRRT" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  final def crfItemDurchfall = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_DURCHFALL" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  final def crfItemErbrechen = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_ERBRECHEN" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  final def crfItemHusten = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_HUSTEN" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  final def crfItemKurzatmig = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_KURZATMIG" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  final def crfItemUebelkeit = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_UEBELKEIT" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  final def crfItemFieber = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_FIEBER" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  final def crfItemKopfschmerzen = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_KOPFSCHMERZEN" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }


  if (!crfItemGeruch &&
          !crfItemBauch &&
          !crfItemBewusst &&
          !crfItemDurchfall &&
          !crfItemErbrechen &&
          !crfItemHusten &&
          !crfItemKurzatmig &&
          !crfItemUebelkeit &&
          !crfItemFieber &&
          !crfItemKopfschmerzen){
    return //no export
  }

  else {

    id = "ChronicLiverDisease/" + context.source[studyVisitItem().crf().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/symptoms-covid-19"
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
        system = "http://loinc.org"
        code = "75325-1"
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    recordedDate {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
    }
  }


  code {
    if (crfItemGeruch){
      if (crfItemGeruch[CrfItem.CATALOG_ENTRY_VALUE] != []){
        crfItemGeruch[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = item[CatalogEntry.CODE] as String
          if (SNOMEDcode == "COV_JA") {
            coding {
              system = "http://snomed.info/sct"
              code = "36955009" //or 44169009
            }
          }
        }
      }
    }
    if (crfItemBauch) {
      if (crfItemBauch[CrfItem.CATALOG_ENTRY_VALUE] != []) {
        crfItemBauch[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = item[CatalogEntry.CODE] as String
          if (SNOMEDcode == "COV_JA") {
            coding {
              system = "http://snomed.info/sct"
              code = "21522001"
            }
          }
        }
      }
    }
    if (crfItemBewusst) {
      if (crfItemBewusst[CrfItem.CATALOG_ENTRY_VALUE] != []) {
        crfItemBewusst[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = item[CatalogEntry.CODE] as String
          if (SNOMEDcode == "COV_JA") {
            coding {
              system = "http://snomed.info/sct"
              code = "40917007"
            }
          }
        }
      }
    }
    if (crfItemDurchfall) {
      if (crfItemDurchfall[CrfItem.CATALOG_ENTRY_VALUE] != []) {
        crfItemDurchfall[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = item[CatalogEntry.CODE] as String
          if (SNOMEDcode == "COV_JA") {
            coding {
              system = "http://snomed.info/sct"
              code = "62315008"
            }
          }
        }
      }
    }
    if (crfItemErbrechen) {
      if (crfItemErbrechen[CrfItem.CATALOG_ENTRY_VALUE] != []) {
        crfItemErbrechen[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = item[CatalogEntry.CODE] as String
          if (SNOMEDcode == "COV_JA") {
            coding {
              system = "http://snomed.info/sct"
              code = "422400008"
            }
          }
        }
      }
    }
    if (crfItemHusten) {
      if (crfItemHusten[CrfItem.CATALOG_ENTRY_VALUE] != []) {
        crfItemHusten[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = item[CatalogEntry.CODE] as String
          if (SNOMEDcode == "COV_JA") {
            coding {
              system = "http://snomed.info/sct"
              code = "49727002"
            }
          }
        }
      }
    }
    if (crfItemKurzatmig) {
      if (crfItemKurzatmig[CrfItem.CATALOG_ENTRY_VALUE] != []) {
        crfItemKurzatmig[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = item[CatalogEntry.CODE] as String
          if (SNOMEDcode == "COV_JA") {
            coding {
              system = "http://snomed.info/sct"
              code = "267036007"
            }
          }
        }
      }
    }
    if (crfItemUebelkeit) {
      if (crfItemUebelkeit[CrfItem.CATALOG_ENTRY_VALUE] != []) {
        crfItemUebelkeit[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = item[CatalogEntry.CODE] as String
          if (SNOMEDcode == "COV_JA") {
            coding {
              system = "http://snomed.info/sct"
              code = "422587007"
            }
          }
        }
      }
    }
    if (crfItemFieber) {
      if (crfItemFieber[CrfItem.CATALOG_ENTRY_VALUE] != []) {
        crfItemFieber[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = item[CatalogEntry.CODE] as String
          if (SNOMEDcode == "COV_JA") {
            coding {
              system = "http://snomed.info/sct"
              code = "426000000"
            }
          }
        }
      }
    }
    if (crfItemKopfschmerzen) {
      if (crfItemKopfschmerzen[CrfItem.CATALOG_ENTRY_VALUE] != []) {
        crfItemKopfschmerzen[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = item[CatalogEntry.CODE] as String
          if (SNOMEDcode == "COV_JA") {
            coding {
              system = "http://snomed.info/sct"
              code = "25064002"
            }
          }
        }
      }
    }
  }

}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}