package projects.gecco.umg

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/bodyweight
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
  final def crfItemWeight = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_GEWICHT" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemWeight) {
    return
  }
  if (crfItemWeight[CrfItem.NUMERIC_VALUE]) {
    id = "Observation/BodyWeight-" + context.source[studyVisitItem().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-weight"
    }

    status = Observation.ObservationStatus.UNKNOWN

    category {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/observation-category"
        code = "vital-signs"
      }
    }

    code {
      coding {
        system = "http://loinc.org"
        code = "29463-7"
      }
      coding {
        system = "http://snomed.info/sct"
        code = "27113001"
      }
    }

    subject {
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
      precision = TemporalPrecisionEnum.DAY.toString()
    }

    crfItemWeight[CrfItem.NUMERIC_VALUE]?.each { final item ->
      if (item) {
        valueQuantity {
          value = item
          unit = "kilogram"
          system = "http://unitsofmeasure.org"
          code = "kg"
        }
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}
