package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/sarscov2igaserpliaacnc
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * hints:
 *  A StudyEpisode is no regular episode and cannot reference an encounter
 */

observation {
  //final def studyMember = context.source[laborMapping().relatedPatient().studyMembers()].find{
  //  it[StudyMember.STUDY][FlexiStudy.CODE] == "SARS-Cov-2"
  //}
  //if (!studyMember) {
  //  return //no export
  //}
  final def profileName = context.source[laborMapping().laborFinding().laborMethod().code()]
  if (profileName != "COV_GECOO_LABOR") {
    return //no export
  }

  final def labValAb = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COV_GECCO_SARS-COV-2_IGG_IA" == it[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE]
  }
  if (!labValAb) {
    return
  }


  id = "Observation/SARSCoV2-IgA-" + context.source[laborMapping().id()]

  meta {
    source = "https://fhir.centraxx.de"
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sars-cov-2-iga-ser-pl-ia-acnc"
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
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "94720-0"
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source[laborMapping().creationDate()] as String)
    precision = TemporalPrecisionEnum.DAY.toString()
  }

  //Iteration to remove "[]" from id in string
  context.source[laborMapping().relatedPatient().idContainer().id()].each { final id ->
    subject {
      reference = "Patient/Patient-" + id
    }
  }

  labValAb[LaborFindingLaborValue.NUMERIC_VALUE]?.each { final numVal ->
    if (numVal){
      valueQuantity {
        value = numVal
        unit = "mg/dl"
        system = "http://unitsofmeasure.org"
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}