package projects.mii.modul.person

import de.kairos.fhir.centraxx.metamodel.RootEntities
import org.hl7.fhir.r4.model.ResearchSubject

import static de.kairos.fhir.centraxx.metamodel.RootEntities.*
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient
import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyMember

/**
 * Represented by CXX StudyMember
 * @author Mike Wähnert
 * @since v.1.9.0, CXX.v.3.18.2
 * TODO: work in progress
 */
researchSubject {

  id = "ResearchSubject/" + context.source[studyMember().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/ResearchSubject"
  }

  status = ResearchSubject.ResearchSubjectStatus.ONSTUDY

  study {
    reference = "ResearchStudy/" + context.source[studyMember().study().id()]
  }

  individual {
    reference = "Patient/" + context.source[studyMember().patientContainer().id()]
  }

}
