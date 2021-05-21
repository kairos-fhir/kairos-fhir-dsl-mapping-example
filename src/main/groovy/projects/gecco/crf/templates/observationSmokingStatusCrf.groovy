package projects.gecco.crf.templates


import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/smokingstatus
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * hints:
 *  A StudyEpisode is no regular episode and cannot reference an encounter
 */
observation {

  if (context.source[studyVisitItem().studyMember().study().profile().code()] != "Testprofil" &&
      context.source[studyVisitItem().studyMember().study().code()] != "RaucherTest") {
    return // no export
  }

  id = "Observation/SmokingStatus-" + context.source[studyVisitItem().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/smoking-status"
  }

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      system = "http://loinc.org"
      code = "72166-2"
    }
  }

  subject {
    reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
  }

  effectiveDateTime {
    date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
  }

  final def smokeStatSvi = context.source[studyVisitItem().crf().items()].find {
    "Grade (Radiobox (UsageEntry))" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (smokeStatSvi) {
    final Map<String, Object> multiValue = smokeStatSvi[LaborFindingLaborValue.MULTI_VALUE] as Map<String, Object>
    final def singleValue = multiValue.iterator().next()[UsageEntry.CODE] as String

    valueCodeableConcept {
      coding {
        code = mapSmokingStatus(singleValue)
      }
    }
  }

  //If the CRF contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
  final def smokeStatAnnotationSvi = context.source[studyVisitItem().crf().items()].find {
    "ANNOTATION_CODE" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (smokeStatAnnotationSvi) {
    note {
      text = smokeStatAnnotationSvi[LaborFindingLaborValue.STRING_VALUE] as String
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
    case null:
      return null
    case "CURRENT_EVERY_DAY_SMOKER_CODE":
      return "LA18976-3"
    case "FORMER_SMOKER_CODE":
      return "LA15920-4"
    case "NEVER_SMOKER_CODE":
      return "LA18978-9"
    case "UNKNOWN_IF_EVER_SMOKED_CODE":
      return "LA18980-5"
    case "1FAT":
      return "LA18976-3"
    case "DEFINITE":
      return "LA18978-9"
    default:
      return null
  }
}
