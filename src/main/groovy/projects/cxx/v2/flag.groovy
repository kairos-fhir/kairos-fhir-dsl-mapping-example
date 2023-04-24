package projects.cxx.v2

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Flag

import static de.kairos.fhir.centraxx.metamodel.RootEntities.flexiFlagItem

/**
 * Represented by a CXX FlexiFlagItem
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.20.0, CXX.v.2023.1.1, CXX.v.2023.2.0
 */
flag {

  id = "Patient/" + context.source[flexiFlagItem().id()]

  status = Flag.FlagStatus.ACTIVE

  code {
    coding {
      system = FhirUrls.System.FlexiFlagItem.FlexiFlagDefEntry.BASE_URL
      code = context.source[flexiFlagItem().definition().code()] as String
    }
  }

  subject {
    reference = "Patient/" + context.source[flexiFlagItem().patientContainer().id()]
  }

  period {
    start {
      date = context.source[flexiFlagItem().validFrom()]
    }
    end {
      date = context.source[flexiFlagItem().validUntil()]
    }
  }

  if (context.source[flexiFlagItem().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[flexiFlagItem().episode().id()]
    }
  }

  extension {
    url = FhirUrls.Extension.FlexiFlagItem.FLAG_PRIVATE
    valueBoolean = context.source[flexiFlagItem().flagPrivate()]
  }

  extension {
    url = FhirUrls.Extension.FlexiFlagItem.COMMENTS
    valueString = context.source[flexiFlagItem().comment()]
  }
}
