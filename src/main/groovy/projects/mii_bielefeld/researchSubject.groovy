package projects.mii_bielefeld

import de.kairos.fhir.centraxx.metamodel.StudyMember
import org.hl7.fhir.r4.model.ResearchSubject

import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ID
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientStudy

/**
 * Represented by CXX StudyMember
 * Specified by https://simplifier.net/medizininformatikinitiative-modulperson/probantin
 * @author Mike Wähnert
 * @since v.1.38.0, CXX.v.2024.4.0
 */
researchSubject {

  id = "ResearchSubject/" + context.source[patientStudy().id()]

  final def studyOid = context.source[patientStudy().flexiStudy().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/ResearchSubject"
  }

  final def patientStudyStatus = context.source[patientStudy().status()] as String

  status = patientStudyStatus != null ? ResearchSubject.ResearchSubjectStatus.fromCode(patientStudyStatus) : ResearchSubject.ResearchSubjectStatus.CANDIDATE
  // messwert oder patient study status.

  final def studyMember = context.source[patientStudy().patientContainer().studyMembers()].find { final def sm ->
    sm[StudyMember.STUDY][ID] == studyOid
  }

  if (studyMember) {
    identifier {
      type {
        coding {
          system = "http://terminology.hl7.org/CodeSystem/v2-0203"
          code = "ANON"
        }
      }
      system = "urn:centraxx" // site specific
      value = studyMember[StudyMember.STUDY_MEMBER_ID]
    }
  }

  study {
    reference = "ResearchStudy/" + studyOid
  }

  individual {
    reference = "Patient/" + context.source[patientStudy().patientContainer().id()]
  }


  consent {
    reference = "Consent/" + context.source[patientStudy().consent().id()]
  }


  final def memberFrom = context.source[patientStudy().memberFrom()]
  final def memberUntil = context.source[patientStudy().memberUntil()]
  period {
    if (memberFrom) {
      start {
        date = memberFrom
      }
    }
    if (memberUntil) {
      end {
        date = memberUntil
      }
    }
  }
}
