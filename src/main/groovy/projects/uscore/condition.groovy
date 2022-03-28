package projects.uscore

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Diagnosis
 * Specified: https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-condition.html
 * @author Niklas Biedka
 * @since v.1.13.0, CXX.v.2022.1.0
 */

condition {

  id = "Condition/" + context.source[diagnosis().id()]

  meta {
    profile "https://www.hl7.org/fhir/us/core/StructureDefinition/us-core-condition"
  }

  verificationStatus {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/condition-ver-status"
      code = matchVerificationStatusToDiagnosisCertainty(context.source[diagnosis().diagnosisCertainty()] as String)
    }
  }

  clinicalStatus {
    extension {
      url = "http://hl7.org/fhir/StructureDefinition/data-absent-reason"
      valueCode = "unknown"
    }
  }

  code {
    coding {
      system = "http://hl7.org/fhir/sid/icd-10-cm"
      code = context.source[diagnosis().icdEntry().code()] as String
    }
  }

  subject {
    reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
  }

}

static String matchVerificationStatusToDiagnosisCertainty(final String resp) {
  switch (resp) {
    case ("V"): return "unconfirmed"
    case ("G"): return "confirmed"
    case ("A"): return "refuted"
    case ("Z"): return "differential"
    default: null
  }

}
