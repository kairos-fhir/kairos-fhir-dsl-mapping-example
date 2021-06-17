package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
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
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2"){
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "SarsCov2_ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemVaccine = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_covid19f-dataelement-2.2211" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemVaccine){
    return //no export
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
    }

    patient {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    final def crfItemVaccineDate = context.source[studyVisitItem().crf().items()].find {
      "COV_GECCO_DAT_covid19f-dataelement-2.2211" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
    }

    crfItemVaccineDate[CrfItem.DATE_VALUE]?.each { final vaccDate ->
      if (vaccDate) {
        occurrenceDateTime {
          date = vaccDate as String
          //normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
          precision = TemporalPrecisionEnum.DAY.toString()
        }
      }
    }
  }
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_INFLUENZA"):
      return "6142004"
    case ("COV_PNEUMOKOKKEN"):
      return "16814004"
    case ("COV_BCG"):
      return "56717001"
    case ("COV_COVID19"):
      return "840539006"
    case ("COV_UMG_UNBEKANNT"):
      return "no-immunization-info"
    default: null
  }
}


