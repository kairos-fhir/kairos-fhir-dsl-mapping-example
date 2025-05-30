package projects.patientfinder.hull

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.enums.FhirAppointmentParticipantTypeEnum
import org.hl7.fhir.r4.model.Appointment

import static de.kairos.fhir.centraxx.metamodel.RootEntities.calendarEvent

/**
 * Represented by a CXX CalendarEvent of a Patient
 * @author Mike Wähnert
 * @since v.1.26.0, CXX.v.2023.5.0
 */
appointment {

  id = "Appointment/" + context.source[calendarEvent().id()]

  identifier {
    value = context.source[calendarEvent().eventId()]
  }

  serviceCategory {
    coding {
      system = FhirUrls.System.Calendar.Type.BASE_URL
      code = context.source[calendarEvent().type()]
    }
  }

  description = context.source[calendarEvent().description()]

  start {
    date = context.source[calendarEvent().startDate()]
  }

  end {
    date = context.source[calendarEvent().endDate()]
  }


  if (context.source[calendarEvent().patientContainer()]) {
    participant {
      actor {
        reference = "Patient/" + context.source[calendarEvent().patientContainer().id()]
      }
    }
  }

  if (context.source[calendarEvent().attendingDoctor()]) {
    participant {
      actor {
        reference = "Practitioner/" + context.source[calendarEvent().attendingDoctor().id()]
      }
    }
  }
}