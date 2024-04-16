package projects.mii.modul.biobanking

import de.kairos.fhir.centraxx.metamodel.AbstractIdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import groovy.json.JsonSlurper
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Specimen

import static de.kairos.fhir.centraxx.metamodel.Multilingual.LANGUAGE
import static de.kairos.fhir.centraxx.metamodel.Multilingual.NAME
import static de.kairos.fhir.centraxx.metamodel.RootEntities.abstractSample
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX SAMPLE
 * Specified by https://simplifier.net/medizininformatikinitiative-modulbiobank/profilespecimenbioprobe History.v.8 (draft)
 * Codings are customized in CXX. Therefore, the code system is unknown. If other codings are used in the local CXX system, the code systems must be adjusted.
 * In this example SPREC codes for the sample type, and container are translated requesting the the provided concept maps per HTTP.
 * TODO: NOTE: The script was written while the corresponding FHIR profile on simplifier.net was still in draft state. Changes in the profile might require adjustments in the script.
 * @author Jonas Küttner
 * @since KAIROS-FHIR-DSL.v.1.32.0, CXX.v.2024.2.1
 */

final String sampleTypeConceptMapUrl = "https://fhir.simplifier.net/MedizininformatikInitiative-ModulBiobank/ConceptMap/SPRECSampleTypeMap"
final String longTermStorageConceptMapUrl = "https://fhir.simplifier.net/MedizininformatikInitiative-ModulBiobank/ConceptMap/SPRECLongTermStorageMap"
specimen {

  id = "Specimen/" + context.source[sample().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ProfileSpecimenBioprobe"
  }

  if (context.source[abstractSample().diagnosis()]) {
    extension {
      url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ExtensionDiagnose"
      valueReference {
        reference = "Condition/" + context.source[sample().diagnosis().id()]
      }
    }
  }

  if (context.source[sample().organisationUnit()]) {
    extension {
      url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ExtensionVerwaltendeOrganisation"
      valueReference {
        reference = "Organization/" + context.source[sample().organisationUnit().id()]
      }
    }
  }


  context.source[sample().idContainer()].each { final def idObj ->
    identifier {
      type {
        coding {
          system = "urn:centraxx"
          code = idObj[AbstractIdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE] as String
          display = idObj[AbstractIdContainer.ID_CONTAINER_TYPE][IdContainerType.NAME] as String
        }
      }
      value = idObj[AbstractIdContainer.PSN]
    }
  }

  // Specimen status is customized in CXX. Exact meaning depends on implementation in CXX. Here, it is assumed that the codes of the codesystem
  // are implemented in CXX.
  status = mapSpecimenStatus(context.source[sample().sampleStatus().code()] as String)

  if (context.source[sample().sampleType()]) {
    type {
      final TranslationResult sampleTypeTranslationResult = translateConceptMap(context.source[sample().sampleType().sprecCode()] as String, sampleTypeConceptMapUrl)
      if (sampleTypeTranslationResult.code) {
        coding {
          system = "http://snomed.info/sct"
          code = sampleTypeTranslationResult.code
          display = sampleTypeTranslationResult.display
        }
      }
    }
  }
  subject {
    reference = "Patient/" + context.source[sample().patientContainer().id()]
  }

  receivedTime {
    date = context.source[sample().receiptDate()]?.getAt(PrecisionDate.DATE)
  }

  if (context.source[sample().parent()]) {
    parent {
      reference = "Specimen/" + context.source[sample().parent().id()]
    }
  }

  collection {
    collectedDateTime = context.source[sample().samplingDate().date()]
    if (context.source[sample().orgSample()]) {
      bodySite {
        //Organs are specified user-defined in CXX. sct coding only applies, when used for coding in CXX
        coding {
          system = "http://snomed.info/sct"
          code = context.source[sample().orgSample().code()]
          display = context.source[sample().orgSample().multilinguals()]?.find { it[LANGUAGE] == "de" }?.getAt(NAME)
        }
      }
    }
  }

  if (context.source[sample().sampleLocation()]) {
    processing {
      procedure {
        coding = [new Coding("https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/CodeSystem/Probenlagerung",
            "LAGERUNG",
            "Lagerung einer Probe")]
      }
    }
    timeDateTime = context.source[sample().repositionDate()]?.getAt(PrecisionDate.DATE)
  }

  if (context.source[sample().receptable()]) {
    container {
      type {
        final TranslationResult longTermStorageTranslationResult = translateConceptMap(context.source[sample().receptable().sprecCode()] as String,
            longTermStorageConceptMapUrl)
        if (longTermStorageTranslationResult.code) {
          coding {
            system = "http://snomed.info/sct"
            code = longTermStorageTranslationResult.code
            display = longTermStorageTranslationResult.display
          }
        }
      }
      capacity {
        value = context.source[sample().receptable().size()]
        unit = context.source[sample().receptable().volume()]
      }
      specimenQuantity {
        value = context.source[sample().restAmount().amount()]
        unit = context.source[sample().restAmount().unit()]
      }
      additiveReference {
        if (context.source[sample().sprecPrimarySampleContainer()]) {
          additiveCodeableConcept {
            coding {
              system = "https://doi.org/10.1089/bio.2017.0109/type-of-primary-container"
              code = context.source[sample().sprecPrimarySampleContainer().sprecCode()]
            }
          }
        }
        if (context.source[sample().stockType()]) {
          additiveCodeableConcept {
            coding {
              system = "https://doi.org/10.1089/bio.2017.0109/type-of-primary-container"
              code = context.source[sample().stockType().sprecCode()]
            }
          }
        }
      }
      /*additiveReference {
        if (context.source[sample().sprecPrimarySampleContainer()]) {
          final TranslationResult sprecPrimaryContainerTranslationResult = translateConceptMap(context.source[sample().sprecPrimarySampleContainer().sprecCode()] as String, sprecPrimaryContainerConceptMapUrl)
          if (sprecPrimaryContainerTranslationResult.code) {
            additiveCodeableConcept {
              system = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/ValueSet/additive"
              code = sprecPrimaryContainerTranslationResult.code
              display = sprecPrimaryContainerTranslationResult.display
            }
          }
        }*/
      /*if (context.source[sample().stockType()]) {
        final TranslationResult sprecFixationTypeTranslationResult = translateConceptMap(context.source[sample().stockType().sprecCode()] as String, sprecFixationTypeConceptMapUrl)
        if (sprecFixationTypeTranslationResult.code) {
          additiveCodeableConcept {
            system = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/ValueSet/additive"
            code = sprecFixationTypeTranslationResult.code
            display = sprecFixationTypeTranslationResult.display
          }
        }
      }*/
    }
  }
  note {
    text = context.source[sample().note()] as String
  }
}

