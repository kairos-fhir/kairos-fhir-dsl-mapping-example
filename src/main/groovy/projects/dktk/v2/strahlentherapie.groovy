package projects.dktk.v2


import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.radiationTherapy

/**
 * Represented by a CXX RadiationTherapy
 * Specified by https://simplifier.net/oncology/strahlentherapie
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
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTTherapieartCS"
      code = "ST" //Strahlentherapie
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

  if (context.source[radiationTherapy().tumour()]) {
    reasonReference {
      reference = "Condition/" + context.source[radiationTherapy().tumour().centraxxDiagnosis().id()]
    }
  }

  if (context.source[radiationTherapy().intentionDict()]) {
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SYSTIntention"
      valueCoding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTIntentionCS"
        code = context.source[radiationTherapy().intentionDict()]?.getAt(CODE)?.toString()?.toUpperCase()
      }
    }
  }
}
