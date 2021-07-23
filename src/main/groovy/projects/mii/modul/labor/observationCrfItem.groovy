package projects.mii.modul.labor


import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.crfItem

/**
 * Represented by CXX CrfItem
 * specified by https://simplifier.net/medizininformatikinitiative-modullabor/observationlab
 * @author Mike Wähnert, Jonas Küttner
 * @since v.1.9.0, CXX.v.3.18.2
 * TODO: work in progress
 */
observation {

  id = "Observation/" + context.source[crfItem().id()]

  meta {
    source = "urn:centraxx"
    profile("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/ObservationLab")
  }

  identifier {
    type {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "OBI"
      }
    }
    system = "urn:centraxx"
    value = context.source[crfItem().id()]
  }

  status = Observation.ObservationStatus.UNKNOWN

  // There is no categorization for LaborFindingLaborValues in Centraxx. Therefore, laboratory is assigned here.
  category {
    coding {
      system = "http://loinc.org"
      code = "26436-6"
    }
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "laboratory"
    }
  }

  code {
    coding {
      system = "urn:centraxx"
      code = context.source[crfItem().template().laborValue().code()] as String
    }
  }

  subject {
    reference = "Patient/" + context.source[crfItem().crf().studyVisitItem().studyMember().patientContainer().id()]
  }

}
