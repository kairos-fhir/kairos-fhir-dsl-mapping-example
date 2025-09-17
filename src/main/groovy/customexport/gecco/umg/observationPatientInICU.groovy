package customexport.gecco.umg

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a HDRP StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/patientinicu
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, HDRP.v.3.18.1
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
  if (crfName != "GECCO - THERAPIE" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemPatinICU = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_INTENSIVSTATION" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemPatinICU) {
    return
  }
  if (crfItemPatinICU[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Observation/PatientInICU-" + context.source[studyVisitItem().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/Patient-in-icu"
    }

    status = "final"

    code {
      coding {
        system = "http://loinc.org"
        code = "95420-6"
      }
    }

    subject {
      reference = "Patient/UMG-HDRP-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }

    valueCodeableConcept {
      crfItemPatinICU[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = mapExpoSNOMED(item[CatalogEntry.CODE] as String)
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

static String mapExpoSNOMED(final String patInICU) {
  switch (patInICU) {
    default:
      return null
    case "COV_JA":
      return "373066001"
    case "COV_NEIN":
      return "373067005"
    case "COV_NA":
      return "385432009"
  }
}
