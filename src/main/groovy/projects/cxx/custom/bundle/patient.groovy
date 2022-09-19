package projects.cxx.custom.bundle

import ca.uhn.fhir.context.FhirContext
import org.apache.commons.io.output.FileWriterWithEncoding
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Enumerations

import java.nio.charset.StandardCharsets
import java.nio.file.Paths

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.2022.3.0
 */

patient {

  final String uuid = UUID.randomUUID().toString()
  final Bundle bundleToExport = bundle {
    id = "Bundle/" + uuid

    entry {
      final def fullUrl = "Patient/TwinA-" + context.source[patientMasterDataAnonymous().patientContainer().id()]
      resource {
        patient {
          id = fullUrl
          gender = Enumerations.AdministrativeGender.MALE
          birthDate {
            date = context.source[patientMasterDataAnonymous().birthdate().date()]
          }
        }
      }
      request {
        method = Bundle.HTTPVerb.POST
        url = fullUrl
      }
    }
    entry {
      final def fullUrl = "Patient/TwinB-" + context.source[patientMasterDataAnonymous().patientContainer().id()]
      resource {
        patient {
          id = fullUrl
          gender = Enumerations.AdministrativeGender.FEMALE
          birthDate {
            date = context.source[patientMasterDataAnonymous().birthdate().date()]
          }
        }
      }
      request {
        method = Bundle.HTTPVerb.POST
        url = fullUrl
      }
    }
  }

  final File bundleFile = Paths.get("C:/centraxx-home/fhir-custom-export/experiment/", uuid + ".json").toFile()
  bundleFile.createNewFile()
  final Writer writer = new FileWriterWithEncoding(bundleFile, StandardCharsets.UTF_8)
  FhirContext.forR4()
      .newJsonParser()
      .setPrettyPrint(true)
      .encodeResourceToWriter(bundleToExport, writer)
  writer.close()
}

