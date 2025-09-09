package customexport.bbmri

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * Specified by https://simplifier.net/bbmri.de/patient
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.2
 */
patient {

  id = "Patient/" + context.source["patientcontainer.id"]

  meta {
    profile "https://fhir.bbmri.de/StructureDefinition/Patient"
  }

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
    gender = mapGender(context.source["genderType"])
  }

  birthDate = normalizeDate(context.source["birthdate.date"] as String)
  deceasedDateTime = "UNKNOWN" != context.source["dateOfDeath.precision"] ? normalizeDate(context.source["dateOfDeath.date"] as String) : null
}

static def mapGender(final Object cxx) {
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

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
}
