package projects.cxx.napkon.num_hgw

import de.kairos.fhir.centraxx.metamodel.IdContainerType
import org.hl7.fhir.r4.model.ResearchSubject

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientStudy
/**
 * Represented by CXX PatientStudy (StudyRegister)
 * @author Mario Schattschneider
 */
researchSubject {

  id = "ResearchSubject/" + context.source[patientStudy().id()]

  status = ResearchSubject.ResearchSubjectStatus.CANDIDATE

  final def idContainer = context.source[patientStudy().patientContainer().idContainer()]?.find {
    "LIMSPSN" == it[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }
  if (idContainer) {
    individual {
      identifier {
        value = idContainer[PSN]
        type {
          coding {
            system = "urn:centraxx"
            code = "MPI"
          }
        }
      }
    }
  } else {
    System.println("No Patient ID Container")
  }

  study {
    identifier {
      value = context.source[patientStudy().flexiStudy().code()]
    }
  }

  period {
    if (context.source[patientStudy().memberFrom()]) {
      start {
        date = context.source[patientStudy().memberFrom()]
      }
    }
    if (context.source[patientStudy().memberUntil()]) {
      end {
        date = context.source[patientStudy().memberUntil()]
      }
    }
  }

  actualArm = context.source[patientStudy().studyArm().name()]

}