package customexport.patientfinder.fnusa

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Country
import de.kairos.fhir.centraxx.metamodel.Ethnicity
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.PatientAddress
import de.kairos.fhir.centraxx.metamodel.enums.GenderType
import org.hl7.fhir.r4.model.codesystems.ContactPointSystem

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.IdContainerType.DECISIVE
import static de.kairos.fhir.centraxx.metamodel.Multilingual.LANGUAGE
import static de.kairos.fhir.centraxx.metamodel.Multilingual.NAME
import static de.kairos.fhir.centraxx.metamodel.PatientMaster.GENDER_TYPE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * Specified: http://www.hl7.org/fhir/us/core/StructureDefinition-us-core-patient.html
 * @author Mike Wähnert
 * @since v.1.32.0, CXX.v.2024.2.1
 */
patient {

  id = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]

  meta {
    profile "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient"
  }

  context.source[patientMasterDataAnonymous().patientContainer().idContainer()].each { final idContainer ->
    final boolean isDecisive = idContainer[ID_CONTAINER_TYPE]?.getAt(DECISIVE)
    if (isDecisive) {
      identifier {
        value = idContainer[PSN]
        type {
          coding {
            system = FhirUrls.System.IdContainerType.BASE_URL
            code = idContainer[ID_CONTAINER_TYPE]?.getAt(CODE)
          }
        }
      }
    }
  }

  humanName {
    use = "official"
    family = context.source[patient().lastName()]
    given context.source[patient().firstName()] as String
    prefix context.source[patient().title().multilinguals()]?.find { it[LANGUAGE] == "en" }?.getAt(Multilingual.DESCRIPTION) as String
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
    contact {
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

  final def firstEthnicity = context.source[patient().patientContainer().ethnicities()].find { final def ethnicity -> ethnicity != null }
  if (firstEthnicity != null) {
    extension {
      url = "http://hl7.org/fhir/us/core/StructureDefinition/us-core-ethnicity"
      extension {
        url = "text"
        valueString = firstEthnicity[Ethnicity.MULTILINGUALS].find { it[LANGUAGE] == "en"}?.getAt(NAME) as String
      }
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
