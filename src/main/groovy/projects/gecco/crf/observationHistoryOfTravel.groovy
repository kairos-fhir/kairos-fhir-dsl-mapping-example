package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/historyoftravel
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * hints:
 *  A StudyEpisode is no regular episode and cannot reference an encounter
 */
observation {
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemTravel = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_REISE" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemTravel?.toString() != "null") {
    //id = "HistoryOfTravel/" + context.source[studyVisitItem().id()]
    id = "HistoryOfTravel/123" + crfItemTravel[CrfItem.STRING_VALUE]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/history-of-travel"
    }

    status = Observation.ObservationStatus.UNKNOWN

    category{
      coding{
        system = "http://terminology.hl7.org/CodeSystem/observation-category"
        code = "social-history"
      }
    }
    code {
      coding {
        system = "http://loinc.org"
        code = "8691-8"
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
    }

    final def SNOMEDcode = mapTravel(crfItemTravel[CrfItem.STRING_VALUE] as String)
    if (SNOMEDcode) {
      valueCodeableConcept {
        coding{
          code = SNOMEDcode
          system = "http://snomed.info/sct"
        }
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String mapTravel(final String smokingStatus) {
  switch (smokingStatus) {
    case "Ja":
      return "373066001"
    case "Nein":
      return "373067005"
    case "Unbekannt":
      return "261665006"
    case "Andere":
      return "74964007"
    case "Nicht anwendbar":
      return "385432009"
    default:
      return null
  }
}
