package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.RootEntities
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
  final def crfItemDisc = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_covid19f-dataelement-2.2211" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemDisc){
    return
  }

  final def crfItems = context.source[studyVisitItem().crf().items()]
  if (!crfItems || crfItems == []) {
    return
  }

  id = "HistoryOfVaccination" + context.source[studyVisitItem().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization"
  }

  clinicalStatus = Immunization.ImmunizationStatus.COMPLETED

  patient {
    reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
  }

  final def valIndex = []

  vaccineCode {
    crfItems?.each { final item ->
      final def measParamCode = item[CrfItem.TEMPLATE][CrfTemplateField.LABOR_VALUE][LaborValue.CODE]
      if (measParamCode == "COV_GECCO_covid19f-dataelement-2.2211"){
        valIndex.add(item[CrfItem.VALUE_INDEX])
        item[CrfItem.CATALOG_ENTRY_VALUE]?.each { final ite ->
          final def SNOMEDcode = matchResponseToSNOMED(ite[CatalogEntry.CODE] as String)
          if (SNOMEDcode) {
            coding {
              system = "http://snomed.info/sct"
              code = SNOMEDcode
            }
          }
        }
      }
    }
  }

  final def crfItemVaccineDates = context.source[studyVisitItem().crf().items()]
  if (!crfItemVaccineDates || crfItemVaccineDates == []) {
    return
  }

  crfItemVaccineDates?.each { final dates ->
    final def vaccDateCode = dates[CrfItem.TEMPLATE][CrfTemplateField.LABOR_VALUE][LaborValue.CODE]
    if (vaccDateCode == "COV_GECCO_DAT_covid19f-dataelement-2.2211"){
      final def valIndexDate = dates[CrfItem.VALUE_INDEX]
      if (valIndex.contains(valIndexDate)){
        dates[CrfItem.DATE_VALUE]?.each { final vD ->
          final def vaccDate = normalizeDate(vD.toString())
          if (vaccDate) {
            occurrenceDateTime {
              date = vaccDate
              precision = TemporalPrecisionEnum.DAY.toString()
            }
          }
        }
      }
    }
  }
}

static String normalizeDate(final String dateTimeString) {
  if (dateTimeString.contains("DAY")){
    return null
  }
  else{
    return dateTimeString != null ? dateTimeString.substring(5) : null
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