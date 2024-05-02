package projects.mii.modul.person

import de.kairos.fhir.centraxx.metamodel.ContactAddress
import de.kairos.fhir.centraxx.metamodel.Country
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.InsuranceCompany
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.PatientAddress
import de.kairos.fhir.centraxx.metamodel.PatientInsurance
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.CoverageType

import static de.kairos.fhir.centraxx.metamodel.Multilingual.LANGUAGE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

/**
 * represented by CXX Patient
 * Export of address data requires the rights to export clear data.
 * @author Jonas Küttner
 * @since v.1.8.0, CXX.v.3.18.1
 */


patient {
  id = "Patient/" + context.source[patient().patientContainer().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient"
  }

  final def idContainer = context.source[patient().patientContainer().idContainer()].find {
    "SAPID" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  if (idContainer) {
    identifier {
      use = "official"
      type {
        coding {
          system = "http://terminology.hl7.org/CodeSystem/v2-0203"
          code = "MR"
        }
        value = idContainer[IdContainer.PSN]
      }
      system = "https://www.medizininformatik-initiative.de/fhir/core/NamingSystem/patientOld-identifier"
      assigner {
        identifier {
          system = "https://www.medizininformatik-initiative.de/fhir/core/CodeSystem/core-location-identifier"
          value = "UMG"
        }
      }
    }
  }

  final def gkvInsurance = context.source[patient().patientContainer().patientInsurances()]?.find {
    CoverageType.T == it[PatientInsurance.COVERAGE_TYPE]
  }

  if (gkvInsurance) {
    identifier {
      use = "official"
      type {
        coding {
          system = "http://fhir.de/CodeSystem/identifier-type-de-basis"
          code = "GKV"
        }
      }
      system = "http://fhir.de/NamingSystem/gkv/kvid-10"
      value = gkvInsurance[PatientInsurance.POLICE_NUMBER]
      assigner {
        identifier {
          use = "official"
          system = "http://fhir.de/NamingSystem/arge-ik/iknr"
          value = gkvInsurance[PatientInsurance.INSURANCE_COMPANY]?.getAt(InsuranceCompany.COMPANY_ID) as String
        }
      }
    }
  }

  final def pkvInsurance = context.source[patient().patientContainer().patientInsurances()]?.find {
    CoverageType.C == it[PatientInsurance.COVERAGE_TYPE] || CoverageType.P == it[PatientInsurance.COVERAGE_TYPE]
  }

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
        display = pkvInsurance[PatientInsurance.INSURANCE_COMPANY]?.getAt(InsuranceCompany.CONTACT_ADDRESS)?.getAt(ContactAddress.INSTITUTE)
      }
    }
  }

  active = context.source[patient().patientContainer().patientStatus()]

  final def contacts = [context.source[patient().addresses().email()],
                        context.source[patient().addresses().phone1()],
                        context.source[patient().addresses().phone2()],
                        context.source[patient().addresses().mobile()],
                        context.source[patient().addresses().fax()]
  ].collect {
    it as List
  }

  final def contactInfo = ["email", "phone", "phone", "phone", "fax"]

  contacts.eachWithIndex { contact, i ->
    if (contact?.first() != null) {
      telecom {
        system = contactInfo[i]
        value = contact.first() as String
      }
    }
  }


  humanName {
    use = "official"
    family = context.source[patient().lastName()]
    given context.source[patient().firstName()] as String
    prefix context.source[patient().title().multilinguals()]?.find { it[LANGUAGE] == "de" }?.getAt(Multilingual.DESCRIPTION) as String
  }

  if (context.source[patient().birthName()]) {
    humanName {
      use = "maiden"
      family = context.source[patient().birthName()]
      given context.source[patient().firstName()] as String
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
    address {
      type = "physical"
      city = ad[PatientAddress.CITY]
      postalCode = ad[PatientAddress.ZIPCODE]
      country = ad[PatientAddress.COUNTRY]?.getAt(Country.ISO2_CODE)
      def lineString = getLineString(ad as Map)
      if (lineString) {
        line lineString
      }
    }
  }

  String maritalStatusCode = getMaritalStatusCode(context.source[patient().maritalStatus().code()])
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

static String getLineString(Map address) {
  def keys = [PatientAddress.STREET, PatientAddress.STREETNO]
  def addressParts = keys.collect { return address[it] }.findAll()
  return addressParts.findAll() ? addressParts.join(" ") : null
}

static def toGender(Object cxx) {
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

static String getMaritalStatusCode(maritalStatus) {
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
