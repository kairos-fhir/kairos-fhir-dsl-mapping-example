package projects.gecco.umg

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/frailtyscore
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * hints:
 *  A StudyEpisode is no regular episode and cannot reference an encounter
 */
observation {


  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2") {
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "GECCO - DEMOGRAPHIE" || studyVisitStatus == "OPEN") {
    return //no export
  }

  final def crfItemFrailty = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_FARILITYSCORE" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemFrailty) {
    return //no export
  }

  id = "Observation/FrailtyScore-" + context.source[studyVisitItem().id()]

  meta {
    source = "https://fhir.centraxx.de"
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/frailty-score"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "survey"
    }
  }

  code {
    coding {
      system = "http://snomed.info/sct"
      code = "763264000"
    }
  }

  subject {
    reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
  }

  effectiveDateTime {
    date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
    precision = TemporalPrecisionEnum.DAY.toString()
  }


  crfItemFrailty[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
    final def Fcode = item[CatalogEntry.CODE]
    if (Fcode) {
      valueCodeableConcept {
        coding {
          system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score"
          code = getFrailtyScore(Fcode as String)
          display = mapFrailty(Fcode as String)
        }
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String getFrailtyScore(final String frailtyString) {
  switch (frailtyString) {
    case "COV_FRAILITY_VERY_FIT":
      return "1"
    case "COV_FRAILITY_WELL":
      return "2"
    case "COV_FRAILITY_MANAGING_WELL":
      return "3"
    case "COV_FRAILITY_VULNERABLE":
      return "4"
    case "COV_FRAILITY_MIDLY_FRAIL":
      return "5"
    case "COV_FRAILITY_MODERATE_FRAIL":
      return "6"
    case "COV_FRAILITY_SEVERELY_FRAIL":
      return "7"
    case "COV_FRAILITY_VERY_SEVERELY_FRAIL":
      return "8"
    case "COV_FRAILITY_TERMINALY_ILL":
      return "9"
    default:
      return null
  }
}

static String mapFrailty(final String frailty) {
  switch (frailty) {
    case "COV_FRAILITY_VERY_FIT":
      return "Very Fit"
    case "COV_FRAILITY_WELL":
      return "Well"
    case "COV_FRAILITY_MANAGING_WELL":
      return "Managing Well"
    case "COV_FRAILITY_VULNERABLE":
      return "Vulnerable"
    case "COV_FRAILITY_MIDLY_FRAIL":
      return "Mildly Frail"
    case "COV_FRAILITY_MODERATE_FRAIL":
      return "Moderately Frail"
    case "COV_FRAILITY_SEVERELY_FRAIL":
      return "Severely Frail"
    case "COV_FRAILITY_VERY_SEVERELY_FRAIL":
      return "Very Severely Frail"
    case "COV_FRAILITY_TERMINALY_ILL":
      return "Terminally Ill"
    default:
      return null
  }
}
