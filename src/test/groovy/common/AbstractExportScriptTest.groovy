package common

import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptEngine
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import groovy.json.JsonSlurper
import org.hl7.fhir.r4.model.DomainResource
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Named
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.provider.Arguments
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.annotation.Nonnull
import java.nio.charset.StandardCharsets
import java.util.stream.Stream

/**
 * This class provides a framework for end-to-end testing of Groovy transformation scripts.
 * The Custom-Export process takes a Groovy script and an entity map as input, producing a FHIR resource as output.
 * <br><br>
 * Classes extending this abstract class must be annotated with the {@link TestResources} annotation,
 * which specifies the path to the Groovy script under test and the JSON files containing the entity maps used as test inputs.
 * <br><br>
 * For each entity map in the specified directory, this class creates a {@link Context} and initializes a Groovy
 * Transformation engine for the provided script. The script transforms each context into a HAPI FHIR resource.
 * <br><br>
 * The transformed contexts and their resulting FHIR resources are provided by {@link AbstractExportScriptTest#getTestData}.
 * Test methods annotated with {@link ExportScriptTest} and accepting two parameters of type {@link Context} and E
 * are executed for each pair of context and resulting resource.
 * <br><br>
 * The {@link AbstractExportScriptTest#getValidator} method can be used to lazily initialize a {@link FhirResourceValidator},
 * which can be utilized to validate the resulting resources against structure definitions in the provided FHIR packages.
 *
 * @param <E>   The type parameter representing the FHIR resource.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractExportScriptTest<E extends DomainResource> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractExportScriptTest.name)
  public static final String METHOD_SOURCE = "getTestData"
  private List<Arguments> mappingResults

  private FhirResourceValidator validator

  @BeforeAll
  void setUp() {
    LOG.info("✅ setting up ${this.class.simpleName}")
    final TestResources resources = this.class.getAnnotation(TestResources)

    if (resources == null) {
      throw new IllegalStateException("@${TestResources.class.simpleName} annotation is missing on ${this.class.simpleName}")
    }

    final def groovyPath = resources.groovyScriptPath()
    final def contextMapsPath = resources.contextMapsPath()

    if (groovyPath == null || contextMapsPath == null) {
      throw new IllegalArgumentException("The TestResourcesAnnotation parameters must be given.")
    }

    loadAndTransform(groovyPath, contextMapsPath)
  }

  /**
   * @param path
   * @return
   */
  @Nonnull
  protected FhirResourceValidator getValidator(@Nonnull final String path) {
    if (validator == null) {
      validator = new FhirResourceValidator(path)
    }
    return validator
  }

  private void loadAndTransform(@Nonnull final String groovyPath,
                                @Nonnull final String contextMapsPath) throws FileNotFoundException {

    final Map<String, Map<String, Object>> contextMaps = createTestData(contextMapsPath)

    final Fhir4ScriptRunner runner = createRunner(groovyPath)

    final List<ArgumentContainer<E>> arguments = contextMaps.collect { final fileName, final contextMap ->
      final Context context = new Context(contextMap)
      final E resource = (E) runner.run(context)
      return new ArgumentContainer(fileName, context, resource)
    }.findAll {
      it.resource.hasId()
    }

    mappingResults = arguments.collect {
      Arguments.of(new NamedArg(it.fileName, it.context), it.resource)
    }

    LOG.info("✅ Loaded ${mappingResults.size()} test cases.")

  }

  @Nonnull
  static Map<String, Map<String, Object>> createTestData(@Nonnull final String contextMapsPath) throws FileNotFoundException {
    LOG.info("🔍 Loading test data from $contextMapsPath")

    final File contextMapDir = new File(contextMapsPath)

    if (!contextMapDir.exists()) {
      throw new IllegalStateException("The given contextMapPath does not exist. Path: ${contextMapDir.path}")
    }

    final Map<String, Map<String, Object>> contextMapByFile = new HashMap<>()

    contextMapDir.eachFileMatch(~/.*\.json/) {
      final def json = new JsonSlurper().parse(it)

      if (json instanceof List) {
        throw new IllegalStateException("The given context map JSON file must contain a representation of a single entity," +
            " not an array of entity maps.")
      }

      contextMapByFile.put(it.name, json as Map<String, Object>)
    }

    return contextMapByFile
  }

  @Nonnull
  static Fhir4ScriptRunner createRunner(@Nonnull final String groovyPath) {
    final FileInputStream is = new FileInputStream(groovyPath)
    return getFhir4ScriptRunner(is, "test")
  }

  @Nonnull
  private static Fhir4ScriptRunner getFhir4ScriptRunner(@Nonnull final InputStream is,
                                                        @Nonnull final String className) throws UnsupportedEncodingException {
    final InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)
    final Fhir4ScriptEngine engine = new Fhir4ScriptEngine()
    return engine.create(reader, className)
  }

  @Nonnull
  protected Stream<Arguments> getTestData() {
    return mappingResults.stream()
  }

  class ArgumentContainer<R extends DomainResource> {
    private final String fileName
    private final Context context
    private final R resource

    ArgumentContainer(@Nonnull final String fileName, @Nonnull final Context context, @Nonnull final R resource) {
      this.fileName = fileName
      this.context = context
      this.resource = resource
    }
  }

  static class NamedArg implements Named<Context> {
    private final String name
    private final Context context

    NamedArg(@Nonnull final String name, @Nonnull final Context context) {
      this.name = name
      this.context = context
    }

    @Override
    String getName() {
      return name
    }

    @Override
    Context getPayload() {
      return context
    }
  }
}
