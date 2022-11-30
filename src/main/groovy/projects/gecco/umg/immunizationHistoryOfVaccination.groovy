package projects.gecco.umg

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Extension
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
  if (studyCode != "SARS-Cov-2") {
    return //no export
  }

  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "GECCO - ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemDisc = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_covid19f-dataelement-2.2211" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemDisc) {
    return
  }

  final def crfItems = context.source[studyVisitItem().crf().items()]
  if (!crfItems || crfItems == []) {
    return
  }

  id = "Immunization/HistoryOfVaccination-" + context.source[studyVisitItem().id()]

  meta {
    source = "https://fhir.centraxx.de"
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization"
  }


  clinicalStatus = Immunization.ImmunizationStatus.COMPLETED

  patient {
    reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
  }

  final def valIndex = []
  vaccineCode {
    crfItems?.each { final item ->
      final def measParamCode = item[CrfItem.TEMPLATE][CrfTemplateField.LABOR_VALUE][LaborValue.CODE]
      if (measParamCode == "COV_GECCO_covid19f-dataelement-2.2211") {
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

  if (getVaccineCode().getCoding().isEmpty()) {
    Coding coding = new Coding()
    coding.addExtension(new Extension("http://hl7.org/fhir/StructureDefinition/data-absent-reason", new CodeType("unsupported")))
    getVaccineCode().addCoding(coding)
  }


  final def crfItemVaccineDates = context.source[studyVisitItem().crf().items()]
  if (!crfItemVaccineDates || crfItemVaccineDates == []) {
    return
  }

  final List vaccDateList = []
  crfItemVaccineDates?.each { final dates ->
    final def vaccDateCode = dates[CrfItem.TEMPLATE][CrfTemplateField.LABOR_VALUE][LaborValue.CODE]
    if (vaccDateCode == "COV_GECCO_DAT_covid19f-dataelement-2.2211") {
      final String vDs_date_value = dates[CrfItem.DATE_VALUE]
      if (vDs_date_value){
        final String vDs = normalizeDate(dates[CrfItem.DATE_VALUE][PrecisionDate.DATE] as String)
        if (vDs) {
          vaccDateList.add(vDs)
        }
      }
    }
  }

  //Date of last vaccination --> "Date of full immunization
  def final vaccDdate = selectMostRecentDate(vaccDateList)
  if (vaccDdate){
    occurrenceDateTime {
      date = vaccDdate
    }
  }

  if (!getOccurrenceDateTime()) {
    DateTimeType dateTimeType = new DateTimeType()
    dateTimeType.addExtension(new Extension("http://hl7.org/fhir/StructureDefinition/data-absent-reason", new CodeType("not-performed")))
    setOccurrenceDateTime(dateTimeType)
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}

static String selectMostRecentDate(final List<String> vdl) {
  return !vdl ? null : (vdl.sort().last() as String)
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
