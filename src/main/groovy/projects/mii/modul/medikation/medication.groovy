package projects.mii.modul.medikation


import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

/**
 * Represented by a CXX Medication
 * Specified by https://simplifier.net/medizininformatikinitiative-modulmedikation/medication-duplicate-3
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 * TODO: work in progress
 */
medication {

  id = "Medication/" + context.source[medication().id()]

  meta {
    profile("https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/Medication")
  }

  code {
    coding {
      system = "http://fhir.de/CodeSystem/ifa/pzn"
      code = context.source[medication().code()] as String
    }
  }

  batch {
    lotNumber = context.source[medication().fillerOrderNumber()]
  }
}
