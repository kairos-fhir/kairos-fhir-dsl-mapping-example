package customexport.mii.bielefeld

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.*
import de.kairos.fhir.centraxx.metamodel.enums.CatalogCategory
import de.kairos.fhir.centraxx.metamodel.enums.LaborFindingValueStatus
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Observation

import javax.annotation.Nullable

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFindingLaborValue
import static org.hl7.fhir.r4.model.Observation.ObservationStatus.*

// the code of the MII common measurement profile
final String laborMethodName = "MP_DiagnosticReportLab"

// the code of the FHIR DiagnosticReport.status laborValue
final String statusLvCode = "DiagnosticReport.status"

// the issued Date laborValue
final String issuedLvCode = "DiagnosticReport.issued"

// the identifier.assigner laborValue
final String assignerLvCode = "DiagnosticReport.identifier.assigner"

// von KB: 34487-9, 40982-1
final Set<String> influenzaCodes = [
        "34487-9", "60416-5", "49521-8", "61365-3", "48509-4",
        "29909-9", "40982-1", "100343-3", "100344-1", "100345-8",
        "100972-9", "100973-7", "100974-5", "101292-1", "101293-9",
        "101294-7", "101295-4", "101423-2", "101424-0", "101983-5",
        "104727-3", "104730-7", "104735-6", "105075-6", "105076-4",
        "105214-1", "105215-8", "105216-6", "105232-3", "105233-1",
        "105234-9", "105725-6", "39025-2", "39102-9", "39103-7",
        "44263-2", "44264-0", "44265-7", "44266-5", "49523-4",
        "49524-2", "49526-7", "49527-5", "49528-3", "49530-9",
        "49531-7", "49532-5", "49535-8", "49536-6", "49537-4",
        "50700-4", "50702-0", "50704-6", "50705-3", "50706-1",
        "50707-9", "50708-7", "50711-1", "50713-7", "53250-7",
        "53251-5", "55133-3", "55134-1", "55463-4", "55464-2",
        "55465-9", "55466-7", "56024-3", "57895-5", "57896-3",
        "57897-1", "57985-4", "59423-4", "59424-2", "60267-2",
        "60494-2", "60530-3", "60538-6", "61101-2", "62462-7",
        "62860-2", "68986-9", "68987-7", "72200-9", "72201-7",
        "74038-1", "74039-9", "74040-7", "74784-0", "74785-7",
        "74786-5", "74787-3", "76077-7", "76078-5", "76079-3",
        "76080-1", "77026-3", "77027-1", "77028-9", "77605-4",
        "80588-7", "80589-5", "80590-3", "80591-1", "81233-9",
        "81305-5", "81307-1", "81308-9", "81309-7", "81320-4",
        "81321-2", "81325-3", "81327-9", "81428-5", "82166-0",
        "82167-8", "82168-6", "82169-4", "82170-2", "82461-5",
        "85476-0", "85477-8", "85478-6", "85526-2", "85532-0",
        "85535-3", "86317-5", "86568-3", "86569-1", "86571-7",
        "86572-5", "87714-2", "87715-9", "87716-7", "88193-8",
        "88195-3", "88592-1", "88596-2", "88599-6", "88600-2",
        "88601-0", "88835-4", "90455-7", "90456-5", "90457-3",
        "90885-5", "90886-3", "91072-9", "91771-6", "92141-1",
        "92142-9", "92808-5", "92809-3", "92882-0", "92976-0",
        "92977-8", "93759-9", "93760-7", "93761-5", "93762-3",
        "93763-1", "94394-4", "94395-1", "94396-9", "95380-2",
        "95422-2", "95423-0", "95658-1", "95941-1", "97733-0",
        "99356-8", "99623-1", "88187-0", "29906-5", "29907-3",
        "38270-5", "38271-3", "38272-1", "44795-3", "49520-0"
] as Set<String>

