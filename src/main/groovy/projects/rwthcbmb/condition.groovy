package projects.rwthcbmb

import de.kairos.fhir.centraxx.metamodel.IdContainer

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Diagnosis
 * Specified by https://simplifier.net/bbmri.de/condition
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.2
 */
condition {

  id = "Condition/" + context.source[diagnosis().id()]

  meta {
    profile "https://fhir.bbmri.de/StructureDefinition/Condition"
  }

  final def idContainer = context.source[diagnosis().patientContainer().idContainer()]?.find {
    "MPI" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(CODE)
  }

  subject {
    reference = "Patient/" + idContainer[IdContainer.ID_CONTAINER_TYPE]?.getAt(CODE)
  }

  final def diagnosisId = context.source["diagnosisId"]
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

  final def clinician = context.source["clinician"]
  if (clinician) {
    recorder {
      identifier {
        display = clinician
      }
    }
  }

  onsetDateTime {
    date = normalizeDate(context.source["diagnosisDate.date"] as String)
  }

  code {
    coding {
      system = "urn:centraxx:CodeSystem/IcdCatalog-" + context.source["icdEntry.catalogue.id"]
      code = context.source["icdEntry.code"] as String
      version = context.source["icdEntry.kind"]
      display = context.source["icdEntry.name"]
    }

    coding {
      system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
      code = context.source["icdEntry.code"] as String
      version = context.source["icdEntry.catalogue.version"]
      display = context.source["icdEntry.name"]
    }
  }

}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
}

