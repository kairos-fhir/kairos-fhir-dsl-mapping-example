package customexport.mii.bielefeld

import de.kairos.fhir.centraxx.metamodel.*
import de.kairos.fhir.centraxx.metamodel.enums.CoverageType
import de.kairos.fhir.centraxx.metamodel.enums.GenderType
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier

import javax.annotation.Nonnull
import javax.annotation.Nullable

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

/**
 * represented by HDRP Patient
 * Export of address data requires the rights to export clear data.
 * @author Jonas Küttner
 * @since v.1.43.0, HDRP.v.2024.5.0
 */

patient {
    id = "Patient/" + context.source[patient().patientContainer().id()]

//  // Please leave it in. It has not yet been conclusively clarified how this profile is generated.
//  final boolean isPseudo = (isBlank(context.source[patient().firstName()] as String) && isBlank(context.source[patient().firstName()] as String))
//
//  if (isPseudo) {
//    // pseudo patient
//
//    meta {
//      profile "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/PatientPseudonymisiert"
//    }
//
//    context.source[patient().patientContainer().idContainer()].each {
//      identifier {
//        type {
//          coding {
//            system = "http://terminology.hl7.org/CodeSystem/v3-ObservationValue"
//            code = "PSEUDED"
//          }
//        }
//      }
//    }
//
//    if (context.source[patient().birthdate()] && context.source[patient().birthdate().date()]) {
//      birthDate = roundDate(context.source[patient().birthdate().date()] as String)
//    }
//
//    context.source[patient().addresses()]?.each { final ad ->
//      address {
//        type = "both"
//        postalCode = ad[PatientAddress.ZIPCODE] as String
//        country = ad[PatientAddress.COUNTRY]?.getAt(Country.ISO2_CODE) as String
//      }
//    }
//  } else {
    // full patient
    meta {
        profile "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient"
    }

    // GKV insurance identifier
    final def gkvInsurance = context.source[patient().patientContainer().patientInsurances()]?.find {
        CoverageType.T == it[PatientInsurance.COVERAGE_TYPE] as CoverageType
    }

    // id now completely configured by insurance.
    def gkvOrPkv
    if (gkvInsurance) {
        identifier {
            use = Identifier.IdentifierUse.OFFICIAL
            type {
                coding {
                    system = "http://fhir.de/CodeSystem/identifier-type-de-basis"
                    code = "KVZ10"
                }
            }
            system = "http://fhir.de/sid/gkv/kvid-10"
            value = gkvInsurance[PatientInsurance.POLICE_NUMBER]
            assigner {
                identifier {
                    system = "http://fhir.de/sid/arge-ik/iknr"
                    value = gkvInsurance[PatientInsurance.INSURANCE_COMPANY]?.getAt(InsuranceCompany.COMPANY_ID) as String
                    gkvOrPkv = value
                }
            }
        }
    } else {

        final def pkvInsurance = context.source[patient().patientContainer().patientInsurances()]?.find {
            CoverageType.C == it[PatientInsurance.COVERAGE_TYPE] as CoverageType || CoverageType.P == it[PatientInsurance.COVERAGE_TYPE] as CoverageType
        }

        // PKV insurance identifier
        if (pkvInsurance) {
            identifier {
                use = Identifier.IdentifierUse.SECONDARY
                type {
                    coding {
                        system = "http://fhir.de/CodeSystem/identifier-type-de-basis"
                        code = "KVZ10"
                    }
                }
                system = "http://fhir.de/sid/gkv/kvid-10"
                value = pkvInsurance[PatientInsurance.POLICE_NUMBER]
                assigner {
                    identifier {
                        system = "http://fhir.de/sid/arge-ik/iknr"
                        value = pkvInsurance[PatientInsurance.INSURANCE_COMPANY]?.getAt(InsuranceCompany.COMPANY_ID) as String
                        gkvOrPkv = value
                    }
                }
            }
        }
    }

    final def psnCode = "Patient.identifier:PseudonymisierterIdentifier"
    final def gkvCode = "Patient.identifier:versichertenId_GKV"
    final def pkvCode = "Patient.identifier:versichertenId_pkv"

    // Pid (Patient.identifier:pid)
    // export all identifiers
    context.source[patient().patientContainer().idContainer()].each { final def idContainer ->

        final def usedCode = idContainer[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE) as String

        if (usedCode == pkvCode || usedCode == gkvCode) {
            // already taken into account with insurance policies
        } else if (usedCode == psnCode) {
            identifier {
                type {
                    coding {
                        system = "http://terminology.hl7.org/CodeSystem/v3-ObservationValue"
                        code = "PSEUDED"
                    }
                }
                system = "https://www.medizininformatik-initiative.de/fhir/sid/pseudonym"
                value = idContainer[IdContainer.PSN]
            }
        } else {
            identifier {
                type {
                    coding {
                        system = "http://fhir.de/CodeSystem/v2-0203"
                        code = "MR"
                    }
                }
                system = "https://fhir.centraxx.de/system/idContainer/psn"
                value = idContainer[IdContainer.PSN]
            }
        }
    }


    humanName {
        use = HumanName.NameUse.OFFICIAL
        family = context.source[patient().lastName()]
        given(context.source[patient().firstName()] as String)
    }

    if (context.source[patient().birthName()]) {
        humanName {
            use = HumanName.NameUse.MAIDEN
            family = context.source[patient().birthName()]
        }
    }


    if (context.source[patient().birthdate()] && context.source[patient().birthdate().date()]) {
        birthDate = context.source[patient().birthdate().date()]
    }

    final def dateOfDeath = context.source[patient().dateOfDeath()]

    if (dateOfDeath) {
        deceasedBoolean = true
        deceasedDateTime = dateOfDeath[PrecisionDate.DATE]
    }

    context.source[patient().addresses()]?.each { final ad ->
        if (ad[PatientAddress.STREET]) { // normal address, Postfach address, extensions could be added
            address {
                type = "both"
                city = ad[PatientAddress.CITY] as String
                postalCode = ad[PatientAddress.ZIPCODE] as String
                country = ad[PatientAddress.COUNTRY]?.getAt(Country.ISO2_CODE) as String
                line(getLineString(ad as Map))
            }
        } else if (ad[PatientAddress.PO_BOX]) { // Postfach address, extensions could be added
            address {
                type = "postal"
                city = ad[PatientAddress.CITY] as String
                postalCode = ad[PatientAddress.ZIPCODE] as String
                country = ad[PatientAddress.COUNTRY]?.getAt(Country.ISO2_CODE) as String
                line(ad[PatientAddress.PO_BOX] as String)
            }
        }
    }

    final String maritalStatusCode = getMaritalStatusCode(context.source[patient().maritalStatus().code()])
    if (maritalStatusCode) {
        maritalStatus {
            coding {
                system = "http://terminology.hl7.org/CodeSystem/v3-MaritalStatus"
                code = maritalStatusCode
            }
        }
    } else {
        maritalStatus {
            coding {
                system = "http://terminology.hl7.org/CodeSystem/v3-NullFlavor"
                code = "UNK"
            }
        }
    }
    //}

    final GenderType genderType = context.source[patient().genderType()] as GenderType

    gender {
        if (genderType == GenderType.MALE) {
            value = Enumerations.AdministrativeGender.MALE
        } else if (genderType == GenderType.FEMALE) {
            value = Enumerations.AdministrativeGender.FEMALE
        } else if (genderType == GenderType.UNKNOWN) {
            value = Enumerations.AdministrativeGender.UNKNOWN
        } else if (genderType == GenderType.UNDEFINED) {
            value = Enumerations.AdministrativeGender.OTHER
            extension {
                url = "http://fhir.de/StructureDefinition/gender-amtlich-de"
                valueString = "X"
            }
        } else if (genderType == GenderType.X) {
            value = Enumerations.AdministrativeGender.OTHER
            extension {
                url = "http://fhir.de/StructureDefinition/gender-amtlich-de"
                valueString = "D"
            }
        } else {
            value = Enumerations.AdministrativeGender.OTHER
        }
    }
}

