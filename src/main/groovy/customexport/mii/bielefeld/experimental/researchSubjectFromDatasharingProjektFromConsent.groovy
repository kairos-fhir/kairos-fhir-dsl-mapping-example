package customexport.mii.bielefeld.experimental


import org.hl7.fhir.r4.model.ResearchSubject

import static de.kairos.fhir.centraxx.metamodel.RootEntities.consent
/**
 * Represented by HDRP StudyMember
 * Specified by https://simplifier.net/medizininformatikinitiative-modulperson/probantin
 * @author Jonas Küttner
 * @since v.1.38.0, HDRP.v.2024.4.0
 *
 *
 */
researchSubject {

  if(!context.source[consent().consentType().flexiStudy()])
    return

  if(!context.source[consent().consentType().flexiStudy().profile()])
    return

  if(context.source[consent().consentType().flexiStudy().status()] != "APPROVED")
    return

  final def studyOid = context.source[consent().id()]

  meta {
    profile "https://www.uni-bielefeld.de/fhir/ResearchSubject/StructureDefinition/data-usage-project"
  }

  status = ResearchSubject.ResearchSubjectStatus.CANDIDATE


  id = "ResearchSubject/data-usage-project-" + studyOid

  identifier {
    type {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "ANON"
      }
    }
    system = context.source[consent().consentType().flexiStudy().profile().code()]
    value = context.source[consent().consentType().flexiStudy().code()]
  }

 // sonst will er die auf dem Blaze kennen. Aber hab noch kein Profil dafür:
 // study {
 //   reference = "ResearchStudy/" + context.source[consent().consentType().flexiStudy().id()]
 // }

  individual {
    reference = "Patient/" + context.source[consent().patientContainer().id()]
  }

  consent {
    reference = "Consent/" + context.source[consent().id()]
  }

  final def memberFrom = context.source[consent().signedOn().date()]
  final def memberUntil = context.source[consent().validUntil().date()]
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
