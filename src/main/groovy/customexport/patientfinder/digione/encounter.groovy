package customexport.patientfinder.digione

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.PatientTransfer
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Encounter

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.IdContainerType.DECISIVE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode

/**
 * Represents a HDRP Episode.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-encounter.html
 *
 * hints:
 * - Mapping uses SNOMED-CT concepts.
 * - There is no participant, reasonCode/reference, hospitalization, location in HDRP
 *
 *
 * @author Mike Wähnert
 * @since v.1.13.0, HDRP.v.2023.3.0
 */
encounter {

  id = "Encounter/" + context.source[episode().id()]


  context.source[episode().idContainer()].findAll { final def idc ->
    idc[ID_CONTAINER_TYPE][DECISIVE] as boolean
  }.each { final def idc ->
    identifier {
      value = idc[PSN]
    }
  }

  status = Encounter.EncounterStatus.UNKNOWN

  if (context.source[episode().stayType().code()]) {

    final String classCode = context.source[episode().stayType().code()]
    class_ {

      code = classCode == "Inpatient" ? "IMP" : classCode
      display = context.source[episode().stayType().multilinguals()].find { final def ml ->
        ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
      }?.getAt(Multilingual.SHORT_NAME)
    }
  }

  subject {
    reference = "Patient/" + context.source[episode().patientContainer().id()]
  }

  if (context.source[episode().parent()]) {
    partOf {
      reference = "Episode/" + context.source[episode().parent().id()]
    }
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

  if (context.source[episode().validFrom()] != null && context.source[episode().validUntil()] != null) {
    final DateTimeType startDate = new DateTimeType(context.source[episode().validFrom()] as String)
    final DateTimeType endDate = new DateTimeType(context.source[episode().validUntil()] as String)

    final long diff = endDate.getValue().getTime() - startDate.getValue().getTime()

    length {
      value = diff
      unit = "ms"
      system = "http://unitsofmeasure.org"
    }
  }

  type {
    coding {
      system = "http://snomed.info/sct"
      code = "308335008"
      display = "Patient encounter procedure"
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

  if (context.source[episode().attendingDoctor()]) {
    participant {
      individual {
        reference = "Practitioner/" + context.source[episode().attendingDoctor().id()]
      }
    }
  }
}

