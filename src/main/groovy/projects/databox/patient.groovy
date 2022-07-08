package projects.databox


import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.enums.GenderType

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.PatientMaster.GENDER_TYPE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * @author Marvin Schmidtke
 */
patient {

  id = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]

  final def idContainer = context.source[patientMasterDataAnonymous().patientContainer().idContainer()]?.find {
    "MPI" == it[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
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
  birthDate = normalizeDate(context.source[patientMasterDataAnonymous().birthdate().date()] as String)
  generalPractitioner {
    identifier {
      value = "Databox"
    }
  }

  extension {
    url = "https://fhir.centraxx.de/extension/updateWithOverwrite"
    valueBoolean = false
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
