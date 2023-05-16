package projects.gecco.uksh

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Tests the export initializing for the edc units and recorded on date
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.22.0, CXX.v.2022.3.2
 *
 * hints:
 *  A StudyEpisode is no regular episode and cannot reference an encounter
 */
observation {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "deetetest") {
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  if (crfName != "EdcUnitTest") {
    return //no export
  }
  final def crfItemHeight = context.source[studyVisitItem().crf().items()].find {
    "AA_Unit" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemHeight) {
    return //no export
  }

  if (crfItemHeight[CrfItem.NUMERIC_VALUE]) {
    id = "Observation/AA-Unit-" + context.source[studyVisitItem().id()]
    status = Observation.ObservationStatus.UNKNOWN


    code {
      coding {
        system = "urn:centraxx"
        code = "AA_Unit"
      }
    }

    subject {
      reference = "Patient/Patient-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    effectiveDateTime {
      date = crfItemHeight[CrfItem.EDC_VALUE_RECORDED_ON][PrecisionDate.DATE]
      precision = TemporalPrecisionEnum.MONTH.toString()
    }

    valueQuantity {
      value = crfItemHeight[CrfItem.NUMERIC_VALUE]
      unit = crfItemHeight[CrfItem.EDC_VALUE_UNIT]
      system = "http://unitsofmeasure.org"
      code = crfItemHeight[CrfItem.EDC_VALUE_UNIT] as String
    }

    referenceRange {
      low {
        value = crfItemHeight[CrfItem.LOWER_RANGE_NUMERIC] as Number
      }
      high {
        value = crfItemHeight[CrfItem.UPPER_RANGE_NUMERIC] as Number
      }
    }
  }
}
