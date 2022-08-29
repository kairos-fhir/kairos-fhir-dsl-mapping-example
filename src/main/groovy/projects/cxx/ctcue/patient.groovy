package projects.cxx.ctcue

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.OrganisationUnit
import de.kairos.fhir.centraxx.metamodel.enums.GenderType

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.PatientMaster.GENDER_TYPE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.2022.2.0
 */
patient {

  id = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]

  context.source[patientMasterDataAnonymous().patientContainer().idContainer()].each { final idContainer ->
    identifier {
      value = idContainer[PSN]
      type {
        coding {
          system = FhirUrls.System.IdContainerType.BASE_URL
          code = idContainer[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
      }
    }
  }

  if (context.source[GENDER_TYPE]) {
    gender = mapGender(context.source[GENDER_TYPE] as GenderType)
  }

  birthDate = normalizeDate(context.source[patientMasterDataAnonymous().birthdate().date()] as String)

  deceasedDateTime = "UNKNOWN" != context.source[patientMasterDataAnonymous().dateOfDeath().precision()] ?
      context.source[patientMasterDataAnonymous().dateOfDeath().date()] : null

  context.source[patientMasterDataAnonymous().patientContainer().organisationUnits()].each { final orgUnit ->
    generalPractitioner {
      identifier {
        value = orgUnit[OrganisationUnit.CODE]
      }
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
