package projects.patientfinder.hull


import static de.kairos.fhir.centraxx.metamodel.RootEntities.attendingDoctor

/**
 * AttendingDoctor to PractitionerRole
 * v.2025.1.6
 */
practitionerRole {

  id = "PractitionerRole/" + context.source[attendingDoctor().id()]

  active = true

  specialty {
    coding {
      code = context.source[attendingDoctor().contact().position()] as String
      display = context.source[attendingDoctor().contact().position()] as String
    }
  }

  practitioner {
    reference = "Practitioner/" + context.source[attendingDoctor().id()]
  }
}