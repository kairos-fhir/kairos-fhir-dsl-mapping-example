package projects.gecco.umg

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/dependenceonventilator
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * NOTE: Due to the Cardinality-restraint (1..1) for "code", multiple selections in CXX for this parameter
 *       will be added as additional codings.
 */
condition {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2") {
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "OUTCOME BEI ENTLASSUNG" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemVent = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_RESP_OUTCOME" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemVent) {
    return //no export
  }
  if (crfItemVent[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Condition/DependenceOnVentilator-" + context.source[studyVisitItem().crf().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/dependence-on-ventilator"
    }

    verificationStatus {
      crfItemVent[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def VERcode = matchResponseToVerStat(item[CatalogEntry.CODE] as String)
        if (VERcode) {
          coding {
            system = "http://snomed.info/sct"
            code = VERcode
          }
          coding {
            system = "http://terminology.hl7.org/CodeSystem/condition-ver-status"
            code = matchResponseToVerificationStatusHL7(item[CatalogEntry.CODE] as String)
          }
        }
      }
    }

    category {
      coding {
        system = "http://snomed.info/sct"
        code = "404989005"
      }
    }

    code {
      crfItemVent[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
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
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    recordedDate {
      date = normalizeDate(crfItemVent[CrfItem.CREATIONDATE] as String)
      precision = TemporalPrecisionEnum.SECOND.toString()
    }
  }
}


static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_BEATMET_JA"):
      return "444932008"
    case ("COV_BEATMET_NEIN"):
      return "444932008"
    default: null
  }
}

static String matchResponseToVerStat(final String resp) {
  switch (resp) {
    case ("COV_BEATMET_JA"):
      return "410605003"
    case ("COV_BEATMET_NEIN"):
      return "410594000"
    default: null
  }
}
static String matchResponseToVerificationStatusHL7(final String resp) {
  switch (resp) {
    case null:
      return null
    case ("COV_BEATMET_JA"):
      return "confirmed"
    case ("COV_BEATMET_NEIN"):
      return "refuted"
    default: null
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}
