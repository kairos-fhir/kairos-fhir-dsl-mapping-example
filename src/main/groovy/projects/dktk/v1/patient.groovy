package projects.dktk.v1

import de.kairos.fhir.centraxx.metamodel.enums.GenderType
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.2
 */
patient {

  id = "Patient/" + context.source["patientcontainer.id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Patient-Pseudonym"
  }

  final def localId = context.source["patientcontainer.idContainer"]?.find {
    "Lokal" == it["idContainerType"]?.getAt("code") // TODO: site specific
  }

  if (localId) {
    identifier {
      value = localId["psn"]
      type {
        coding {
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS"
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
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS"
          code = "Global" // TODO: site specific
        }
      }
    }
  }

  birthDate = normalizeDate(context.source["birthdate.date"] as String)
  deceasedDateTime = "UNKNOWN" != context.source["dateOfDeath.precision"] ? normalizeDate(context.source["dateOfDeath.date"] as String) : null

  if (context.source["genderType"]) {
    gender = mapGender(context.source["genderType"] as GenderType)
  }
}

static AdministrativeGender mapGender(final GenderType genderType) {
  switch (genderType) {
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
