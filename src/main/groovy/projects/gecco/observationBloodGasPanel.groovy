package projects.gecco


import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since v.1.7.0, CXX.v.3.17.2
 */
observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "BLOODGASPANEL_PROFILE") {
    return // no export
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/blood-gas-panel"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://loinc.org"
      code = "26436-6"
    }
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "laboratory"
    }
    coding {
      system = "http://loinc.org"
      code = "18767-4"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "24338-6"
    }
    coding {
      system = "http://loinc.org"
      code = "24336-0"
    }
    coding {
      system = "http://loinc.org"
      code = "24337-8"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }
  encounter {
    reference = "Episode/" + context.source[laborMapping().episode().id()]
  }


  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  hasMember {
    reference = "pH/" + context.source[laborMapping().laborFinding().id()]
  }
  hasMember {
    reference = "PaCO2/" + context.source[laborMapping().laborFinding().id()]
  }
  hasMember {
    reference = "PaO2/" + context.source[laborMapping().laborFinding().id()]
  }
  hasMember {
    reference = "Oxygen Saturation/" + context.source[laborMapping().laborFinding().id()]
  }
  hasMember {
    reference = "FiO2/" + context.source[laborMapping().laborFinding().id()]
  }

}



