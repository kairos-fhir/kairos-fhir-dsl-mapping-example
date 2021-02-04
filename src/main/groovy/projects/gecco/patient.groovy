package projects.gecco

import de.kairos.centraxx.common.types.GenderType
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * Defined by https://simplifier.net/forschungsnetzcovid-19/patient
 * @author Lukas Reinert
 */
patient {

  id = "Patient/" + context.source["patientcontainer.id"]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/Patient"
  }

  context.source["patientcontainer.ethnicities"]?.each { final ethn ->
    extension {
      url = "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/ethnic-group"
      valueCoding {
        system = "http://snomed.info/sct"
        code = mapEthnicityCode(ethn["code"] as String)
      }
    }
  }

  extension {
    url = "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/age"
    extension {
      url = "dateTimeOfDocumentation"
      valueDateTime = new Date().format("yyyy-MM-dd") // Interpreted as date when age is calculated (date of export)
    }

    if (context.source["birthdate.date"]) {
      extension {
        url = "age"
        valueAge {
          //TODO: Calculate age between birthdate and dateOfDeath if exists.
          valueDecimal = computeAge(context.source["birthdate.date"] as String)
          system = "http://unitsofmeasure.org"
          code = "a"
          unit = "years"
        }
      }
    }
  }

  final def localId = context.source["patientcontainer.idContainer"]?.find {
    "Lokal" == it["idContainerType"]?.getAt("code") // TODO: site specific
  }

  if (localId) {
    identifier {
      value = localId["psn"]
      type {
        coding {
          //system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS"
          code = "Lokal" // TODO: site specific
        }
      }
    }
  }

  final def globalId = context.source["patientcontainer.idContainer"]?.find {
    "DKTK" == it["idContainerType"]?.getAt("code") // TODO: site specific
  }

  if (globalId) {
    identifier {
      value = globalId["psn"]
      type {
        coding {
          //system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS"
          code = "Global" // TODO: site specific
        }
      }
    }
  }


  active = context.source["PatientStatus"]
  // name =
  // telecom = PatientAddress
  if (context.source["genderType"]) {
    gender = mapGender(context.source["genderType"] as GenderType)
  }
  birthDate = normalizeDate(context.source["birthdate.date"] as String)
  deceasedDateTime = "UNKNOWN" != context.source["dateOfDeath.precision"] ? normalizeDate(context.source["dateOfDeath.date"] as String) : null
  // deceasedBoolean =
  // address = context.source["PatientAddress"]


}


static AdministrativeGender mapGender(final GenderType genderType) {
  switch (genderType) {
    case null:
      return null
    case GenderType.MALE:
      return AdministrativeGender.MALE
    case GenderType.FEMALE:
      return AdministrativeGender.FEMALE
    case GenderType.UNKNOWN:
      return AdministrativeGender.UNKNOWN
    default:
      return AdministrativeGender.OTHER
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
}


//Compute age of patient from birthdate
static int computeAge(final String dateString) {
  final int doe = dateString.substring(0, 4).toInteger()
  final int now = Calendar.getInstance().get(Calendar.YEAR)
  return now - doe
}

//Function to map ethnicities
static String mapEthnicityCode(final String ethnicity) {
  switch (ethnicity) {
    case "ETHN_CAUCASIAN":
      return "14045001"
    case "ETHN_AFRICAN":
      return "18167009"
    case "ETHN_ASIAN":
      return "315280000"
    case "ETHN_ARAB":
      return "90027003"
    case "ETHN_OTHER":
      return "186019001"
    default:
      return "186019001"
  }
}




