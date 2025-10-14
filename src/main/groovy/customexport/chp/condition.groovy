package customexport.chp

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.Multilingual.LANGUAGE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

condition {

  id = "Condition/" + context.source[diagnosis().id()]

  subject {
    reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
  }

  encounter {
    reference = "Encounter/" + context.source[diagnosis().episode().id()]
  }


  recordedDate {
    date = context.source[diagnosis().creationDate()]
  }

  if (context.source[diagnosis().diagnosisDate()] != null && context.source[diagnosis().diagnosisDate().date()] != null) {
    onsetDateTime {
      date = context.source[diagnosis().diagnosisDate().date()]
    }
  }

  final def diagnosisId = context.source[diagnosis().diagnosisId()]
  if (diagnosisId) {
    identifier {
      system = "https://fhir.centraxx.de/system/diagnosis/diagnosisId"
      value = diagnosisId
    }
  }

  code {
    if (context.source[diagnosis().icdEntry()]) {
      coding {
        system = "https://fhir.centraxx.de/system/" + context.source[diagnosis().icdEntry().catalogue().name()]
        code = context.source[diagnosis().icdEntry().code()] as String
        version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()]
        display = context.source[diagnosis().icdEntry().preferredLong()]
      }
    } else if (context.source[diagnosis().userDefinedCatalogEntry()]) {
      coding {
        system = "https://fhir.centraxx.de/system/" + context.source[diagnosis().userDefinedCatalogEntry().catalog().code()]
        version = context.source[diagnosis().userDefinedCatalogEntry().catalog().version()]
        code = context.source[diagnosis().userDefinedCatalogEntry().code()] as String
        display = context.source[diagnosis().userDefinedCatalogEntry().multilinguals()]
            ?.find { it[LANGUAGE] == "en" && it[Multilingual.SHORT_NAME] != null }?.getAt(Multilingual.SHORT_NAME)
      }
    }
  }

  final String diagNote = context.source[diagnosis().comments()] as String
  if (diagNote) {
    note {
      text = diagNote
    }
  }
}
