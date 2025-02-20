package common


import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Use on an implementation of {@link AbstractExportScriptTest}.
 * @param String groovyScriptPath Path to the Groovy script used to transform the given source map.
 * @param String contextMapsPath Path to the JSON file containing an array of serialized maps representing the CXX entity object graph.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface TestResources {
  String groovyScriptPath()

  String contextMapsPath()
}
