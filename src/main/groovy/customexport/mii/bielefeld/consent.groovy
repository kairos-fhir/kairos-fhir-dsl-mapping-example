package customexport.mii.bielefeld

import de.kairos.fhir.centraxx.metamodel.ConsentPolicy
import de.kairos.fhir.centraxx.metamodel.ConsentableAction
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Consent

import static de.kairos.fhir.centraxx.metamodel.RootEntities.consent

enum RevokationStatus {
    NOT_REVOKED,
    COMPLETE,
    PARTIAL
}

final Map<RevokationStatus, String> consentCodeMap_1_6_d = [
        (RevokationStatus.NOT_REVOKED): "urn:oid:2.16.840.1.113883.3.1937.777.24.2.1790",
        (RevokationStatus.PARTIAL)    : "urn:oid:2.16.840.1.113883.3.1937.777.24.2.2719",
        (RevokationStatus.COMPLETE)   : "urn:oid:2.16.840.1.113883.3.1937.777.24.2.2718"
]

class PolicyComponent {
    String code
    String display

    PolicyComponent(String code, String display) {
        this.code = code
        this.display = display
    }
}

// defines cxx codes and corresponding mii valuesets
//final Map<String, String> consentMiiCodeMap = [
//    m_bc_patdat   : "2.16.840.1.113883.3.1937.777.24.5.3.1",
//    m_bc_ins_retro: "2.16.840.1.113883.3.1937.777.24.5.3.10",
//    m_bc_ins_prosp: "2.16.840.1.113883.3.1937.777.24.5.3.14",
//    m_bc_recon_res: "2.16.840.1.113883.3.1937.777.24.5.3.26",
//    m_bc_recon_med: "2.16.840.1.113883.3.1937.777.24.5.3.30"
//]
final Map<String, List<PolicyComponent>> consentMiiCodeMap = [
        m_bc_patdat   : [
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.1", "Patientendaten erheben, speichern, nutzen"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.2", "IDAT erheben"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.3", "IDAT speichern, verarbeiten"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.4", "IDAT zusammenfuehren Dritte"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.5", "IDAT bereitstellen EU DSGVO NIVEAU"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.6", "MDAT erheben"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.7", "MDAT speichern, verarbeiten"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.8", "MDAT wissenschaftlich nutzen EU DSGVO NIVEAU"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.9", "MDAT zusammenfuehren Dritte"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.37", "Rekontaktierung Ergebnisse erheblicher Bedeutung")
        ],
        m_bc_ins_retro: [
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.10", "Krankenkassendaten retrospektiv uebertragen, speichern, nutzen"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.11", "KKDAT 5J retrospektiv uebertragen"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.12", "KKDAT 5J retrospektiv speichern verarbeiten"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.13", "KKDAT 5J retrospektiv wissenschaftlich nutzen"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.38", "KKDAT 5J retrospektiv uebertragen KVNR")
        ],
        m_bc_ins_prosp: [
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.14", "KKDAT prospektiv uebertragen speichern nutzen"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.15", "KKDAT 5J prospektiv uebertragen"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.16", "KKDAT 5J prospektiv speichern verarbeiten"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.17", "KKDAT 5J prospektiv wissenschaftlich nutzen"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.39", "KKDAT 5J prospektiv uebertragen KVNR")
        ],
        m_bc_recon_res: [
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.26", "Rekontaktierung Ergänzungen"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.27", "Rekontaktierung Verknüpfung Datenbanken"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.28", "Rekontaktierung weitere Erhebung"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.29", "Rekontaktierung weitere Studien")
        ],
        m_bc_recon_med: [
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.30", "Rekontaktierung Zusatzbefund"),
                new PolicyComponent("2.16.840.1.113883.3.1937.777.24.5.3.31", "Rekontaktierung Zusatzbefund")
        ],
]

consent {
    id = "Consent/" + context.source[consent().id()]

    meta {
        profile "https://www.medizininformatik-initiative.de/fhir/modul-consent/StructureDefinition/mii-pr-consent-einwilligung"
    }

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

    if ((context.source[consent().consentType().code()] as String).contains("t_bc_16d")) {
        final String miiCode = getUri(context, consentCodeMap_1_6_d)

        if (miiCode != null) {
            policy {
                uri = miiCode
            }
        }
    }else {
        println("You need to update this script or have it updated. It is not suitable for ConstenType.Code " +
                context.source[consent().consentType().code()])
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
                    final def polComps = consentMiiCodeMap[cxxPol[ConsentPolicy.CONSENTABLE_ACTION][ConsentableAction.CODE] as String]
                    for (final def pc : polComps) {
                        code {
                            coding {
                                system = "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3"
                                code = pc.code
                                display = pc.display
                            }
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