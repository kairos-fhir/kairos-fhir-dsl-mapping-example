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
 * Specified by https://simplifier.net/forschungsnetzcovid-19/dischargedisposition
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
  if (crfName != "OUTCOME BEI ENTLASSUNG" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemDisc = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_ENTLASSUNGSART" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemDisc) {
    return
  }
  if (crfItemDisc[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Observation/DischargeDisposition-" + context.source[studyVisitItem().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/discharge-disposition"
    }

    status = Observation.ObservationStatus.UNKNOWN

    category {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/observation-category"
        code = "social-history"
      }
    }

    code {
      coding {
        system = "http://loinc.org"
        code = "55128-3"
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
      crfItemDisc[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def SNOMEDcode = mapDiscSNOMED(item[CatalogEntry.CODE] as String)
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


static String mapDiscSNOMED(final String discharge) {
  switch (discharge) {
    default:
      return null
    case "COV_LEBEND":
      return "371827001"
    case "COV_KRANKENHAUSEINWEISUNG":
      return "32485007"
    case "COV_UEBERWEISUNG":
      return "3457005"
    case "COV_TOD":
      return "419099009"
    case "COV_PALLIATIV":
      return "306237005"
    case "COV_UNBEKANNT":
      return "261665006"
  }
}
