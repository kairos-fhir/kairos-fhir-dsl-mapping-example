package projects.mii.modul.person

import de.kairos.fhir.centraxx.metamodel.Consent
import de.kairos.fhir.centraxx.metamodel.ConsentType
import de.kairos.fhir.centraxx.metamodel.PatientStudy
import org.hl7.fhir.r4.model.ResearchSubject

import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ID
import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyMember

/**
 * Represented by CXX StudyMember
 * Specified by https://simplifier.net/medizininformatikinitiative-modulperson/probantin
 * @author Mike Wähnert
 * @since v.1.10.0, CXX.v.3.18.2
 */
researchSubject {

  id = "ResearchSubject/" + context.source[studyMember().id()]

  println(context.source)
  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/ResearchSubject"
  }

  status = ResearchSubject.ResearchSubjectStatus.ONSTUDY // messwert oder patient study status.

  identifier {
    type {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "ANON"
      }
    }
    system = "urn:centraxx" // site specific
    value = context.source[studyMember().studyMemberId()]
  }

  final def studyOid = context.source[studyMember().study().id()]
  study {
    reference = "ResearchStudy/" + studyOid
  }

  individual {
    reference = "Patient/" + context.source[studyMember().patientContainer().id()]
  }

  println(context.source[studyMember().patientContainer().consents()])
  println(context.source[studyMember().patientContainer().patientStudies()])

  final def studyConsent = context.source[studyMember().patientContainer().consents()].find { final def c ->
    println( c[Consent.CONSENT_TYPE][ConsentType.FLEXI_STUDY])
    return studyOid == c[Consent.CONSENT_TYPE][ConsentType.FLEXI_STUDY]?.getAt(ID)
  }

  println(studyConsent)
  if (studyConsent) {
    consent {
      reference = "Consent/" + studyConsent[ID]
    }
  }

  assignedArm = context.source[studyMember().enrollment().patientGroupName()]
  actualArm = context.source[studyMember().enrollment().patientGroupName()]

  final def patientStudy = context.source[studyMember().patientContainer().patientStudies()].find { final def patientStudy ->
    return studyOid == patientStudy[PatientStudy.FLEXI_STUDY]?.getAt(ID)
  }
  println("PatientStudy")
  println(patientStudy)
  if (patientStudy) {
    final def memberFrom = patientStudy[PatientStudy.MEMBER_FROM]
    final def memberUntil = patientStudy[PatientStudy.MEMBER_UNTIL]
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
}
