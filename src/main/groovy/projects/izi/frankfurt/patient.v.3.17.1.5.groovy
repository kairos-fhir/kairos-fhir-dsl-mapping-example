package projects.izi.frankfurt
/**
 * Represented by a CXX PatientMasterDataAnonymous
 * @author Franzy Hohnstaedter, Mike Wähnert
 * @since v.1.5.0, CXX.v.3.17.1.5
 */
patient {

  id = "Patient/" + context.source["patientcontainer.idContainer.id"]

  final def idContainer = context.source["patientcontainer.idContainer"]?.find {
    "SID" == it["idContainerType"]?.getAt("code")
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
    gender = mapGender(context.source["genderType"])
  }
  birthDate = normalizeDate(context.source["birthdate.date"] as String)
  deceasedDateTime = "UNKNOWN" != context.source["dateOfDeath.precision"] ?
      context.source["dateOfDeath.date"] : null
  generalPractitioner {
    identifier {
      value = "FRANKFURT"
    }
  }

}

static def mapGender(final Object genderType) {
  switch (genderType) {
    case "MALE": return "male"
    case "FEMALE": return "female"
    case "UNKNOWN": return "unknown"
    default: return "other"
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
}
