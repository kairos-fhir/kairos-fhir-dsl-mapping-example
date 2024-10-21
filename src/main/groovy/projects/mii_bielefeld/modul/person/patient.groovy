package projects.mii_bielefeld.modul.person

import de.kairos.fhir.centraxx.metamodel.Country
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.InsuranceCompany
import de.kairos.fhir.centraxx.metamodel.PatientAddress
import de.kairos.fhir.centraxx.metamodel.PatientInsurance
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.CoverageType

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

/**
 * represented by CXX Patient
 * Export of address data requires the rights to export clear data.
 * @author Jonas Küttner
 * @since v.1.40.0, CXX.v.2024.4.0
 */

patient {

  id = "Patient/" + context.source[patient().patientContainer().id()]

  println(context.source)

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient"
  }

  // PKV insurance identifier

  final def gkvInsurance = context.source[patient().patientContainer().patientInsurances()]?.find {
    CoverageType.T == it[PatientInsurance.COVERAGE_TYPE] as CoverageType
  }

  println(gkvInsurance)

  // id now completely configured by insurance.
  if (gkvInsurance) {
    println("GKV Insurance")
    identifier {
      use = "official"
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
          system = "http://fhir.de/NamingSystem/arge-ik/iknr"
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
      use = gkvInsurance ? "secondary" : "official"
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
          value = gkvInsurance[PatientInsurance.INSURANCE_COMPANY]?.getAt(InsuranceCompany.COMPANY_ID) as String
        }
      }
    }
  }

  // Pid (Patient.identifier:pid)
  // identifier.system is hospital specific
  final def pidContainer = context.source[patient().patientContainer().idContainer()].find { final def idc ->
    "Patient.identifier:pid" == idc[IdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE]
  }

  if (pidContainer) {
    identifier {
      type {
        coding {
          system = "http://terminology.hl7.org/CodeSystem/v2-0203"
          code = "MR"
        }
      }
      system = "Site-specific-hospital" // needs to be configured
      value = pidContainer[IdContainer.PSN]
    }
  }

  humanName {
    use = "official"
    family = context.source[patient().lastName()]
    given = List.of(context.source[patient().firstName()] as String)
  }

  if (context.source[patient().birthName()]) {
    humanName {
      use = "maiden"
      family = context.source[patient().birthName()]
      given = List.of(context.source[patient().firstName()] as String)
    }
  }

  gender {
    value = toGender(context.source[patient().genderType()])
    if (value.toString() == "other") {
      extension {
        url = "http://fhir.de/StructureDefinition/gender-amtlich-de"
        valueCoding {
          system = "http://fhir.de/CodeSystem/gender-amtlich-de"
          code = "D"
          display = "divers"
        }
      }
    }
  }

  birthDate = normalizeDate(context.source[patient().birthdate().date()] as String)

  final def dateOfDeath = context.source[patient().dateOfDeath()]

  if (dateOfDeath) {
    deceasedBoolean = true
    deceasedDateTime = normalizeDate(dateOfDeath[PrecisionDate.DATE] as String)
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
  println("done")
}

static String getLineString(final Map address) {
  final def keys = [PatientAddress.STREET, PatientAddress.STREETNO]
  final def addressParts = keys.collect { return address[it] }.findAll()
  return addressParts.findAll() ? addressParts.join(" ") : null
}

static def toGender(final Object cxx) {
  switch (cxx) {
    case 'MALE':
      return "male"
    case 'FEMALE':
      return "female"
    case 'UNKNOWN':
      return "unknown"
    default:
      return "other"
  }
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

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}
