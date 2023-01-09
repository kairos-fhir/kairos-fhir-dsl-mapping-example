package projects.cxx.custom.aggregate

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IdContainerType

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode

/**
 * Represents a CXX Episode
 * dummy script for aggregate bundle demonstration
 * @author Jonas Küttner
 */

encounter {
  id = "Encounter/" + context.source[episode().id()]

  context.source[episode().idContainer()].each { final idContainer ->
    identifier {
      value = idContainer[PSN]
      type {
        coding {
          system = FhirUrls.System.IdContainerType.BASE_URL
          code = idContainer[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
      }
    }
  }

  subject {
    reference = "Patient/" + context.source[episode().patientContainer().id()]
  }
}

