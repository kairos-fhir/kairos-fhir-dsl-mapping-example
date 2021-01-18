package projects.dktk

import org.hl7.fhir.r4.model.Procedure

/**
 * Represented by a CXX Surgery
 * OPS code for surgeries are not available in CXX
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.4
 */
procedure {
  id = "Procedure/Surgery-" + context.source["id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Procedure-Operation"
  }

  status = Procedure.ProcedureStatus.UNKNOWN

  category {
    coding {
      system = "urn:dktk:dataelement:33:2"
      code = "OP"
    }
  }

  subject {
    reference = "Patient/" + context.source["patientcontainer.id"]
  }

  if (context.source["episode"]) {
    encounter {
      reference = "Encounter/" + context.source["episode.id"]
    }
  }

  reasonReference {
    reference = "Condition/" + context.source["tumour.centraXXDiagnosis.id"]
  }

  outcome {
    if (context.source["rClassificationDict"]) {
      coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS"
        version = context.source["rClassificationDict.version"]
        code = context.source["rClassificationDict.nameMultilingualEntries2"]?.find { it["lang"] == "en" }?.getAt("value") as String
      }
    }
    if (context.source["rClassificationLocalDict.code"]) {
      coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS"
        version = context.source["rClassificationLocalDict.version"]
        code = context.source["rClassificationLocalDict.nameMultilingualEntries2"]?.find { it["lang"] == "en" }?.getAt("value") as String
      }
    }
  }
}

