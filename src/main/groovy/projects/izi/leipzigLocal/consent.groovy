package projects.izi.leipzigLocal

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType

import java.text.SimpleDateFormat

import static de.kairos.fhir.centraxx.metamodel.RootEntities.consent
import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Consent
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.16.0, CXX.v.2022.2.0
 * @since v.3.18.3.19, 3.18.4, 2023.6.2, 2024.1.0 CXX can import the data absence reason extension to represent the UNKNOWN precision date
 * HINT: binary file attachments are not needed and not supported yet.
 */
consent {

  final Map<String, String> localToCentralType = [
      //Frankfurt ITMP => Leipzig IZI Central
      "CIMD EINWILLIGUNG"      : "CIMD_Consent",
      "BB_EINWILLIGUNG"        : "Broad_Consent",
      "ACC_ EINWILLIGUNG"      : "Study_Consent", // the space is intended!
      "SIL_EINWILLIGUNG"       : "Study_Consent",
      "AG_EINWILLIGUNG"        : "Study_Consent",
      "ORG_EINWILLIGUNG"       : "Study_Consent",
      // Hannover HUB => Leipzig IZI Central (Broad_Consent is the default for all other local consent types.)
      "ConsentCIMD"            : "CIMD_Consent", // same for IZI local
      "ConsentDefaultStudy"    : "Study_Consent",
      //Leipzig IZI Local => Leipzig IZI Central
      "BC"                     : "Broad_Consent",
      "ABGESTUFTE_EINWILLIGUNG": "Study_Consent"]

  final String localConsentTypeCode = context.source[consent().consentType().code()]
  final String centralConsentTypeCode = localToCentralType.get(localConsentTypeCode)
  if (centralConsentTypeCode == null) {
    return // no export
  }

  final def validFrom = context.source[consent().validFrom().date()]
  if (validFrom == null) {
    return // no export with empty or unknown date
  }

  id = "Consent/Consent-" + context.source[consent().id()]

  final def patIdContainer = context.source[diagnosis().patientContainer().idContainer()]?.find {
    "SID" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  if (patIdContainer) {
    patient {
      identifier {
        value = patIdContainer[IdContainer.PSN]
        type {
          coding {
            system = "urn:centraxx"
            code = patIdContainer[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE) as String
          }
        }
      }
    }
  }

  final def validUntil = context.source[consent().validUntil().date()]

  provision {
    period {
      start = validFrom
      if ("UNKNOWN" == context.source[consent().validUntil().precision()]) {
        end {
          extension {
            url = FhirUrls.Extension.FhirDefaults.DATA_ABSENT_REASON
            valueCode = "unknown"
          }
        }
      } else {
        end = validUntil
      }
    }

    purpose {
      system = FhirUrls.System.Consent.Type.BASE_URL
      code = centralConsentTypeCode
    }
  }

  final boolean isDeclined = context.source[consent().declined()]
  final boolean isCompleteRevoked = context.source[consent().revocation()] != null && !context.source[consent().revocation().revokePartsOnly()]
  final String interpretedStatus = getStatus(isDeclined, isCompleteRevoked, validUntil as String)
  status = interpretedStatus

  final boolean hasFlexiStudy = context.source[consent().consentType().flexiStudy()] != null
  scope {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/consentscope"
      code = hasFlexiStudy ? "research" : "patient-privacy"
    }
  }

  category {
    coding {
      system = "http://loinc.org"
      code = "59284-0" // Patient Consent
    }
  }

  dateTime {
    date = context.source[consent().creationDate()]
  }

  policyRule {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/v3-ActCode"
      code = "OPTINR" // opt-in with restrictions
    }
  }

  if (context.source[consent().signedOn()]) {
    verification {
      verified = true
      verificationDate {
        if ("UNKNOWN" == context.source[consent().signedOn().precision()]) {
          extension {
            url = FhirUrls.Extension.FhirDefaults.DATA_ABSENT_REASON
            valueCode = "unknown"
          }
        } else {
          date = context.source[consent().signedOn().date()]
        }
      }
    }
  }

  extension {
    url = FhirUrls.Extension.Consent.NOTES
    valueString = context.source[consent().notes()]
  }


  if (context.source[consent().revocation()] && context.source[consent().revocation().notes()]) {
    extension {
      url = FhirUrls.Extension.Consent.Revocation.REVOCATION_NOTES
      valueString = context.source[consent().revocation().notes()]
    }
  }
}

static String getStatus(final boolean isDeclined, final boolean isCompleteRevoked, final String validUntilDate) {
  if (isDeclined) {
    return "rejected"
  }

  if (isCompleteRevoked) {
    return "inactive"
  }

  if (!validUntilDate) {
    return "active"
  }

  final Date fromDate = new SimpleDateFormat("yyyy-MM-dd").parse(validUntilDate.substring(0, 10))
  final Date currDate = new Date()
  final int res = currDate <=> (fromDate)
  return res == 1 ? "inactive" : "active"
}
