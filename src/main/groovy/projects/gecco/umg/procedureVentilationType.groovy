package projects.gecco.umg

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Extension

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
  if (crfName != "GECCO - THERAPIE" || studyVisitStatus == "OPEN") {
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

    crfItemRespThera[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
      status = matchResponseToStatus(item[CatalogEntry.CODE] as String)
    }

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
        else {
          extension{
            url = "http://hl7.org/fhir/StructureDefinition/data-absent-reason"
            valueCode = "not-applicable"
          }
        }
      }
    }
    subject {
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    performedDateTime {
      crfItemRespThera[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def CXXCode = item[CatalogEntry.CODE] as String
        if (!["COV_NEIN","COV_NA"].contains(CXXCode)){
          date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
          precision = TemporalPrecisionEnum.DAY.toString()
        }
      }
    }


    crfItemRespThera[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
      final def CXXCode = item[CatalogEntry.CODE] as String
      if (CXXCode == "COV_NEIN"){
        DateTimeType dateTimeType = new DateTimeType()
        dateTimeType.addExtension(new Extension("http://hl7.org/fhir/StructureDefinition/data-absent-reason", new CodeType("not-performed")))
        setPerformedDateTime(dateTimeType)
      }
      else if (CXXCode == "COV_NA"){
        DateTimeType dateTimeType = new DateTimeType()
        dateTimeType.addExtension(new Extension("http://hl7.org/fhir/StructureDefinition/data-absent-reason", new CodeType("not-applicable")))
        setPerformedDateTime(dateTimeType)
      }
    }
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_NEIN"):
      return "53950000"
    case ("COV_NHFST"):
      return "371907003"
    case ("COV_NIB"):
      return "428311008"
    case ("COV_INVASIVE_BEATMUNG"):
      return "40617009:425391005=26412008"
    case ("COV_TRACHEOTOMIE"):
      return "40617009:425391005=129121000"
    case ("COV_NA"):
      return "53950000"
  }
}

static String matchResponseToStatus(final String resp) {
  switch (resp) {
    case ("COV_NEIN"):
      return "not-done"
    case ("COV_NHFST"):
      return "in-progress"
    case ("COV_NIB"):
      return "in-progress"
    case ("COV_INVASIVE_BEATMUNG"):
      return "in-progress"
    case ("COV_TRACHEOTOMIE"):
      return "in-progress"
    case ("COV_NA"):
      return "unknown"
  }
}
