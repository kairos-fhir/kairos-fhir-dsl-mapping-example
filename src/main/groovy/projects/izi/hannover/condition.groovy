package projects.izi.hannover

/**
 * Represented by a CXX Diagnosis
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.2
 */
condition {

  id = "Condition/" + context.source["id"]

  final def patIdContainer = context.source["patientcontainer.idContainer"]?.find {
    "SID" == it["idContainerType"]?.getAt("code")
  }

  if (patIdContainer) {
    subject {
      identifier {
        value = patIdContainer["psn"]
        type {
          coding {
            system = "urn:centraxx"
            code = patIdContainer["idContainerType"]?.getAt("code") as String
          }
        }
      }
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
      system = "urn:centraxx:CodeSystem/IcdCatalog-" + context.source["icdEntry.catalogue.id"]
      code = context.source["icdEntry.code"] as String
      version = context.source["icdEntry.kind"]
      display = context.source["icdEntry.name"]
    }
  }

}

