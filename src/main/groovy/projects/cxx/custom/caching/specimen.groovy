package projects.cxx.custom.caching

import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX AbstractSample
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.3.17.2
 */
specimen {

  id = "Specimen/" + context.source[sample().id()]

  final def idContainer = context.source[sample().patientContainer().idContainer()]?.find {
    "MPI" == it[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  subject {
    reference = "Patient/" + getPatientId(idContainer[IdContainer.PSN] as String)
  }
}

static String getPatientId(final String mpi) {
  final String cacheFilePath = "C:/centraxx-home/groovy-cache/mpiToFhirId.json"
  final Map<String, String> mpiToFhirId = loadCache(cacheFilePath)
  final String fhirId = mpiToFhirId.computeIfAbsent(mpi, { final k -> queryFhirIdFromDiz(mpi) })
  persistCache(cacheFilePath, mpiToFhirId)
  return fhirId
}

static String queryFhirIdFromDiz(final String mpi) {
  Thread.sleep(1000) // simulate a long running task
  return UUID.randomUUID().toString()
}

static Map<String, String> loadCache(final String cacheFilePath) {
  final File cacheFile = new File(cacheFilePath)
  return !cacheFile.exists() ? [:] : (new JsonSlurper().parse(cacheFile) as Map<String, String>)
}

static void persistCache(final String cacheFilePath, final Map<String, String> cacheMap) {
  final File cacheFile = new File(cacheFilePath)
  cacheFile.write(JsonOutput.prettyPrint(JsonOutput.toJson(cacheMap)), "UTF-8")
}