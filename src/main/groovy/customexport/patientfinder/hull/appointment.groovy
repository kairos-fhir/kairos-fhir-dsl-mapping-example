package customexport.patientfinder.hull

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Appointment

import static de.kairos.fhir.centraxx.metamodel.RootEntities.calendarEvent

/**
 * Represented by a HDRP CalendarEvent of a Patient
 * @author Mike Wähnert
 * @since v.1.26.0, HDRP.v.2023.5.0
 */

final String APPOINTMENT_STATUS = "appointment.status"


final Map PROFILE_TYPES = [
    (APPOINTMENT_STATUS)    : LaborFindingLaborValue.STRING_VALUE
]

appointment {

  id = "Appointment/" + context.source[calendarEvent().id()]


  final def mapping = context.source[calendarEvent().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "Appointment_profile"
  }

  final Map<String, Object> lflvMap = getLflvMap(mapping, PROFILE_TYPES)

  if (lflvMap.containsKey(APPOINTMENT_STATUS)) {
    status = lflvMap[APPOINTMENT_STATUS]
  } else {
    status = Appointment.AppointmentStatus.BOOKED
  }


  identifier {
    value = context.source[calendarEvent().eventId()]
  }

  serviceCategory {
    coding {
      system = FhirUrls.System.Calendar.Type.BASE_URL
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

static Map<String, Object> getLflvMap(final def mapping, final Map<String, String> types) {
  final Map<String, Object> lflvMap = [:]
  if (!mapping) {
    return lflvMap
  }

  types.each { final String lvCode, final String lvType ->
    final def lflvForLv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == lvCode
    }

    if (lflvForLv && lflvForLv[lvType]) {
      lflvMap[(lvCode)] = lflvForLv[lvType]
    }
  }
  return lflvMap
}