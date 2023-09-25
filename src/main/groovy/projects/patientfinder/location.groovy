package projects.patientfinder

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.dsl.r4.execution.Fhir4Source

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientTransfer

/**
 * Represented by a CXX PatientTransfer of an episode
 * @author Mike Wähnert
 * @since v.1.23.0, CXX.v.2023.3.2
 */
location {

  id = "Location/PT-" + context.source[patientTransfer().id()]

  type {
    coding {
      system = FhirUrls.System.LOCATION_TYPE
      code = "PATIENT_TRANSFER"
    }
  }

  name = createName(context.source)

  if (context.source[patientTransfer().habitation()]) {
    managingOrganization {
      reference = "Organization/" + context.source[patientTransfer().habitation().id()]
    }
  }

  if (context.source[patientTransfer().episode()]) {
    extension {
      url = FhirUrls.Extension.PatientTransfer.EPISODE
      valueReference {
        reference = "Encounter/" + context.source[patientTransfer().episode().id()]
      }
    }
  }
  if (context.source[patientTransfer().transferDate()]) {
    extension {
      url = FhirUrls.Extension.PatientTransfer.TRANSFER_DATE
      valueDateTime = context.source[patientTransfer().transferDate()]
    }
  }

  extension {
    url = FhirUrls.Extension.PatientTransfer.CURRENT_LOCATION

    if (context.source[patientTransfer().habitation()]) {
      extension {
        url = FhirUrls.Extension.PatientTransfer.Location.HABITATION
        valueReference {
          reference = "Organization/" + context.source[patientTransfer().habitation().id()]
        }
      }
    }
    if (context.source[patientTransfer().floor()]) {
      extension {
        url = FhirUrls.Extension.PatientTransfer.Location.FLOOR
        valueCoding {
          system = "urn:centraxx:CodeSystem/Catalog-" + context.source[patientTransfer().floor().catalog().id()]
          code = context.source[patientTransfer().floor().code()]
        }
      }
    }
    if (context.source[patientTransfer().room()]) {
      extension {
        url = FhirUrls.Extension.PatientTransfer.Location.ROOM
        valueCoding {
          system = "urn:centraxx:CodeSystem/Catalog-" + context.source[patientTransfer().room().catalog().id()]
          code = context.source[patientTransfer().room().code()]
        }
      }
    }
    if (context.source[patientTransfer().bed()]) {
      extension {
        url = FhirUrls.Extension.PatientTransfer.Location.BED
        valueCoding {
          system = "urn:centraxx:CodeSystem/Catalog-" + context.source[patientTransfer().bed().catalog().id()]
          code = context.source[patientTransfer().bed().code()]
        }
      }
    }
  }
  extension {
    url = FhirUrls.Extension.PatientTransfer.PRIOR_LOCATION
    if (context.source[patientTransfer().priorHabitation()]) {
      extension {
        url = FhirUrls.Extension.PatientTransfer.Location.HABITATION
        valueReference {
          reference = "Organization/" + context.source[patientTransfer().priorHabitation().id()]
        }
      }
    }
    if (context.source[patientTransfer().priorFloor()]) {
      extension {
        url = FhirUrls.Extension.PatientTransfer.Location.FLOOR
        valueCoding {
          system = "urn:centraxx:CodeSystem/Catalog-" + context.source[patientTransfer().priorFloor().catalog().id()]
          code = context.source[patientTransfer().priorFloor().code()]
        }
      }
    }
    if (context.source[patientTransfer().priorRoom()]) {
      extension {
        url = FhirUrls.Extension.PatientTransfer.Location.ROOM
        valueCoding {
          system = "urn:centraxx:CodeSystem/Catalog-" + context.source[patientTransfer().priorRoom().catalog().id()]
          code = context.source[patientTransfer().priorRoom().code()]
        }
      }
    }
    if (context.source[patientTransfer().priorBed()]) {
      extension {
        url = FhirUrls.Extension.PatientTransfer.Location.BED
        valueCoding {
          system = "urn:centraxx:CodeSystem/Catalog-" + context.source[patientTransfer().priorBed().catalog().id()]
          code = context.source[patientTransfer().priorBed().code()]
        }
      }
    }
  }
}

static String createName(final Fhir4Source contextSource) {
  return ["Habitation": contextSource[patientTransfer().habitation().code()]
          , "Floor"   : contextSource[patientTransfer().floor().code()]
          , "Room"    : contextSource[patientTransfer().room().code()]
          , "Bed"     : contextSource[patientTransfer().bed().code()]]
      .entrySet().stream()
      .filter { it.value != null }
      .map { it.key + ":" + it.value }
      .collect()
      .join("=>")
}

static boolean isFakeEpisode(final def episode) {
  if (episode == null) {
    return true
  }

  if (["SACT", "COSD"].contains(episode[Episode.ENTITY_SOURCE])) {
    return true
  }

  final def fakeId = episode[Episode.ID_CONTAINER]?.find { (it[PSN] as String).toUpperCase().startsWith("FAKE") }
  return fakeId != null
}