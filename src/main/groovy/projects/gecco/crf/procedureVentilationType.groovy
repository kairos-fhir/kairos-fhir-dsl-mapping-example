package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/respiratorytherapies-procedure
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
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
  final def crfItemRespThera = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_BEATMUNGSTYP" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemRespThera) {
    return
  }
  if (crfItemRespThera[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Procedure/VentilationType-" + context.source[studyVisitItem().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/respiratory-therapies"
    }

    status = "unknown"

    category {
      coding {
        system = "http://snomed.info/sct"
        code = "277132007"
      }
    }

    code {
      crfItemRespThera[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
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

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_NEIN"):
      return "not-performed"
    case ("COV_NHFST"):
      return "371907003:425391005=426854004"
    case ("COV_NIB"):
      return "428311008"
    case ("COV_INVASIVE_BEATMUNG"):
      return "40617009:425391005=26412008"
    case ("COV_TRACHEOTOMIE"):
      return "40617009:425391005=129121000"
    default: null
  }
}
