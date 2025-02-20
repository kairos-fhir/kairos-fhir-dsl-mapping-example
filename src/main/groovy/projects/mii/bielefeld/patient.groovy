package projects.mii.bielefeld

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Country
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.InsuranceCompany
import de.kairos.fhir.centraxx.metamodel.PatientAddress
import de.kairos.fhir.centraxx.metamodel.PatientInsurance
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.CoverageType
import de.kairos.fhir.centraxx.metamodel.enums.GenderType
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient
/**
 * represented by CXX Patient
 * Export of address data requires the rights to export clear data.
 * @author Jonas Küttner
 * @since v.1.43.0, CXX.v.2024.5.0
 */

patient {

  id = "Patient/" + context.source[patient().patientContainer().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient"
  }

  // PKV insurance identifier

  final def gkvInsurance = context.source[patient().patientContainer().patientInsurances()]?.find {
    CoverageType.T == it[PatientInsurance.COVERAGE_TYPE] as CoverageType
  }

  // id now completely configured by insurance.
  if (gkvInsurance) {

    identifier {
      use = Identifier.IdentifierUse.OFFICIAL
      type {
        coding {
          system = "http://fhir.de/CodeSystem/identifier-type-de-basis"
          code = "GKV"
        }
      }
      system = "http://fhir.de/sid/gkv/kvid-10"
      value = gkvInsurance[PatientInsurance.POLICE_NUMBER]
      assigner {
        identifier {
          system = "http://fhir.de/sid/arge-ik/iknr"
          value = gkvInsurance[PatientInsurance.INSURANCE_COMPANY]?.getAt(InsuranceCompany.COMPANY_ID) as String
        }
      }
    }
  }

  final def pkvInsurance = context.source[patient().patientContainer().patientInsurances()]?.find {
    CoverageType.C == it[PatientInsurance.COVERAGE_TYPE] as CoverageType || CoverageType.P == it[PatientInsurance.COVERAGE_TYPE] as CoverageType
  }

  // PKV insurance identifier

  if (pkvInsurance) {
    identifier {
      use = gkvInsurance ? Identifier.IdentifierUse.SECONDARY : Identifier.IdentifierUse.SECONDARY
      type {
        coding {
          system = "http://fhir.de/CodeSystem/identifier-type-de-basis"
          code = "PKV"
        }
      }
      value = pkvInsurance[PatientInsurance.POLICE_NUMBER]
      assigner {
        identifier {
          system = "http://fhir.de/NamingSystem/arge-ik/iknr"
          value = pkvInsurance[PatientInsurance.INSURANCE_COMPANY]?.getAt(InsuranceCompany.COMPANY_ID) as String
        }
      }
    }
  }

  // Pid (Patient.identifier:pid)
  // export all identifiers
  context.source[patient().patientContainer().idContainer()].each { final def idContainer ->
    identifier {
      type {
        coding {
          system = "http://fhir.de/CodeSystem/identifier-type-de-basis"
          code = "MR"
        }
        coding {
          system = FhirUrls.System.IdContainerType.BASE_URL
          code = idContainer[IdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE] as String
        }
      }
      system = "https://fhir.centraxx.de/system/idContainer/psn"
      value = idContainer[IdContainer.PSN]
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

  final GenderType genderType = context.source[patient().genderType()] as GenderType
  gender {
    if (genderType == GenderType.MALE) {
      value = Enumerations.AdministrativeGender.MALE
    } else if (genderType == GenderType.FEMALE) {
      value = Enumerations.AdministrativeGender.MALE
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

