package projects.patientfinder.hull


import de.kairos.fhir.centraxx.metamodel.Country
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PatientAddress
import de.kairos.fhir.centraxx.metamodel.enums.GenderType
import org.hl7.fhir.r4.model.codesystems.ContactPointSystem

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.PatientMaster.GENDER_TYPE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.STRING_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

final String COUNTRY_OF_BIRTH = "countryofbirth"
final String PLACEOFBIRTH = "placeofbirth"
final String ADDRESS_CITY = "address.city"
final String ADDRESS_LINE = "address.line"
final String ADDRESS_PERIOD_END = "address.period.end"
final String ADDRESS_PERIOD_START = "address.period.start"
final String ADDRESS_POSTALCODE = "address.postalCode"
final String ADDRESS_USE = "address.use"

final Map PROFILE_TYPES = [
    (COUNTRY_OF_BIRTH)    : STRING_VALUE,
    (PLACEOFBIRTH)        : STRING_VALUE,
    (ADDRESS_CITY)        : STRING_VALUE,
    (ADDRESS_LINE)        : STRING_VALUE,
    (ADDRESS_PERIOD_END)  : STRING_VALUE,
    (ADDRESS_PERIOD_START): STRING_VALUE,
    (ADDRESS_POSTALCODE)  : STRING_VALUE,
    (ADDRESS_USE)         : STRING_VALUE
]

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * Specified: http://www.hl7.org/fhir/us/core/StructureDefinition-us-core-patient.html
 * @author Mike Wähnert
 * @since v.1.32.0, CXX.v.2024.2.1
 */
patient {

  final def nhsIdc = context.source[patientMasterDataAnonymous().patientContainer().idContainer()]
      .find { final def idc -> idc[ID_CONTAINER_TYPE][CODE] == "NHS" }

  if (nhsIdc == null) {
    return
  }

  id = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]

  meta {
    profile "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient"
  }

  final def patientMapping = context.source[medication().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == "Patient_profile"
  }

  final Map<String, Object> lflvPatientMap = getLflvMap(patientMapping, PROFILE_TYPES)

  if (lflvPatientMap.containsKey(PLACEOFBIRTH) || lflvPatientMap.containsKey(COUNTRY_OF_BIRTH)) {
    extension {
      url = "https://fhir.iqvia.com/patientfinder/extension/place-of-birth"

      valueAddress {
        city = lflvPatientMap.get(PLACEOFBIRTH)
        country = lflvPatientMap.get(COUNTRY_OF_BIRTH)
      }
    }
  }

  identifier {
    value = nhsIdc[PSN]
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

  final def addressMappings = context.source[patient().patientContainer().laborMappings()].findAll { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == "Address_profile"
  }

  final List<Map<String, Object>> lflvAddressMaps = addressMappings.collect { final def lm -> getLflvMap(lm, PROFILE_TYPES) }

  lflvAddressMaps.each { final Map<String, Object> addressMap ->
    address {
      line(addressMap.get(ADDRESS_LINE) as String)
      city = addressMap.get(ADDRESS_CITY) as String
      postalCode = addressMap.get(ADDRESS_POSTALCODE) as String
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
