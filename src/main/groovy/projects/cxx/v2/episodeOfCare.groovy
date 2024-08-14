package projects.cxx.v2

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Diagnosis
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import org.hl7.fhir.r4.model.EpisodeOfCare
import org.hl7.fhir.r4.model.codesystems.EncounterStatus

import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode
/**
 * @author Jonas Küttner
 */
episodeOfCare {

  id = "EpisodeOfCare/Episode-" + context.source[episode().id()]

  patient {
    reference = "Patient/" + context.source[episode().patientContainer().id()]
  }

  context.source[episode().idContainer()].each { final def idc ->
    identifier {
      type {
        coding {
          system = FhirUrls.System.IdContainerType.BASE_URL
          code = idc[IdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE]
        }
      }
      value = idc[IdContainer.PSN]
    }
  }

  status = EpisodeOfCare.EpisodeOfCareStatus.ACTIVE

  period {
    start = context.source[episode().validFrom()]
    end = context.source[episode().validUntil()]
  }

  if (context.source[episode().habitation()]) {
    managingOrganization {
      reference = "Organization/" + context.source[episode().habitation().id()]
    }
  }

  status = EncounterStatus.UNKNOWN


  for (final def diag : context.source[episode().diagnoses()]) {
    diagnosis {
      condition {
        reference = "Condition/" + diag[Diagnosis.ID]
      }
    }
  }
}

