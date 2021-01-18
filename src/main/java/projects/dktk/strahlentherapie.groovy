package projects.dktk

import org.hl7.fhir.r4.model.Procedure

/**
 * Represented by a CXX RadiationTherapy
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.4
 */
procedure {
  id = "Procedure/RadiationTherapy-" + context.source["id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Procedure-Strahlentherapie"
  }

  status = Procedure.ProcedureStatus.UNKNOWN

  category {
    coding {
      system = "urn:dktk:dataelement:34:2"
      code = "ST"
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

  extension {
    url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SYSTIntention"
    valueCoding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTIntentionCS"
      code = context.source["intentionDict"]?.getAt("code")?.toString()?.toUpperCase()
    }
  }

  // TODO which CXX field?
//  extension {
//    url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-StellungZurOp"
//    valueCoding {
//      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTStellungOPCS"
//    }
//  }

}
