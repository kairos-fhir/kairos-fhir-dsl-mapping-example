package customexport.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a HDRP StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/extracorporealmembraneoxygenation
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, HDRP.v.3.18.1
 */
procedure {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2") {
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "SarsCov2_THERAPIE" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemRespECMO = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_ECMO" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemRespECMO) {
    return
  }
  if (crfItemRespECMO[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Procedure/ECMO" + context.source[studyVisitItem().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/extracorporeal-membrane-oxygenation"
    }

    crfItemRespECMO[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
      final def STATUScode = matchResponseToSTATUS(item[CatalogEntry.CODE] as String)
      if (STATUScode) {
        status = STATUScode
      }
    }

    category {
      coding {
        system = "http://snomed.info/sct"
        code = "277132007"
      }
    }

    code {
      coding {
        system = "http://fhir.de/CodeSystem/dimdi/ops"
        code = "8-852"
        version = "2021"
      }
      coding {
        system = "http://snomed.info/sct"
        code = "233573008"
      }
    }

    subject {
      reference = "Patient/Patient-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    performedDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String matchResponseToSTATUS(final String resp) {
  switch (resp) {
    case ("COV_JA"):
      return "in-progress"
    case ("COV_NEIN"):
      return "not-done"
    case ("COV_UNBEKANNT"):
      return "unknown"
    default: null
  }
}