static Specimen.SpecimenStatus mapSpecimenStatus(final String specimenStatus) {
  switch (specimenStatus) {
    case "available": return Specimen.SpecimenStatus.AVAILABLE
    case "unavailable": return Specimen.SpecimenStatus.UNAVAILABLE
    case "unsatisfactory": return Specimen.SpecimenStatus.UNSATISFACTORY
    case "entered-in-error": return Specimen.SpecimenStatus.ENTEREDINERROR
    default: return Specimen.SpecimenStatus.NULL
  }
}

class TranslationResult {
  String code
  String display
  String equivalence

  TranslationResult(final code, final display, final equivalence) {
    this.code = code
    this.display = display
    this.equivalence = equivalence
  }
}

private static TranslationResult translateConceptMap(final String code, final String queryUrl) {
  final String httpMethod = "GET"
  final URL url = new URL(queryUrl)

  final HttpURLConnection connection = url.openConnection() as HttpURLConnection
  connection.setRequestMethod(httpMethod)
  connection.setRequestProperty("Accept", "application/json")

  validateResponse(connection.getResponseCode(), httpMethod, url)
  final def json = connection.getInputStream().withCloseable { final inStream ->
    new JsonSlurper().parse(inStream as InputStream)
  }

  final def snomed_group = json["group"]?.find { final def group ->
    "http://snomed.info/sct" == group["target"]
  }

  final def target = snomed_group["element"]?.find { final def element ->
    code == element["code"]
  }?.getAt("target") as Map<String, List>

  return new TranslationResult(target["code"][0], target["display"][0], target["equivalence"][0])
}

/**
 * Validates the HTTP response. If a response is not valid (not 200), an exception is thrown and the transformation ends.
 */
private static void validateResponse(final int httpStatusCode, final String httpMethod, final URL url) {
  final int expectedStatusCode = 200
  if (httpStatusCode != expectedStatusCode) {
    throw new IllegalStateException("'" + httpMethod + "' request on '" + url + "' returned status code: " + httpStatusCode + ". Expected: " + expectedStatusCode)
  }
}