// von KB: 94500-6, 96895-8
final Set<String> covidCodes = [
        "96957-6", "94306-8", "94640-0", "96765-3", "96763-8",
        "96986-5", "95409-9", "94760-6", "94533-7", "95425-5",
        "94766-3", "94316-7", "97098-8", "98132-4", "98494-8",
        "94559-2", "95824-9", "94639-2", "98131-6", "98493-0",
        "94534-5", "96120-1", "96123-5", "96091-4", "94314-2",
        "105749-6", "94759-8", "94845-5", "101289-7", "105748-8",
        "94767-1", "94641-8", "96448-6", "96958-4", "106617-4",
        "95406-5", "94565-9", "96797-6", "95608-6", "94500-6",
        "94660-8", "108180-1", "94309-2", "96829-7", "94756-4",
        "94757-2", "94307-6", "94308-4", "96895-8", "96741-4",
        "100156-9"
] as Set<String>

observation {

    if (!isExportable(context, laborMethodName, [statusLvCode, issuedLvCode, assignerLvCode])) {
        return
    }

    final def loincIdc = context.source[laborFindingLaborValue().crfTemplateField().laborValue().idContainers()].find { final def idc ->
        idc[IdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE] == "LOINC"
    }

    if (loincIdc == null) {
        return
    }

    final def loincCode = (loincIdc[IdContainer.PSN] as String).trim().toLowerCase()

    if (loincCode == null) {
        return
    }


    id = "Observation/" + context.source[laborFindingLaborValue().id()]

    // Create unique Id from LaborValue code and Lflv Oid
    // assigner not possible

    meta {
        profile "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/ObservationLab|2026.0.0"
    }

    identifier {
        type {
            coding {
                system = "http://terminology.hl7.org/CodeSystem/v2-0203"
                code = "OBI"
            }
        }
        system = "urn:centraxx/MessparameterCodeAndMesswertOid"
        value = context.source[laborFindingLaborValue().crfTemplateField().laborValue().code()] + "_" +
                context.source[laborFindingLaborValue().laborFinding().laborFindingId()]
        assigner {
            identifier {
                system = "https://www.medizininformatik-initiative.de/fhir/core/CodeSystem/core-location-identifier"
                value = "ukowl.de"
            }
        }
    }

    status(mapStatus(context.source[laborFindingLaborValue().status()] as LaborFindingValueStatus))

    category {
        coding {
            system = "http://loinc.org"
            code = "26436-6"
        }
        coding {
            system = "http://terminology.hl7.org/CodeSystem/observation-category"
            code = "laboratory"
        }
    }


    code {
        coding {
            system = "http://loinc.org"
            code = loincCode
        }

        coding {
            system = FhirUrls.System.LaborValue.BASE_URL
            code = context.source[laborFindingLaborValue().crfTemplateField().laborValue().code()] as String
        }
    }

    subject {
        reference = "Patient/" + context.source[laborFindingLaborValue().laborFinding().laborMappings()]
                .find()[LaborMapping.RELATED_PATIENT][PatientMaster.ID]
    }

    if (context.source[laborFindingLaborValue().recordedOn()] && context.source[laborFindingLaborValue().recordedOn().date()]) {
        effectiveDateTime = context.source[laborFindingLaborValue().recordedOn().date()]
    }

    final LaborValueDType dType = context.source[laborFindingLaborValue().crfTemplateField().laborValue().dType()] as LaborValueDType

    if (dType == LaborValueDType.DECIMAL || dType == LaborValueDType.INTEGER) {
        valueQuantity {
            value = context.source[laborFindingLaborValue().numericValue()]
            final def lvUnit = context.source[laborFindingLaborValue().crfTemplateField().laborValueDecimal().unit()]
            if (lvUnit) {
                system = "http://unitsofmeasure.org"
                code = lvUnit[Unity.CODE] as String
                unit = lvUnit[Unity.CODE] as String
            }
        }
    } else if (dType in [LaborValueDType.CATALOG, LaborValueDType.ENUMERATION, LaborValueDType.OPTIONGROUP]) {
        valueCodeableConcept {
            context.source[laborFindingLaborValue().catalogEntryValue()].each { final def ce ->
                coding {
                    system = createSystem(ce)
                    code = ce[CatalogEntry.CODE] as String
                }
            }
        }

        valueCodeableConcept {
            context.source[laborFindingLaborValue().multiValue()].each { final def ue ->
                coding {
                    system = "https://fhir.centraxx.de/system/catalogs/usageEntry"
                    code = ue[UsageEntry.CODE] as String
                }
            }
        }
    } else if (dType in [LaborValueDType.STRING, LaborValueDType.LONGSTRING]) {

        final def stringValue = context.source[laborFindingLaborValue().stringValue()] as String

        valueCodeableConcept {
            text = stringValue
            coding {
                if (influenzaCodes.contains(loincCode) || covidCodes.contains(loincCode)) {
                    //"https://www.medizininformatik-initiative.de/fhir/core/modul-labor/ValueSet/Laborergebnis-qualitativ"
                    system = "http://snomed.info/sct"
                    switch (stringValue?.trim()?.toLowerCase()) {
                        case "negativ":
                            code = "260385009"
                            display = "Negative (qualifier value)"
                            break

                        case "positiv":
                            code = "10828004"
                            display = "Positive (qualifier value)"
                            break

                        case "grenzwertig":
                            code = "280416009"
                            display = "Indeterminate result (qualifier value)"
                            break

                        default:
                            code = "419984006"
                            display = "Inconclusive (qualifier value)"
                            break
                    }
                } else {
                    system = "urn:centraxx:" + loincIdc[IdContainer.PSN] + "Answers"
                    code = stringValue
                }
            }
        }
    } else {
     //   println "Default element-type (LaborValueDType) not in script: " + dType
        dataAbsentReason {
            coding {
                system = "http://terminology.hl7.org/CodeSystem/data-absent-reason"
                code = "unsupported"
            }
        }
    }
}

