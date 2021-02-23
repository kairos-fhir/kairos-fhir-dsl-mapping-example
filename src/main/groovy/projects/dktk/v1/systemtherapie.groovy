package projects.dktk.v1

import org.hl7.fhir.r4.model.MedicationStatement

/**
 * Represented by a CXX SystemTherapy
 *
 * Hints:
 * There is no representation in a CXX SystemTherapy for the Extensions StellungZurOp, LokaleResidualstatus and GesamtbeurteilungResidualstatus
 *
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

  extension {
    url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SYSTIntention"
    valueCoding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTIntentionCS"
      code = context.source["intentionDict"]?.getAt("code")?.toString()?.toUpperCase()
    }
  }
}
