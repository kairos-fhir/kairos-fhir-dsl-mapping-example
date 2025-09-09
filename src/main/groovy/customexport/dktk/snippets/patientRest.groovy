package customexport.dktk.snippets


import groovy.json.JsonSlurper

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * This is an example how to use an external information queried by a REST call.
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 */
patient {

  id = "Patient/" + context.source[patientMasterDataAnonymous().patientContainer().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Patient-Pseudonym"
  }

  final def localId = context.source[patientMasterDataAnonymous().patientContainer().idContainer()]?.find {
    "Lokal" == it[ID_CONTAINER_TYPE]?.getAt(CODE) // TODO: site specific
  }

  if (localId) {
    identifier {
      value = localId[PSN]
      type {
        coding {
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/PseudonymArtCS"
          code = "Lokal" // A local site id has always type "Lokal"
          display = readFromSamplyMdr()
        }
      }
    }
  }

}

static String readFromSamplyMdr() {
  final URL postmanGet = new URL("https://mdr.ccpit.dktk.dkfz.de/v2/api/mdr/dataelements/urn%3Adktk%3Adataelement%3A91%3A1")
  final HttpURLConnection connection = postmanGet.openConnection() as HttpURLConnection
  connection.requestMethod = 'GET'
  final def json = connection.getInputStream().withCloseable { final inStream ->
    new JsonSlurper().parse(inStream as InputStream)
  }
  // returns "Die standortspezifische lokale DKTK-ID "
  return json.designations[0].definition
}
