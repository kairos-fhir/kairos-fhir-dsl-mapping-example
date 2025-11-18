package common


import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Use on an implementation of {@link AbstractExportScriptTest}.
 * @param String groovyScriptPath Path to the Groovy script used to transform the given source map.
 * @param String contextMapsPath Path to the directory containing JSON files of entity maps used as test input
 * for the parametrized tests.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface TestResources {
  String groovyScriptPath()

  String contextMapsPath()
}