static String getLineString(final Map address) {
    final def keys = [PatientAddress.STREET, PatientAddress.STREETNO]
    final def addressParts = keys.collect { return address[it] }.findAll()
    return addressParts.findAll() ? addressParts.join(" ") : null
}

static String getMaritalStatusCode(final maritalStatus) {
    if (!maritalStatus) {
        return null
    }
    switch (maritalStatus) {
        case "GS":
            return "D"
        case "LD":
            return "U"
        case "VH":
            return "M"
        case "VW":
            return "W"
        case "VP":
            return "T"
        default:
            return null
    }
}

@Nullable
static String roundDate(@Nullable final String fhirDate) {
    if (!fhirDate) {
        return null
    }

    // Extract the year, month, and day from the FHIR date string
    final def dateParts = fhirDate.split("-")
    final String year = dateParts[0]
    final String month = dateParts.length > 1 ? roundMonth(dateParts[1]) : null
    final String day = dateParts.length > 2 ? dateParts[2] : null

    // Round the month to the first month of the quarter

    if (day != null) {
        return "$year-$month-01"
    }
    if (month != null) {
        return "$year-$month"
    }
    return year
}

@Nonnull
static String roundMonth(@Nonnull final String month) {

    switch (month) {
        case "01":
            return "01"
        case "02":
            return "01"
        case "03":
            return "01"
        case "04":
            return "04"
        case "05":
            return "04"
        case "06":
            return "04"
        case "07":
            return "07"
        case "08":
            return "07"
        case "09":
            return "07"
        default:
            return "10"
    }
}

