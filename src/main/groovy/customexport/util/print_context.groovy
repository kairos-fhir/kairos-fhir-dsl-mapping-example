package customexport.util

import com.fasterxml.jackson.databind.ObjectMapper
import de.kairos.fhir.centraxx.metamodel.RootEntities



final String path = "<path>"

/**
 * can be used locally to write the context.source map as a json to the given path
 * useful for debugging
 */
condition {

  final def id = context.source[RootEntities.diagnosis().id()]

  new ObjectMapper().writerWithDefaultPrettyPrinter()
      .writeValue(new File(path + "/" + id + ".json"), context.source)

}