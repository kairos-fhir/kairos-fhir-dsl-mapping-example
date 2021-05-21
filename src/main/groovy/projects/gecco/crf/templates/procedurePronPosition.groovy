package projects.gecco.crf.templates


import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem
/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/proneposition-procedure
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */
procedure {

  id = "Procedure/SVI-" + context.source[studyVisitItem().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/prone-position"
  }

  status = "unknown"

  subject {
    reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
  }

  performedDateTime {
    date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
  }

  // TODO: work in progress
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}
