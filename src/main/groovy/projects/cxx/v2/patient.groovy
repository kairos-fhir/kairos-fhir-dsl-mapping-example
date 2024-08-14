package projects.cxx.v2

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.enums.GenderType

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.PatientMaster.GENDER_TYPE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous
/**
 * Represented by a CXX PatientMasterDataAnonymous
 * @author Mike Wähnert
 * @since v.1.5.0, CXX.v.3.17.1.5
 * @since v.2023.6.2, v.2024.1.0 CXX can import the data absence reason extension to represent the UNKNOWN precision date
 */
patient {

  id = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]

  final def idContainer = context.source[patientMasterDataAnonymous().patientContainer().idContainer()]?.find {
    "COVID-19-PATIENTID" == it[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  if (idContainer) {
    identifier {
      value = idContainer[PSN]
      type {
        coding {
          system = "urn:centraxx"
          code = idContainer[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
      }
    }
  }
  if (context.source[GENDER_TYPE]) {
    gender = mapGender(context.source[GENDER_TYPE] as GenderType)
  }

  if (context.source[patientMasterDataAnonymous().birthdate()]) {
    birthDate {
      if ("UNKNOWN" == context.source[patientMasterDataAnonymous().birthdate().precision()]) {
        extension {
          url = FhirUrls.Extension.FhirDefaults.DATA_ABSENT_REASON
          valueCode = "unknown"
        }
      } else {
        date = normalizeDate(context.source[patientMasterDataAnonymous().birthdate().date()] as String)
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
        date = normalizeDate(context.source[patientMasterDataAnonymous().dateOfDeath().date()] as String)
        precision = TemporalPrecisionEnum.DAY.name()
      }
    }
  }

  generalPractitioner {
    identifier {
      value = "NUM_HUB"
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

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
}
