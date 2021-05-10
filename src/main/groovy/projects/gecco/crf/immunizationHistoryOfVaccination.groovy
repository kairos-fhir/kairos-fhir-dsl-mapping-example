package projects.gecco.crf

import org.hl7.fhir.r4.model.Immunization

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/ForschungsnetzCovid-19/Immunization
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */

immunization {

  id = "Immunization/SVI-" + context.source[studyVisitItem().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization"
  }

  clinicalStatus = Immunization.ImmunizationStatus.COMPLETED

  patient {
    reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
  }

  recordedDate {
    date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
  }

  // TODO: work in progress
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

