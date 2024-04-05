package projects.patientfinder

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.AbstractSample
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
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
  println(context.source)
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

  if (parentSample != null) {
    parent {
      reference = "Specimen/" + parentSample[AbstractSample.ID]
    }
  }

  final def finding = context.source[sample().laborMappings()].find {
    it[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == "ADDITIONAL_SAMPLE_DATA"
  }


  collection {
    collectedDateTime {
      date = context.source[sample().samplingDate().date()]
      quantity {
        value = context.source[sample().initialAmount().amount()] as Number
        unit = context.source[sample().initialAmount().unit()]
      }
    }

    if (finding != null) {
      final def collectionMethod = finding[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find {
        it[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == "COLLECTION_METHOD"
      }

      if (collectionMethod != null) {
        method {
          coding {
            code = collectionMethod[LaborFindingLaborValue.STRING_VALUE]
          }
        }
      }
    }
  }
}