static boolean isExportable(final Context context, final String methodCode, final List<String> lvCodes) {
    final def isMiiProfile = context.source[laborFindingLaborValue().laborFinding().laborMethod().code()] == methodCode

    final def isAdditionalDataLv = ((context.source[laborFindingLaborValue().crfTemplateField().laborValue().code()] as String) in lvCodes)

    return isMiiProfile && !isAdditionalDataLv
}

@Nullable
private static String createSystem(final Object catalogEntry) {

    final CatalogCategory category = catalogEntry[CatalogEntry.CATALOG][AbstractCustomCatalog.CATALOG_CATEGORY] as CatalogCategory

    switch (category) {
        case CatalogCategory.VALUELIST:
            return FhirUrls.System.Catalogs.VALUE_LIST + "/" + catalogEntry[CatalogEntry.CATALOG][AbstractCustomCatalog.CODE]
        case CatalogCategory.CUSTOM:
            return FhirUrls.System.Catalogs.CUSTOM_CATALOG + "/" + catalogEntry[CatalogEntry.CATALOG][AbstractCustomCatalog.CODE]

        default: return null
    }
}

private static Observation.ObservationStatus mapStatus(@Nullable final LaborFindingValueStatus cxxStatus) {
    if (cxxStatus == null) {
        return UNKNOWN
    }
    switch (cxxStatus) {
        case LaborFindingValueStatus.R:
            return REGISTERED
        case LaborFindingValueStatus.P:
            return PRELIMINARY
        case LaborFindingValueStatus.F:
            return FINAL
        case LaborFindingValueStatus.C:
            return CORRECTED
        case LaborFindingValueStatus.W:
            return ENTEREDINERROR
        case LaborFindingValueStatus.X:
            return CANCELLED
        default:
            return UNKNOWN
    }
}

