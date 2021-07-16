package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
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
  if (crfName != "SarsCov2_DEMOGRAPHIE" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemFrailty = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_FARILITYSCORE" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemFrailty) {
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
      reference = "Patient/Patient-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }


    crfItemFrailty[CrfItem.NUMERIC_VALUE]?.each { final item ->
      final def Fcode = item as Integer
      if (Fcode) {
        valueCodeableConcept {
          coding {
            system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/frailty-score"
            code = Fcode as String
            display = mapFrailty(Fcode as String)
          }
        }
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String mapFrailty(final String frailty) {
  switch (frailty) {
    case "1":
      return "Very Fit"
    case "2":
      return "Well"
    case "3":
      return "Managing Well"
    case "4":
      return "Vulnerable"
    case "5":
      return "Mildly Frail"
    case "6":
      return "Moderately Frail"
    case "7":
      return "Severely Frail"
    case "8":
      return "Very Severely Frail"
    case "9":
      return "Terminally Ill"
    default:
      return null
  }
}
