package common

import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptEngine
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner

import java.nio.charset.StandardCharsets


abstract class AbstractDslBuilderTest {

  protected static Fhir4ScriptRunner getFhir4ScriptRunner(final InputStream is, final String className) throws UnsupportedEncodingException {
    final InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)
    final Fhir4ScriptEngine engine = new Fhir4ScriptEngine()
    return engine.create(reader, className)
  }
}
