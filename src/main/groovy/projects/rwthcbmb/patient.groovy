package projects.rwthcbmb

import de.kairos.fhir.centraxx.metamodel.IdContainer

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * Specified by https://simplifier.net/bbmri.de/patient
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.2
 */
patient {

  final def idContainer = context.source[patientMasterDataAnonymous().patientContainer().idContainer()]?.find {
    "MPI" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(CODE)
  }

  if (idContainer == null) {
    return // If patient has no MPI, patient is not exported.
  }

  id = "Patient/" + idContainer[IdContainer.ID_CONTAINER_TYPE]?.getAt(CODE)

  meta {
    profile "https://fhir.bbmri.de/StructureDefinition/Patient"
  }

  if (idContainer) {
    identifier {
      value = idContainer[IdContainer.PSN]
      type {
        coding {
          system = "urn:centraxx"
          code = idContainer[IdContainer.ID_CONTAINER_TYPE]?.getAt(CODE)
        }
      }
    }
  }
}
