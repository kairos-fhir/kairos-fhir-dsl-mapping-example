package projects.dktk.v2


import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.radiationTherapy

/**
 * Represented by a CXX RadiationTherapy
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 */
procedure {
  id = "Procedure/RadiationTherapy-" + context.source[radiationTherapy().id()]

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
    reference = "Patient/" + context.source[radiationTherapy().patientContainer().id()]
  }

  if (context.source[radiationTherapy().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[radiationTherapy().episode().id()]
    }
  }

  reasonReference {
    reference = "Condition/" + context.source[radiationTherapy().tumour().centraxxDiagnosis().id()]
  }

  extension {
    url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SYSTIntention"
    valueCoding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTIntentionCS"
      code = context.source[radiationTherapy().intentionDict()]?.getAt(CODE)?.toString()?.toUpperCase()
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
