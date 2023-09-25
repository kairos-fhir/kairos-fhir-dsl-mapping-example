package projects.patientfinder

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Appointment

import static de.kairos.fhir.centraxx.metamodel.RootEntities.calendarEvent

/**
 * Represented by a CXX CalendarEvent of a Patient
 * @author Mike Wähnert
 * @since v.1.23.0, CXX.v.2023.3.3
 */
appointment {

  id = "Appointment/" + context.source[calendarEvent().id()]

  identifier {
    system = FhirUrls.System.Calendar.EVENT_ID
    value = context.source[calendarEvent().eventId()]
  }

  status = context.source[calendarEvent().isDone()] ? Appointment.AppointmentStatus.FULFILLED : Appointment.AppointmentStatus.BOOKED;

  serviceCategory {
    coding {
      system = FhirUrls.System.Calendar.Type.BASE_URL;
      code = context.source[calendarEvent().type()]
    }
  }

  description = context.source[calendarEvent().title()]

  start {
    date = context.source[calendarEvent().startDate()]
  }

  end {
    date = context.source[calendarEvent().endDate()]
  }

  patientInstruction = context.source[calendarEvent().description()]

  participant {
    type {
      coding {
        system = FhirUrls.System.Calendar.PARTICIPANT_TYPE
        code = "PATIENT"
      }
    }
    actor {
      reference = "Patient/" + context.source[calendarEvent().patientContainer().id()]
    }

    status = Appointment.ParticipationStatus.ACCEPTED
  }

  extension {
    url = FhirUrls.Extension.Calendar.VISIBLE
    valueBoolean = context.source[calendarEvent().visible()]
  }

  extension {
    url = FhirUrls.Extension.Calendar.ALL_DAY
    valueBoolean = context.source[calendarEvent().allDay()]
  }

}