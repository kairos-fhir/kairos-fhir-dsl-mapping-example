package projects.patientfinder.iqtrial

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.PatientTransfer
import org.hl7.fhir.r4.model.Encounter

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.IdContainerType.DECISIVE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode

/**
 * Represents a CXX Episode.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-encounter.html
 *
 * hints:
 * - Mapping uses SNOMED-CT concepts.
 * - There is no participant, reasonCode/reference, hospitalization, location in CXX
 *
 *
 * @author Mike Wähnert
 * @since v.1.13.0, CXX.v.2023.3.0
 */
encounter {

  if (isFakeEpisode(context.source)) {
    return //filters Encounters with EncounterOID prefix FAKE_
  }

  id = "Encounter/" + context.source[episode().id()]

  meta {
    profile "http://hl7.org/fhir/us/core/StructureDefinition/us-core-encounter"
  }

  context.source[episode().idContainer()].each { final idContainer ->
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

  status = Encounter.EncounterStatus.UNKNOWN

  if (context.source[episode().stayType().code()]) {
    class_ {
      system = FhirUrls.System.Episode.StayType.BASE_URL
      code = context.source[episode().stayType().code()]
    }
  }

  type {
    coding {
      system = "http://snomed.info/sct"
      code = "308335008"
      display = "Patient encounter procedure"
    }
  }

  subject {
    reference = "Patient/" + context.source[episode().patientContainer().id()]
  }

  period {

    if (context.source[episode().validFrom()]) {
      start {
        date = context.source[episode().validFrom()]
        precision = TemporalPrecisionEnum.DAY.toString()
      }
    }

    if (context.source[episode().validUntil()]) {
      end {
        date = context.source[episode().validUntil()]
        precision = TemporalPrecisionEnum.DAY.toString()
      }
    }

  }

  if (context.source[episode().habitation()]) {
    serviceProvider {
      reference = "Organization/" + context.source[episode().habitation().id()]
    }
  }

  for (final pt in context.source[episode().patientTransfers()]) {
    location {
      location {
        reference = "Location/PT-" + pt[PatientTransfer.ID]
      }
    }
  }
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
