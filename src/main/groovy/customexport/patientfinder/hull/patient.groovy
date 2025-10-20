package customexport.patientfinder.hull

import de.kairos.fhir.centraxx.metamodel.Country
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
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
 * Represented by a HDRP PatientMasterDataAnonymous
 * Specified: http://www.hl7.org/fhir/us/core/StructureDefinition-us-core-patient.html
 * @author Mike Wähnert
 * @since v.1.32.0, HDRP.v.2024.2.1
 */
patient {

  final def nhsIdc = context.source[patientMasterDataAnonymous().patientContainer().idContainer()]
      .find { final def idc -> idc[ID_CONTAINER_TYPE][CODE] == "NHS" }

  if (nhsIdc == null) {
    return
  }

  id = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]


  final def countryOfBirth = context.source[patientMasterDataAnonymous().addresses()].find { final def ad ->
    (ad["addressId"] as String).startsWith("countryOfBirth")
  }

  if (countryOfBirth != null){
    extension {
      url = "https://fhir.iqvia.com/patientfinder/extension/place-of-birth"
      valueAddress {
        city = countryOfBirth[PatientAddress.CITY]
        country = countryOfBirth[PatientAddress.COUNTRY]
      }
    }
  }

  identifier {
    system = "https://fhir.iqvia.com/patientfinder/CodeSystem/PatientID"
    value = nhsIdc[PSN]
  }

  context.source[patientMasterDataAnonymous().patientContainer().idContainer()].findAll { final def idc ->
    idc[ID_CONTAINER_TYPE][CODE] != "NHS"
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

  context.source[patient().addresses()].findAll {
    final  def ad -> !(ad["addressId"] as String).startsWith("countryOfBirth")
  }.each { final def ad ->
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

static Map<String, Object> getLflvMap(final def mapping, final Map<String, String> types) {
  final Map<String, Object> lflvMap = [:]
  if (!mapping) {
    return lflvMap
  }

  types.each { final String lvCode, final String lvType ->
    final def lflvForLv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == lvCode
    }

    if (lflvForLv && lflvForLv[lvType]) {
      lflvMap[(lvCode)] = lflvForLv[lvType]
    }
  }
  return lflvMap
}
