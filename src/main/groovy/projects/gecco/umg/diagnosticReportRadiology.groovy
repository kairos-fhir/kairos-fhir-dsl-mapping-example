package projects.gecco.umg

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/radiologydiagnosticreport
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * hints:
 *  A StudyEpisode is no regular episode and cannot reference an encounter
 */
diagnosticReport {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2") {
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "BILDGEBUNG" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemFinding = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_BEFUND_BILD_LUNGE" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemFinding) {
    return
  }

  if (crfItemFinding[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "DiagnosticReport/DiagnosticReportRadiology-" + context.source[studyVisitItem().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/diagnostic-report-radiology"
    }

    status = DiagnosticReport.DiagnosticReportStatus.UNKNOWN

    category {
      coding {
        system = "http://loinc.org"
        code = "18726-0"
      }
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0074"
        code = "RAD"
      }
    }


    code {
      coding {
        system = "http://loinc.org"
        code = "18748-4"
      }
    }

    subject {
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }


    crfItemFinding[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
      final def SNOMEDcode = mapSmokingStatus(item[CatalogEntry.CODE] as String)
      if (SNOMEDcode) {
        conclusionCode {
          coding {
            system = "http://snomed.info/sct"
            code = SNOMEDcode
          }
        }
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

//Function to map CXX controlled vocabulary codes to LOINC codes
//Alternatively CXX controlled vocabulary codes could also directly be set to LOINC codes
static String mapSmokingStatus(final String smokingStatus) {
  switch (smokingStatus) {
    case "COV_UNSPEZIFISCHER_BEFUND":
      return "118247008:363713009=373068000"
    case "COV_COVID_TYP_BEFUND":
      return "118247008:{363713009=263654008,42752001=840539006}"
    case "COV_NORMALBEFUND":
      return "118247008:363713009=17621005"
    default:
      return null
  }
}
