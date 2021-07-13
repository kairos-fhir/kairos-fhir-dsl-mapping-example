package projects.dktk.v2

import de.kairos.fhir.centraxx.metamodel.enums.GenderType
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 */
patient {

  id = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Patient-Pseudonym"
  }

  final def localId = context.source[patientMasterDataAnonymous().patientContainer().idContainer()]?.find {
    "Lokal" == it[ID_CONTAINER_TYPE]?.getAt(CODE) // TODO: site specific
  }

  if (localId) {
    identifier {
      value = localId[PSN]
      type {
        coding {
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS"
          code = "Lokal" // A local site id has always type "Lokal"
        }
      }
    }
  }

  final def globalId = context.source[patientMasterDataAnonymous().patientContainer().idContainer()]?.find {
    "DKTK" == it[ID_CONTAINER_TYPE]?.getAt(CODE) // TODO: site specific
  }

  if (globalId) {
    identifier {
      value = globalId[PSN]
      type {
        coding {
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS"
          code = "Global" // The global DKTK Id has always type "Global"
        }
      }
    }
  }

  birthDate = normalizeDate(context.source[patientMasterDataAnonymous().birthdate().date()] as String)
  deceasedDateTime = "UNKNOWN" != context.source[patientMasterDataAnonymous().dateOfDeath().precision()] ? normalizeDate(context.source[patientMasterDataAnonymous().dateOfDeath().date()] as String) : null

  if (context.source[patientMasterDataAnonymous().genderType()]) {
    gender = mapGender(context.source[patientMasterDataAnonymous().genderType()] as GenderType)
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
