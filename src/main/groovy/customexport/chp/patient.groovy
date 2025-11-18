package customexport.chp

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Country
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.OrganisationUnit
import de.kairos.fhir.centraxx.metamodel.PatientAddress
import de.kairos.fhir.centraxx.metamodel.enums.GenderType
import org.hl7.fhir.r4.model.ContactPoint

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.PatientMaster.GENDER_TYPE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * Represented by a HDRP PatientMasterDataAnonymous
 * @author Mike Wähnert
 * @since v.1.5.0, HDRP.v.3.17.1.5
 * @since v.2023.6.2, v.2024.1.0 HDRP can import the data absence reason extension to represent the UNKNOWN precision date
 */
patient {

  id = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]

  context.source[patientMasterDataAnonymous().patientContainer().idContainer()]?.each { final def idc ->
    identifier {
      value = idc[PSN]
      type {
        coding {
          system = FhirUrls.System.IdContainerType.BASE_URL
          code = idc[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
      }
    }
  }

  humanName {
    text = context.source[patient().firstName()] + " " + context.source[patient().lastName()]
    family = context.source[patient().lastName()]
    given(context.source[patient().firstName()] as String)
  }

  if (context.source[patient().birthName()]) {
    humanName {
      use = "maiden"
      family = context.source[patient().birthName()]
      given context.source[patient().firstName()] as String
    }
  }

  context.source[patient().addresses()]?.each { final ad ->
    address {
      type = "physical"
      city = ad[PatientAddress.CITY]
      postalCode = ad[PatientAddress.ZIPCODE]
      country = ad[PatientAddress.COUNTRY]?.getAt(Country.ISO2_CODE)
      final def lineString = getLineString(ad as Map)
      if (lineString) {
        line lineString
      }
    }
  }

  context.source[patient().addresses()]?.each { final ad ->

    if (ad[PatientAddress.PHONE1] != null) {
      telecom {
        system = ContactPoint.ContactPointSystem.PHONE
        value = ad[PatientAddress.PHONE1]
        rank = 1
      }
    }
    if (ad[PatientAddress.PHONE2] != null) {
      telecom {
        system = ContactPoint.ContactPointSystem.PHONE
        value = ad[PatientAddress.PHONE2]
        rank = 2
      }
    }
    if (ad[PatientAddress.MOBILE] != null) {
      telecom {
        system = ContactPoint.ContactPointSystem.PHONE
        value = ad[PatientAddress.MOBILE]
        rank = 3
      }
    }
    if (ad[PatientAddress.EMAIL] != null) {
      telecom {
        system = ContactPoint.ContactPointSystem.EMAIL
        value = ad[PatientAddress.EMAIL]
        rank = 4
      }
    }
    if (ad[PatientAddress.FAX] != null) {
      telecom {
        system = ContactPoint.ContactPointSystem.FAX
        value = ad[PatientAddress.FAX]
        rank = 5
      }
    }
  }

  if (context.source[GENDER_TYPE]) {
    gender = mapGender(context.source[GENDER_TYPE] as GenderType)
  }

  if (context.source[patientMasterDataAnonymous().maritalStatus()]) {
    maritalStatus {
      coding {
        system = FhirUrls.System.Patient.MaritalStatus.BASE_URL
        code = context.source[patientMasterDataAnonymous().maritalStatus().code()]
        display = context.source[patientMasterDataAnonymous().maritalStatus().multilinguals()].find { final def ml ->
          ml[Multilingual.LANGUAGE] == 'en' && ml[Multilingual.SHORT_NAME] == null
        }?.getAt(Multilingual.SHORT_NAME)
      }
    }
  }

  if (context.source[patientMasterDataAnonymous().birthdate()]) {
    birthDate {
      if ("UNKNOWN" == context.source[patientMasterDataAnonymous().birthdate().precision()]) {
        extension {
          url = FhirUrls.Extension.FhirDefaults.DATA_ABSENT_REASON
          valueCode = "unknown"
        }
      } else {
        date = context.source[patientMasterDataAnonymous().birthdate().date()]
        precision = TemporalPrecisionEnum.DAY.name()
      }
    }
  }

  if (context.source[patientMasterDataAnonymous().dateOfDeath()]) {
    deceasedDateTime {
      if ("UNKNOWN" == context.source[patientMasterDataAnonymous().dateOfDeath().precision()]) {
        extension {
          url = FhirUrls.Extension.FhirDefaults.DATA_ABSENT_REASON
          valueCode = "unknown"
        }
      } else {
        date = context.source[patientMasterDataAnonymous().dateOfDeath().date()] as String
        precision = TemporalPrecisionEnum.DAY.name()
      }
    }
  }

  context.source[patientMasterDataAnonymous().patientContainer().organisationUnits()].each { final def ou ->
    generalPractitioner {
      reference = "Organization/" + ou[OrganisationUnit.ID]
    }
  }
}

static def mapGender(final GenderType genderType) {
  switch (genderType) {
    case GenderType.MALE: return "male"
    case GenderType.FEMALE: return "female"
    case GenderType.UNKNOWN: return "unknown"
    default: return "other"
  }
}

static String getLineString(final Map address) {
  final def keys = [PatientAddress.STREET, PatientAddress.STREETNO]
  final def addressParts = keys.collect { return address[it] }.findAll()
  return addressParts.findAll() ? addressParts.join(" ") : null
}

