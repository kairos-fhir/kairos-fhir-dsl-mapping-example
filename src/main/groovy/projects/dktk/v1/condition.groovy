package projects.dktk.v1

/**
 * Represented by a CXX Diagnosis
 * Specified by https://simplifier.net/oncology/primaerdiagnose
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.2
 */
condition {

  id = "Condition/" + context.source["id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Condition-Primaerdiagnose"
  }

  subject {
    reference = "Patient/" + context.source["patientcontainer.id"]
  }

  if (context.source["episode"]) {
    encounter {
      reference = "Encounter/" + context.source["episode.id"]
    }
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
    date = context.source["diagnosisDate.date"]
  }

  code {
    coding {
      system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
      code = context.source["icdEntry.code"] as String
      version = context.source["icdEntry.catalogue.catalogueVersion"]
    }
  }

  context.source["samples"]?.each { final sample ->
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-Specimen"
      valueReference {
        reference = "Specimen/" + sample["id"]
      }
    }
  }


}
