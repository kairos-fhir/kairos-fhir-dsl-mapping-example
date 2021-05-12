package projects.mii


import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

// TODO: work in progress
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
