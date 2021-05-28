package projects.gecco.crf

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
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "OUTCOME BEI ENTLASSUNG" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemVent = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_RESP_OUTCOME" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemVent){
    return //no export
  }
  if (crfItemVent[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "DependenceOnVentilator/" + context.source[studyVisitItem().crf().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/dependence-on-ventilator"
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
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    recordedDate {
      date = normalizeDate(crfItemVent[CrfItem.CREATIONDATE] as String)
    }
  }
}


static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_BEATMET_JA"):
      return "410605003"
    case ("COV_BEATMET_NEIN"):
      return "410594000"
    default: null
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}