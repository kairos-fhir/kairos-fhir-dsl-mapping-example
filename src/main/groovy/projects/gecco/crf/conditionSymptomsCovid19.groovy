package projects.gecco.crf


import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/complications-covid-19-profile
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */
condition {

  id = "Condition/SVI-" + context.source[studyVisitItem().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/symptoms-covid-19"
  }

  extension {
    url = "https://simplifier.net/forschungsnetzcovid-19/uncertaintyofpresence"
    valueCodeableConcept {
      coding {
        system = "http://snomed.info/sct"
        code = "261665006"
      }
    }
  }

  category {
    coding {
      system = "http://loinc.org"
      code = "75325-1"
    }
  }

  subject {
    reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
  }

  // TODO: work in progress
}
