package common


import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptEngine
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import groovy.json.JsonSlurper
import org.hl7.fhir.r4.model.DomainResource
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.provider.Arguments

import javax.annotation.Nonnull
import java.nio.charset.StandardCharsets
import java.util.stream.Stream

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractGroovyScriptTest<E extends DomainResource> {

  public static final String METHOD_SOURCE = "getTestData"
  private List<Arguments> mappingResults

  @BeforeAll
  void setUp() {

    final TestResources resources = this.class.getAnnotation(TestResources)

    if (resources == null) {
      throw new IllegalStateException("TestResources Annotation is missing ion the test class")
    }

    final def groovyPath = resources.groovyScriptPath()
    final def contextMapsPath = resources.contextMapsPath()

    if (groovyPath == null || contextMapsPath == null) {
      throw new IllegalArgumentException("The TestResourcesAnnotation parameters must be given.")
    }

    loadAndTransform(groovyPath, contextMapsPath)
  }

  private  void loadAndTransform(@Nonnull final String groovyPath, @Nonnull final String contextMapsPath) {
    final List<Map<String, Object>> contexts = createTestData(contextMapsPath)
    final Fhir4ScriptRunner runner = createRunner(groovyPath)
    mappingResults = contexts.collect {
      final Context context = new Context(it)
      final E resource = (E) runner.run(context)

      return Arguments.of(context, resource)
    }.asImmutable()
  }

  @Nonnull
  static List<Map<String, Object>> createTestData(@Nonnull final String contextMapsPath) throws FileNotFoundException {

    final FileInputStream is = new FileInputStream(contextMapsPath);
    return new JsonSlurper().parse(is) as List<Map<String, Object>>
  }

  @Nonnull
  static Fhir4ScriptRunner createRunner(@Nonnull final String groovyPath) {
    final FileInputStream is = new FileInputStream(groovyPath);
    return getFhir4ScriptRunner(is, "test")
  }

  @Nonnull
  private static Fhir4ScriptRunner getFhir4ScriptRunner(final InputStream is, final String className) throws UnsupportedEncodingException {
    final InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)
    final Fhir4ScriptEngine engine = new Fhir4ScriptEngine()
    return engine.create(reader, className)
  }

  protected Stream<Arguments> getTestData(){
    return mappingResults.stream()
  }

}
