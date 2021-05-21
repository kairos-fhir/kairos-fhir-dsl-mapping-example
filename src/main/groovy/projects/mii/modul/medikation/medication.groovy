package projects.mii.modul.medikation


import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

/**
 * Represented by a CXX Medication
 * Specified by https://simplifier.net/medizininformatikinitiative-modulmedikation/medication-duplicate-3
 * The specification of the coding used in CXX is unknown. Thus, the use of specific code systems must be integrated
 * depending on the standard that might be used in a customer CXX.
 * @author Mike Wähnert, Jonas Küttner
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 * TODO: work in progress
 */
medication {

  id = "Medication/" + context.source[medication().id()]

  meta {
    source = "urn:centraxx"
    profile("https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/Medication")
  }

  code {
    coding {
      system = "http://fhir.de/CodeSystem/ifa/pzn"
      code = context.source[medication().code()] as String
    }
    text = context.source[medication().name()] as String
  }

  // The
  form {
    coding {
      system = "urn:centraxx"
      code = context.source[medication().applicationForm()] as String
    }
  }

  // export of agent as text. If a specific coding system is used for the agent in a customer system, the coding system can
  // be introduced in the itemCodeableConcept.
  ingredient {
    itemCodeableConcept {
      text = context.source[medication().agent()] as String
    }
  }

  batch {
    lotNumber = context.source[medication().fillerOrderNumber()]
  }
}
