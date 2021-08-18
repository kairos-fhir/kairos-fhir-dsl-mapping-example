package projects.mii.modul.person


import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by CXX Diagnosis
 * Specified by https://simplifier.net/medizininformatikinitiative-modulperson/profileconditiontodesursache
 * @author Jonas Küttner
 * @since v.1.10.0, CXX.v.3.18.2
 */

condition {
  if (context.source[diagnosis().causeOfDeath()]) {
    id = "Condition-DC/" + context.source[diagnosis().id()]
  } else {
    return
  }

  meta {
    source = "urn:centraxx"
    profile("https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Todesursache")
  }

  category {
    coding {
      system = "http://snomed.info/sct"
      code = "16100001"
    }
    coding {
      system = "http://loinc.org"
      code = "79378-6"
    }
  }
  code {
    coding {
      system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
      code = context.source[diagnosis().diagnosisCode()] as String
    }
    text = context.source[diagnosis().diagnosisText()] as String
  }

  subject {
    reference = "Patient/" +  context.source[diagnosis().patientContainer().id()]
  }
}

