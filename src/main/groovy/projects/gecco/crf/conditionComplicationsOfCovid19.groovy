package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/complications-covid-19-profile
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * NOTE: Due to the Cardinality-restraint (1..1) for "code", multiple selections in CXX for this parameter
 *       will be added as additional codings.
 */


condition {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2"){
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "SarsCov2_KOMPLIKATIONEN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemDialyse = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_DIALYSE" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  final def crfItemCoinfect = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_PULMONALE_CO_INFEKTION" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  final def crfItemBlutinfekt = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_BLUTSTROMINFEKTION" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemDialyse && !crfItemCoinfect && !crfItemBlutinfekt){
    return // no export
  }

  if (crfItemDialyse[CrfItem.CATALOG_ENTRY_VALUE] != [] ||
          crfItemCoinfect[CrfItem.CATALOG_ENTRY_VALUE] != [] ||
          crfItemBlutinfekt[CrfItem.CATALOG_ENTRY_VALUE] != []){
    id = "ComplicationsOfCovid/" + context.source[studyVisitItem().crf().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/complications-covid-19"
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

    recordedDate {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }

  }
  code {
    if (crfItemDialyse[CrfItem.CATALOG_ENTRY_VALUE] != []) {
      crfItemDialyse[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = item[CatalogEntry.CODE] as String
        if (ICDcode == "COV_JA") {
          coding {
            system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
            version = "2020"
            code = "N17.9"
          }
        }
      }
      crfItemDialyse[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = item[CatalogEntry.CODE] as String
        if (SNOMEDcode == "COV_JA") {
          coding {
            system = "http://snomed.info/sct"
            code = "14669001"
          }
        }
      }
    }
    if (crfItemCoinfect[CrfItem.CATALOG_ENTRY_VALUE] != []) {
      crfItemCoinfect[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = item[CatalogEntry.CODE] as String
        if (ICDcode == "COV_JA") {
          coding {
            system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
            version = "2020"
            code = "J18.9"
          }
        }
      }
      crfItemCoinfect[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = item[CatalogEntry.CODE] as String
        if (SNOMEDcode == "COV_JA") {
          coding {
            system = "http://snomed.info/sct"
            code = "128601007"
          }
        }
      }

    }
    if (crfItemBlutinfekt[CrfItem.CATALOG_ENTRY_VALUE] != []) {
      crfItemBlutinfekt[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ICDcode = item[CatalogEntry.CODE] as String
        if (ICDcode == "COV_JA") {
          coding {
            system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
            version = "2020"
            code = "A41.9"
          }
        }
      }
      crfItemBlutinfekt[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = item[CatalogEntry.CODE] as String
        if (SNOMEDcode == "COV_JA") {
          coding {
            system = "http://snomed.info/sct"
            code = "434156008"
          }
        }
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}