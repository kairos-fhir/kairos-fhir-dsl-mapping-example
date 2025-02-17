package projects.mii.bielefeld

import de.kairos.fhir.centraxx.metamodel.ConsentPolicy
import de.kairos.fhir.centraxx.metamodel.ConsentableAction
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import de.kairos.fhir.centraxx.metamodel.RootEntities
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Consent

import static de.kairos.fhir.centraxx.metamodel.RootEntities.consent

enum RevokationStatus {
  NOT_REVOKED,
  COMPLETE,
  PARTIAL
}

final Map<RevokationStatus, String> consentCodeMap = [
    (RevokationStatus.NOT_REVOKED): "2.16.840.1.113883.3.1937.777.24.2.1790",
    (RevokationStatus.PARTIAL)    : "2.16.840.1.113883.3.1937.777.24.2.2719",
    (RevokationStatus.COMPLETE)   : "2.16.840.1.113883.3.1937.777.24.2.2718"
]

// defines cxx codes and corresponding mii valuesets
final Map<String, String> consentMiiCodeMap = [
    m_bc_patdat   : "2.16.840.1.113883.3.1937.777.24.5.3.1",
    m_bc_ins_retro: "2.16.840.1.113883.3.1937.777.24.5.3.10",
    m_bc_ins_prosp: "2.16.840.1.113883.3.1937.777.24.5.3.14",
    m_bc_recon_res: "2.16.840.1.113883.3.1937.777.24.5.3.26",
    m_bc_recon_med: "2.16.840.1.113883.3.1937.777.24.5.3.30"
]

consent {
  id = context.source[consent().id()]


  category {
    coding {
      system = "http://loinc.org"
      code = "57016-8"
    }
    coding {
      system = "https://www.medizininformatik-initiative.de/fhir/modul-consent/CodeSystem/mii-cs-consent-consent_category"
      code = "2.16.840.1.113883.3.1937.777.24.2.184"
    }
  }

  scope {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/consentscope"
      code = "research"
    }
  }

  status = context.source[consent().declined()] ? Consent.ConsentState.REJECTED : Consent.ConsentState.ACTIVE

  patient {
    reference = "Patient/" + context.source[consent().patientContainer().id()]
  }

  dateTime {
    date = context.source[consent().creationDate()]
  }


  final String miiCode = getUri(context, consentCodeMap)

  if (miiCode != null) {
    policy {
      uri = miiCode
    }
  }

  final List allPolicies = (context.source[consent().consentElements()] as List).isEmpty() ?
      context.source[consent().consentType().policies()] as List :
      context.source[consent().consentElements()] as List

  final List miiPolicies = allPolicies.findAll {
    final def cxxPol -> consentMiiCodeMap.containsKey(cxxPol[ConsentPolicy.CONSENTABLE_ACTION][ConsentableAction.CODE])
  }

  if (!miiPolicies.empty && !context.source[consent().declined()]) {
    provision {
      type = Consent.ConsentProvisionType.DENY

      period {
        start = context.source[consent().validFrom().date()]
        if (context.source[consent().validUntil()] && context.source[consent().validUntil().date()]) {
          end = context.source[consent().validUntil().date()]
        }
      }

      provision {
        type = Consent.ConsentProvisionType.PERMIT

        period {
          start = context.source[consent().validFrom().date()]
          if (context.source[consent().validUntil()] && context.source[consent().validUntil().date()]) {
            end = context.source[consent().validUntil().date()]
          }
        }

        for (final def cxxPol : miiPolicies) {
          code {
            coding {
              system = "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3"
              code = consentMiiCodeMap[cxxPol[ConsentPolicy.CONSENTABLE_ACTION][ConsentableAction.CODE] as String]
              display = cxxPol[ConsentPolicy.CONSENTABLE_ACTION][ConsentableAction.MULTILINGUALS].find { final def me ->
                me[MultilingualEntry.LANG] == "de"
              }?.getAt(MultilingualEntry.VALUE)
            }
          }
        }
      }
    }

  }
}

private static String getUri(final Context context, final Map<RevokationStatus, String> consentCodeMap) {


  if (context.source[consent().revocation()] == null) {
    return consentCodeMap[RevokationStatus.NOT_REVOKED]
  }

  if (context.source[consent().revocation().revokePartsOnly()]) {
    return consentCodeMap[RevokationStatus.PARTIAL]
  }

  return consentCodeMap[RevokationStatus.COMPLETE]
}