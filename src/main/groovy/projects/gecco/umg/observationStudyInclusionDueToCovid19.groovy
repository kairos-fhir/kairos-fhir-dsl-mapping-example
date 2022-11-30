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
 * Specified by https://simplifier.net/forschungsnetzcovid-19/studyinclusioncovid19
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 */
observation {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2") {
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "STUDIENEINSCHLUSS / EINSCHLUSSKRITERIEN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemStudy = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_STUDIENEINSCHLUSS" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemStudy) {
    return
  }
  if (crfItemStudy[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Observation/StudyInclusionDueToCovid-" + context.source[studyVisitItem().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/study-inclusion-covid-19"
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
        system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes"
        code = "02"
      }
    }

    subject {
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
      precision = TemporalPrecisionEnum.SECOND.toString()
    }

    valueCodeableConcept {
      crfItemStudy[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = mapInclusionSNOMED(item[CatalogEntry.CODE] as String)
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


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String mapInclusionSNOMED(final String inclusion) {
  switch (inclusion) {
    default:
      return null
    case "COV_JA":
      return "373066001"
    case "COV_NEIN":
      return "373067005"
    case "COV_UNBEKANNT":
      return "261665006"
    case "COV_ANDERE":
      return "74964007"
    case "COV_NA":
      return "385432009"
  }
}
