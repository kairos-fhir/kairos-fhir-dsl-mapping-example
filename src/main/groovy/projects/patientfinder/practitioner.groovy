package projects.patientfinder


import static de.kairos.fhir.centraxx.metamodel.RootEntities.attendingDoctor

/**
 * Represents a CXX AttendingDoctor for the US Core Resource Profile: US Core Practitioner Profile
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-practitioner.html
 *
 * Hints:
 * Practitioners/attending doctors of the triggered encounters/episodes of the triggered patients are exported since v.1.15.0, CXX.v.2022.1.0,
 * of medication and calendar events since v.1.26.0, CXX.v.2023.5.0
 *
 * @author Jonas Küttner
 * @since v.1.42.0, CXX.v.2024.4.0
 */
practitioner {

  id = "Practitioner/" + context.source[attendingDoctor().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner")
  }

  identifier {
    value = context.source[attendingDoctor().contact().syncId()]
  }

  name {
    family = context.source[attendingDoctor().contact().contactPersonLastName()]
    given(context.source[attendingDoctor().contact().contactPersonFirstName()] as String)
  }
}