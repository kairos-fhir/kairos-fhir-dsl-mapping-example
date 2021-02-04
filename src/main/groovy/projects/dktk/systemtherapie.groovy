package projects.dktk

import org.hl7.fhir.r4.model.MedicationStatement

/**
 * Represented by a CXX SystemTherapy
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.4
 */
medicationStatement {

  id = "MedicationStatement/SystemTherapy-" + context.source["id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-MedicationStatement-Systemtherapie"
  }

  status = MedicationStatement.MedicationStatementStatus.UNKNOWN

  identifier {
    value = context.source["systemTherapyId"]
  }

  subject {
    reference = "Patient/" + context.source["patientcontainer.id"]
  }

  if (context.source["episode"]) {
    context_ {
      reference = "Encounter/" + context.source["episode.id"]
    }
  }

  reasonReference {
    reference = "Condition/" + context.source["tumour.centraXXDiagnosis.id"]
  }

  // TODO which CXX field?
//  extension {
//    url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-StellungZurOp"
//    valueCoding {
//      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTStellungOPCS"
//    }
//  }

  extension {
    url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SYSTIntention"
    valueCoding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTIntentionCS"
      code = context.source["intentionDict"]?.getAt("code")?.toString()?.toUpperCase()
    }
  }

  if (context.source["rClassificationLocalDict.code"]) {
    extension {
      //TODO Declared is a reference to Observation, but Operation uses a valueCoding
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-LokaleResidualstatus"
      valueCoding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS"
        version = context.source["rClassificationLocalDict.version"]
        code = context.source["rClassificationLocalDict.nameMultilingualEntries2"]?.find { it["lang"] == "en" }?.getAt("value")
      }
    }
  }

  if (context.source["rClassificationDict"]) {
    extension {
      //TODO Declared is a reference to Observation, but Operation uses a valueCoding
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-GesamtbeurteilungResidualstatus"
      valueCoding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS"
        version = context.source["rClassificationDict.version"]
        code = context.source["rClassificationDict.nameMultilingualEntries2"]?.find { it["lang"] == "en" }?.getAt("value")
      }
    }
  }
}
