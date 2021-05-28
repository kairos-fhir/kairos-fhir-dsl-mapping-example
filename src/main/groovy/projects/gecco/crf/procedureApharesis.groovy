package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/apheresis-procedure
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */


procedure {
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "MEDIKATION" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemRespAphe = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_APHERESE" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemRespAphe){
    return
  }
  if (crfItemRespAphe[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Apheresis/" + context.source[studyVisitItem().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/apheresis"
    }

    status = "unknown"

    category {
      coding{
        system = "http://snomed.info/sct"
        code = "277132007"
      }
    }

    code {
      crfItemRespAphe[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def OPScode = matchResponseToOPS(item[CatalogEntry.CODE] as String)
        if (OPScode) {
          coding {
            system = "http://fhir.de/CodeSystem/dimdi/ops"
            code = OPScode
          }
        }
      }
      crfItemRespAphe[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = matchResponseToSNOMED(item[CatalogEntry.CODE] as String)
        if (SNOMEDcode) {
          coding {
            system = "http://snomed.info/sct"
            code = SNOMEDcode
          }
        }
      }
    }
    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    performedDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
    }
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}


static String matchResponseToOPS(final String resp) {
  switch (resp) {
    case ("COV_JA"):
      return "8-82"
    default: null
  }
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_JA"):
      return "127788007"
    default: null
  }
}