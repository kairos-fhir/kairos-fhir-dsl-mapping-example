package projects.patientfinder

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.AbstractSample

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.IdContainerType.DECISIVE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

specimen {

  id = "Specimen/" + context.source[sample().id()]

  context.source[sample().idContainer()].each { final idContainer ->
    final boolean isDecisive = idContainer[ID_CONTAINER_TYPE]?.getAt(DECISIVE)
    if (isDecisive) {
      identifier {
        value = idContainer[PSN]
        type {
          coding {
            system = FhirUrls.System.IdContainerType.BASE_URL
            code = idContainer[ID_CONTAINER_TYPE]?.getAt(CODE)
          }
        }
      }
    }
  }

  type {
    coding {
      system = FhirUrls.System.Sample.SampleKind.BASE_URL
      code = context.source[sample().sampleKind()]
    }
  }

  subject {
    reference = "Patient/" + context.source[sample().patientContainer().id()]
  }

  final def parentSample = context.source[sample().parent()]

  if (parentSample != null){
    parent {
      reference = "Specimen/" + parentSample[AbstractSample.ID]
    }
  }

  collection {
    collectedDateTime {
      date = context.source[sample().samplingDate().date()]
      quantity {
        value = context.source[sample().initialAmount().amount()] as Number
        unit = context.source[sample().initialAmount().unit()]
      }
    }
  }
}