package projects.gecco.crf

import org.hl7.fhir.r4.model.MedicationStatement

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/pharmacologicaltherapy
 * @author Mike Wähnert
 * @since v.1.8.0, CXX.v.3.18.1
 */
medicationStatement {

  id = "MedicationStatement/SVI-" + context.source[studyVisitItem().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy"
  }

  status = MedicationStatement.MedicationStatementStatus.UNKNOWN

  // TODO: work in progress
}
