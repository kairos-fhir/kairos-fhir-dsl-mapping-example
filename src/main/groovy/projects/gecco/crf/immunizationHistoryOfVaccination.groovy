package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Immunization

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/immunization
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */
immunization {
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemVaccine = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_IMPFUNGEN" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemVaccine[CrfItem.CATALOG_ENTRY_VALUE] != []) {

    id = "HistoryOfVaccination" + context.source[studyVisitItem().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization"
    }

    clinicalStatus = Immunization.ImmunizationStatus.COMPLETED

    vaccineCode {
      crfItemVaccine[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = matchResponseToSNOMED(item[CatalogEntry.CODE] as String)
        if (SNOMEDcode) {
          coding {
            system = "http://snomed.info/sct"
            code = SNOMEDcode
          }
        }
      }
      crfItemVaccine[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def ATCcode = matchResponseToATC(item[CatalogEntry.CODE] as String)
        if (ATCcode) {
          coding {
            system = "http://fhir.de/CodeSystem/dimdi/atc"
            version = "2020"
            code = ATCcode
          }
        }
      }
    }

    patient {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    occurrenceDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
    }

  }
}

static String matchResponseToATC(final String resp) {
  switch (resp) {
    case ("COV_INFLUENZA"):
      return "J07AG"
    case ("COV_PNEUMOKOKKEN"):
      return "J07AL"
    case ("COV_BCG"):
      return ""
    case ("COV_COVID19"):
      return ""
    case ("COV_UMG_UNBEKANNT"):
      return "261665006"
    default: null
  }
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_INFLUENZA"):
      return "871895005"
    case ("COV_PNEUMOKOKKEN"):
      return "836398006+836380007"
    case ("COV_BCG"):
      return "J07AN"
    case ("COV_COVID19"):
      return "J07BX03"
    case ("COV_UMG_UNBEKANNT"):
      return "Unknown"
    default: null
  }
}



static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

