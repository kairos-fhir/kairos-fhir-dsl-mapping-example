package projects.patientfinder

import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.IdContainerType.DECISIVE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Export additional data via a SAMPLETYPELABORMAPPING that links to a finding
 * with LABORMETHOD code "ADDITIONAL_SAMPLE_DATA". If that finding
 * contains a value for a parameter with code "COLLECTION_METHOD", the value
 * is exported to collection.method.coding.code
 */
specimen {

  id = "Specimen/" + context.source[sample().id()]

  context.source[sample().idContainer()].each { final idContainer ->
    final boolean isDecisive = idContainer[ID_CONTAINER_TYPE]?.getAt(DECISIVE)
    if (isDecisive) {
      identifier {
        value = idContainer[PSN]
      }
    }
  }

  if (context.source[sample().sampleType()] != null){
    type {
      coding {
        code = context.source[sample().sampleType().code()] as String
        display = context.source[sample().sampleType().multilinguals()].find { final def ml ->
          ml[Multilingual.SHORT_NAME] != null & ml[Multilingual.LANGUAGE] == "de"
        }?.getAt(Multilingual.SHORT_NAME)
      }
    }
  }
}