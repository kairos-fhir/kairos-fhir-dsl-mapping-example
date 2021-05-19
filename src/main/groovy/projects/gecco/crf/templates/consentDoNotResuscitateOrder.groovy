package projects.gecco.crf


import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import org.hl7.fhir.r4.model.Consent

import static de.kairos.fhir.centraxx.metamodel.RootEntities.consent

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/donotresuscitateorder
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */
consent {

  id = "Consent/" + context.source[consent().id()]

  meta {
    profile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/do-not-resuscitate-order")
  }


  status = context.source[consent().revocation()] ? Consent.ConsentState.REJECTED : Consent.ConsentState.ACTIVE

  scope {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/consentscope"
      code = "adr"
    }
  }

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/consentcategorycodes"
      code = "dnr"
    }
  }

  patient {
    reference = "Patient/" + context.source[consent().patientContainer().id()]
  }

  performer {
    reference = "Patient/" + context.source[consent().patientContainer().id()]
  }

  provision {

    type = Consent.ConsentProvisionType.PERMIT

    def validFrom = context.source[consent().validFrom()]
    if (validFrom) {
      period {
        start {
          date = validFrom[PrecisionDate.DATE]
        }

        def validUntil = context.source[consent().validUntil()]
        if (validUntil) {
          end {
            date = validUntil[PrecisionDate.DATE]
          }
        }
      }
    }

    code {
      coding {
        system = "urn:centraxx"
        code = context.source[consent().consentType().code()] as String
      }
    }

  }
}
