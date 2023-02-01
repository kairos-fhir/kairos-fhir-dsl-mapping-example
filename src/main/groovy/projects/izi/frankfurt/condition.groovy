package projects.izi.frankfurt

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Diagnosis
 * @author Franzy Hohnstaedter, Mike Wähnert
 * @since v.1.6.0, CXX.v.3.17.1.7
 *
 * HINTS:
 *  - Catalog system URLS without logical FHIR ID (e.g. instead of with code or version) are implemented since
 *  CXX.v.3.18.2.11, CXX.v.3.18.3.8, CXX.v.3.18.4, CXX.v.2022.1.5, CXX.v.2022.2.5, CXX.v.2022.3.5, CXX.v.2022.4.0
 * - Before those versions, an static ID type mapping for each catalog and value list of each source system is necessary in the target system.
 */
condition {

  id = "Condition/" + context.source[diagnosis().id()]

  extension {
    url = FhirUrls.Extension.UPDATE_WITH_OVERWRITE
    valueBoolean = false
  }

  final def patIdContainer = context.source[diagnosis().patientContainer().idContainer()]?.find {
    "PaIdTMP" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  if (patIdContainer) {
  subject {
      identifier {
        value = patIdContainer[IdContainer.PSN]
        type {
          coding {
            system = "urn:centraxx"
            code = patIdContainer[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE) as String
          }
        }
      }
    }
  }

  final def diagnosisId = context.source[diagnosis().diagnosisId()]
  if (diagnosisId) {
    identifier {
      value = diagnosisId
      type {
        coding {
          system = "urn:centraxx"
          code = "diagnosisId"
        }
      }
    }
  }

  onsetDateTime {
    date = context.source[diagnosis().diagnosisDate().date()]
  }

  recordedDate {
    date = context.source[diagnosis().creationDate()]
  }

  code {
    coding {
      system = "urn:centraxx:CodeSystem/IcdCatalog#v.2020"
      code = context.source[diagnosis().icdEntry().code()] as String
      version = context.source[diagnosis().icdEntry().kind()]
    }
  }

}

