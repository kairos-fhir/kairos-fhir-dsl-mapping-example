package projects.cxx.v1

import de.kairos.fhir.centraxx.metamodel.enums.GenderType


/**
 * Represented by a CXX PatientMasterDataAnonymous
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.2
 */
patient {

  id = "Patient/" + context.source["patientcontainer.id"]

  final def idContainer = context.source["patientcontainer.idContainer"]?.find {
    "COVID-19-PATIENTID" == it["idContainerType"]?.getAt("code")
  }

  if (idContainer) {
    identifier {
      value = idContainer["psn"]
      type {
        coding {
          system = "urn:centraxx"
          code = idContainer["idContainerType"]?.getAt("code")
        }
      }
    }
  }
  if (context.source["genderType"]) {
    gender = mapGender(context.source["genderType"] as GenderType)
  }
  birthDate = normalizeDate(context.source["birthdate.date"] as String)
  deceasedDateTime = "UNKNOWN" != context.source["dateOfDeath.precision"] ? context.source["dateOfDeath.date"] : null
  generalPractitioner {
    identifier {
      value = "NUM_HUB"
    }
  }

}

static def mapGender(final GenderType genderType) {
  switch (genderType) {
    case GenderType.MALE:
      return "male"
    case GenderType.FEMALE:
      return "female"
    case GenderType.UNKNOWN:
      return "unknown"
    default:
      return "other"
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
}
