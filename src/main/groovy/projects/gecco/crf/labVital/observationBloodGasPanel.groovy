package projects.gecco.crf.labVital

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.FlexiStudy
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.StudyMember
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/bloodgaspanel
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
  if (profileName != "COV_GECCO_VITALPARAMTER") {
    return //no export
  }
  final def labValPaO2 = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COV_GECCO_PAO2" == it[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE]
  }
  final def labValPaCO2 = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COV_GECCO_PACO2" == it[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE]
  }
  final def labValFiO2 = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COV_GECCO_FIO2" == it[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE]
  }
  final def labValOxySat = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COV_GECCO_PERI_O2" == it[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE]
  }
  final def labVal_pH = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COV_GECCO_PH_BLUT" == it[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE]
  }

  if (!labValPaO2 &&
          !labValPaCO2 &&
          !labValFiO2 &&
          !labValOxySat &&
          !labVal_pH) {
    return //no export
  }


  identifier {
    type{
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "OBI"
      }
    }
    system = "http://www.acme.com/identifiers/patient"
    value = "Observation/BloodGasPanel-" + context.source[laborMapping().id()]
    assigner {
      reference = "Assigner/" + context.source[laborMapping().creator().id()]
    }
  }

  id = "Observation/BloodGasPanel-" + context.source[laborMapping().id()]

  meta {
    source = "https://fhir.centraxx.de"
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/blood-gas-panel"
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
      code = "24338-6"
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

  if(labValPaO2){
    hasMember {
      reference = "Observation/PaO2-" + context.source[laborMapping().id()]
    }
  }
  if(labValPaCO2){
    hasMember {
      reference = "Observation/PaCO2-" + context.source[laborMapping().id()]
    }
  }
  if(labValFiO2){
    hasMember {
      reference = "Observation/FiO2-" + context.source[laborMapping().id()]
    }
  }
  if(labValOxySat){
    hasMember {
      reference = "Observation/PeriO2Saturation-" + context.source[laborMapping().id()]
    }
  }
  if(labVal_pH){
    hasMember {
      reference = "Observation/pH-" + context.source[laborMapping().id()]
    }
  }



}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}