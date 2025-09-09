package customexport.patientfinder.digione

import de.kairos.fhir.centraxx.metamodel.Country
import de.kairos.fhir.centraxx.metamodel.PatientAddress
import de.kairos.fhir.centraxx.metamodel.enums.GenderType
import org.hl7.fhir.r4.model.codesystems.ContactPointSystem

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.PatientMaster.GENDER_TYPE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * @author Jonas Küttner
 */
patient {

  id = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]

  final def healthCareId = context.source[patientMasterDataAnonymous().patientContainer().idContainer()]
      .find { final def idc -> idc[ID_CONTAINER_TYPE][CODE] == "patient_ID" }

  identifier {
    system = "https://fhir.iqvia.com/patientfinder/CodeSystem/PatientID"
    value = healthCareId[PSN]
  }

  context.source[patientMasterDataAnonymous().patientContainer().idContainer()].findAll { final def idc ->
    idc[ID_CONTAINER_TYPE][CODE] != "patient_ID"
  }.each { final def idc ->
    identifier {
      system = "https://fhir.iqvia.com/patientfinder/CodeSystem/PersonalIdentifier"
      value = idc[PSN]
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

  if (context.source[GENDER_TYPE]) {
    gender = mapGender(context.source[GENDER_TYPE] as GenderType)
  }

  if (context.source[patientMasterDataAnonymous().birthdate().date()]) {
    birthDate = normalizeDate(context.source[patientMasterDataAnonymous().birthdate().date()] as String)
  }

  deceasedDateTime = "UNKNOWN" != context.source[patientMasterDataAnonymous().dateOfDeath().precision()] ?
      context.source[patientMasterDataAnonymous().dateOfDeath().date()] : null

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

    telecom {
      system = ContactPointSystem.PHONE.toCode()
      value = ad[PatientAddress.PHONE1]
    }
    telecom {
      system = ContactPointSystem.EMAIL.toCode()
      value = ad[PatientAddress.EMAIL]
    }
  }
}


static String getLineString(final Map address) {
  final def keys = [PatientAddress.STREET, PatientAddress.STREETNO]
  final def addressParts = keys.collect { return address[it] }.findAll()
  return addressParts.findAll() ? addressParts.join(" ") : null
}

static def mapGender(final GenderType genderType) {
  switch (genderType) {
    case GenderType.MALE: return "male"
    case GenderType.FEMALE: return "female"
    case GenderType.UNKNOWN: return "unknown"
    default: return "other"
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
}
