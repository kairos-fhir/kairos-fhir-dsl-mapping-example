package projects.mii.bielefeld


import de.kairos.fhir.centraxx.metamodel.StudyMember
import org.hl7.fhir.r4.model.ResearchSubject

import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ID
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientStudy

/**
 * Represented by CXX StudyMember
 * Specified by https://simplifier.net/medizininformatikinitiative-modulperson/probantin
 * @author Jonas Küttner
 * @since v.1.38.0, CXX.v.2024.4.0
 */
researchSubject {


  final def studyOid = context.source[patientStudy().flexiStudy().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/ResearchSubject"
  }

  final def patientStudyStatus = context.source[patientStudy().status()] as String

  status = patientStudyStatus != null ? ResearchSubject.ResearchSubjectStatus.fromCode(patientStudyStatus) : ResearchSubject.ResearchSubjectStatus.CANDIDATE

  final def studyMember = context.source[patientStudy().patientContainer().studyMembers()].find { final def sm ->
    sm[StudyMember.STUDY][ID] == studyOid
  }

  // PatientStudy must also have a study member, otherwise there is no identifier, which is mandatory for MII.
  if (!studyMember) {
    return
  }

  id = "ResearchSubject/" + context.source[patientStudy().id()]

  identifier {
    type {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "ANON"
      }
    }
    system = "urn:centraxx"
    value = studyMember[StudyMember.STUDY_MEMBER_ID]
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
