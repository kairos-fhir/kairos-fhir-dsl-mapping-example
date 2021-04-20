package projects.gecco

import de.kairos.centraxx.common.types.GenderType
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * Defined by https://simplifier.net/forschungsnetzcovid-19/patient
 * @author Lukas Reinert
 * @since CXX.v.3.17.0.2
 */
patient {

  id = "Patient/" + context.source[patient().patientContainer().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/Patient"
  }

  context.source[patient().patientContainer().ethnicities()]?.each { final ethn ->
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

    if (context.source[patient().birthdate().date()]) {
      extension {
        url = "age"
        valueAge {
          //TODO: Calculate age between birthdate and dateOfDeath if exists.
          value = computeAge(context.source[patient().birthdate().date()] as String)
          system = "http://unitsofmeasure.org"
          code = "a"
          unit = "years"
        }
      }
    }
  }

  active = context.source[patient().patientContainer().patientStatus()]
  if (context.source["genderType"]) {
    gender = mapGender(context.source[patient().genderType()] as GenderType)
  }
  birthDate = normalizeDate(context.source[patient().birthdate().date()] as String)
  deceasedDateTime = "UNKNOWN" != context.source[patient().dateOfDeath().precision()] ? normalizeDate(context.source[patient().dateOfDeath().date()] as String) : null


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




