package projects.gecco.umg

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/sexassignedatbirth
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
  final def crfItemGen = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_Geschlecht_GEBURT" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemGen) {
    return
  }
  if (crfItemGen[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Observation/SexAssignedAtBirth-" + context.source[studyVisitItem().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sex-assigned-at-birth"
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
        code = "76689-9"
      }
    }

    subject {
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    valueCodeableConcept {
      crfItemGen[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
        final def LOINCcode = mapGender(item[CatalogEntry.CODE] as String)
        if (LOINCcode) {
          if (["male", "female", "unknown"].contains(LOINCcode)) {
            coding {
              system = "http://hl7.org/fhir/administrative-gender"
              code = LOINCcode
            }
          } else if (["x", "D"].contains(LOINCcode)) {
            coding {
              system = "http://fhir.de/CodeSystem/gender-amtlich-de"
              code = LOINCcode
            }
          }
        }
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String mapGender(final String gender) {
  switch (gender) {
    case "COV_MAENNLICH":
      return "male"
    case "COV_WEIBLICH":
      return "female"
    case "COV_KEINE_ANGABE":
      return "X"
    case "COV_DIVERS":
      return "D"
    case "COV_UNBEKANNT":
      return "unknown"
    default:
      return "unknown"
  }
}
