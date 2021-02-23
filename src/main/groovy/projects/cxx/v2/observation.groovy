package projects.cxx.v2

import de.kairos.fhir.centraxx.metamodel.IdContainerType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.3.17.2
 */
observation {
  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      system = "urn:centraxx"
      code = context.source[laborMapping().laborFinding().shortName()] as String
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }


  final def patIdContainer = context.source[laborMapping().relatedPatient().idContainer()]?.find {
    "COVID-19-PATIENTID" == it[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }
  if (patIdContainer) {
    subject {
      identifier {
        value = patIdContainer[PSN]
        type {
          coding {
            system = "urn:centraxx"
            code = patIdContainer[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE) as String
          }
        }
      }
    }
  }

//  issued = context.source[laborMapping().laborFinding().creationDate()] TODO extend builder
  method {
    coding {
      system = "urn:centraxx"
      version = context.source[laborMapping().laborFinding().laborMethod().version()]
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
    }
  }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->
    component {
      code {
        coding {
          system = ""
          code = ""

        }
      }
      valueQuantity {
        value = ""
      }
    }
  }


}

