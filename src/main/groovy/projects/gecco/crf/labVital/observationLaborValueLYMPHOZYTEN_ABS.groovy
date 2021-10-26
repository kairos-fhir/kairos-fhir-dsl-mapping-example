package projects.gecco.crf.labVital.laborValue

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.FlexiStudy
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.StudyMember
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * Specified by https://simplifier.net/guide/GermanCoronaConsensusDataSet-ImplementationGuide/Laboratoryvalue
 * @author Lukas Reinert
 * @since KAIROS-FHIR-DSL.v.1.9.0, CXX.v.3.18.1.7
 *
 */

observation {

  final def studyMember = context.source[laborMapping().relatedPatient().studyMembers()].find {
    it[StudyMember.STUDY][FlexiStudy.CODE] == "SARS-Cov-2"
  }
  if (!studyMember) {
    return //no export
  }

  final def profileName = context.source[laborMapping().laborFinding().laborMethod().code()]
  if (profileName != "COV_GECOO_LABOR") {
    return //no export
  }

  final def lFlV = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COV_GECCO_LYMPHOZYTEN_ABS" == it[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE]
  }
  if (!lFlV) {
    return
  }

  final def numID = context.source[laborMapping().id()]
  final String labValID = lFlV[LaborFindingLaborValue.LABOR_VALUE][LaborValue.ID]

  identifier {
    type{
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "OBI"
      }
    }
    system = "http://www.acme.com/identifiers/patient"
    value = "Observation/LaborValue-" + labValID + "-" + numID
    assigner {
      reference = "Assigner/" + context.source[laborMapping().creator().id()]
    }
  }

  id = "Observation/LaborValue-" + labValID + "-" + numID

  meta {
    source = "https://fhir.centraxx.de"
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/ObservationLab"
  }

  status = Observation.ObservationStatus.FINAL

  category {
    coding {
      system = "http://loinc.org"
      code = "26436-6"
    }
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "laboratory"
    }
  }


  code{
    coding{
      system = "http://loinc.org"
      code = "26474-7"
    }
  }

  //Iteration to remove "[]" from id in string
  context.source[laborMapping().relatedPatient().idContainer().id()].each { final id ->
    subject {
      reference = "Patient/Patient-" + id
    }
  }


  effectiveDateTime {
    date = normalizeDate(context.source[laborMapping().creationDate()] as String)
    precision = TemporalPrecisionEnum.DAY.toString()
  }

  def numVal =lFlV[LaborFindingLaborValue.NUMERIC_VALUE]
  if (numVal){
    valueQuantity {
      value = numVal
      unit = "10*3/µL"
      system = "http://unitsofmeasure.org"
      code = "10*3/µL"
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}
