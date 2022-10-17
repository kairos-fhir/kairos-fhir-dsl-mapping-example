package projects.dktk.v2


import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.surgery

/**
 * Represented by a CXX Surgery
 * OPS code for surgeries are not available in CXX
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 */
procedure {
  id = "Procedure/Surgery-" + context.source[surgery().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Procedure-Operation"
  }

  status = Procedure.ProcedureStatus.UNKNOWN

  category {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTTherapieartCS"
      code = "OP" //Operation
    }
  }

  subject {
    reference = "Patient/" + context.source[surgery().patientContainer().id()]
  }

  if (context.source[surgery().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[surgery().episode().id()]
    }
  }

  if (context.source[surgery().tumour()]) {
    reasonReference {
      reference = "Condition/" + context.source[surgery().tumour().centraxxDiagnosis().id()]
    }
  }

  outcome {
    if (context.source[surgery().rClassificationDict()]) {
      coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS"
        version = context.source[surgery().rClassificationDict().version()]
        code = context.source[surgery().rClassificationDict().nameMultilingualEntries()]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
      }
    }
    if (context.source[surgery().rClassificationLocalDict().code()]) {
      coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS"
        version = context.source[surgery().rClassificationLocalDict()]
        code = context.source[surgery().rClassificationLocalDict().nameMultilingualEntries()]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
      }
    }
  }
}

