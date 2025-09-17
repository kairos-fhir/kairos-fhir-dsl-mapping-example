package customexport.gecco.crf.labVital

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.FlexiStudy
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.StudyMember
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a HDRP StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/gaspanel-pao2
 * @author Lukas Reinert
 * @since KAIROS-FHIR-DSL.v.1.9.0, HDRP.v.3.18.1.7
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
  if (profileName != "COV_GECCO_VITALPARAMTER") {
    return //no export
  }

  final def labVal = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COV_GECCO_PAO2" == it[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE]
  }
  if (!labVal) {
    return
  }

  identifier {
    type{
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "OBI"
      }
    }
    system = "http://www.acme.com/identifiers/patient"
    value = "Observation/PaO2-" + context.source[laborMapping().id()]
    assigner {
      reference = "Assigner/" + context.source[laborMapping().creator().id()]
    }
  }

  id = "Observation/PaO2-" + context.source[laborMapping().id()]

  meta {
    source = "https://fhir.centraxx.de"
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/oxygen-partial-pressure"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://loinc.org"
      code = "26436-6"
    }
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "laboratory"
    }
    coding {
      system = "http://loinc.org"
      code = "18767-4"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "11556-8"
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

  labVal[LaborFindingLaborValue.NUMERIC_VALUE]?.each { final numVal ->
    if (numVal){
      valueQuantity {
        value = numVal
        unit = "mmHg"
        system = "http://unitsofmeasure.org"
        code = "mm[Hg]"
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}