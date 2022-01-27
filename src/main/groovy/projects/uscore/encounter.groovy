package projects.uscore

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
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
 * @since v.1.13.0, CXX.v.2022.1.0
 */
encounter {

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

  class_ {
    system = "http://terminology.hl7.org/CodeSystem/v3-ActCode"
    code = "unknown"
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

    start {
      date = normalizeDate(context.source[episode().validFrom()] as String)
    }

    end {
      date = normalizeDate(context.source[episode().validUntil()] as String)
    }

  }

  if (context.source[episode().habitation()]) {
    serviceProvider {
      reference = "Organization/" + context.source[episode().habitation().id()]
    }
  }
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}
