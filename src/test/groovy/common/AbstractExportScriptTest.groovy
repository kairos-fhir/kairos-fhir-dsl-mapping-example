package common

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport
import ca.uhn.fhir.validation.FhirValidator
import ca.uhn.fhir.validation.ResultSeverityEnum
import ca.uhn.fhir.validation.ValidationResult
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptEngine
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import groovy.json.JsonSlurper
import org.apache.commons.lang3.tuple.Pair
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator
import org.hl7.fhir.r4.model.DomainResource
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.provider.Arguments

import javax.annotation.Nonnull
import javax.annotation.Nullable
import java.nio.charset.StandardCharsets
import java.util.stream.Stream

import static org.junit.jupiter.api.Assertions.fail

/**
 * This test class provides test data for parameterized tests to verify the results of Groovy transformations.
 * The source maps must be provided as JSON files. The FHIR Custom Export transforms a map, accessible in the Groovy
 * transformation script via the 'context.source' variable, into a FHIR resource.
 * <br><br>
 * This test class reads an array of these source maps from JSON files and transforms them using the Groovy script under test
 * into a list of resulting FHIR resources. The Groovy script and the source map JSON file are loaded from the paths specified
 * by the {@link TestResources} annotation on the implementing test class.
 * <br><br>
 * Each test method that runs over the set of source map and resulting FHIR resource pairs needs to be annotated with
 * {@link ExportScriptTest} and declare two arguments for the {@link Context} context and the resource. This annotation indicates
 * that the method is a parameterized test and registers the {@link AbstractExportScriptTest#getTestData} method as the source
 * for the method arguments.
 * <br><br>
 * Optionally, the {@link Validate} annotation can be used to validate the resulting resource against the profiles of a given FHIR package.
 * FHIR packages can be downloaded for specific FHIR projects from https://simplifier.net/
 * @param <E>            the type parameter for the FHIR resource.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractExportScriptTest<E extends DomainResource> {

  public static final String METHOD_SOURCE = "getTestData"
  private List<Arguments> mappingResults

  @BeforeAll
  void setUp() {

    final TestResources resources = this.class.getAnnotation(TestResources)

    final Validate validate = this.class.getAnnotation(Validate)

    final FhirValidator validator = setUpValidator(validate)


    if (resources == null) {
      throw new IllegalStateException("TestResources Annotation is missing ion the test class")
    }

    final def groovyPath = resources.groovyScriptPath()
    final def contextMapsPath = resources.contextMapsPath()

    if (groovyPath == null || contextMapsPath == null) {
      throw new IllegalArgumentException("The TestResourcesAnnotation parameters must be given.")
    }

    loadAndTransform(groovyPath, contextMapsPath, validator)
  }

  private void loadAndTransform(@Nonnull final String groovyPath,
                                @Nonnull final String contextMapsPath,
                                final FhirValidator validator) throws FileNotFoundException {
    final List<Map<String, Object>> contexts = createTestData(contextMapsPath)
    final Fhir4ScriptRunner runner = createRunner(groovyPath)
    final List<Pair> contextResourcePairs = contexts.collect {
      final Context context = new Context(it)
      final E resource = (E) runner.run(context)
      return Pair.of(context, resource)
    }.findAll {
      it.getRight().hasId()
    }

    if (validator != null) {
      validateResources(contextResourcePairs, validator)
    }

    mappingResults = contextResourcePairs.collect {
      Arguments.of(it.getLeft(), it.getRight())
    }

  }

  private static void validateResources(final List<Pair> contextResourcePairs, final validator) {
    final Map<Integer, String> validationErrors = new HashMap<>();
    contextResourcePairs.eachWithIndex { final Pair<Context, E> entry, final int i ->
      final ValidationResult result = validator.validateWithResult(entry.getRight())
      if (!result.isSuccessful()) {
        final def messages = result.getMessages()
            .findAll { it.getSeverity() == ResultSeverityEnum.ERROR }
            .collect { it.toString() }
            .join("\n")

        validationErrors.put(i, messages)
      }
    }

    if (!validationErrors.isEmpty()) {
      final messages = validationErrors
          .collect { "Context at index " + it.key + " " + "-" * 80 +"\n" + it.value }
          .join("\n\n")

      fail("Resource Validation failed for entries:\n" + messages)
    }
  }

  @Nonnull
  static List<Map<String, Object>> createTestData(@Nonnull final String contextMapsPath) throws FileNotFoundException {

    final FileInputStream is = new FileInputStream(contextMapsPath)
    return new JsonSlurper().parse(is) as List<Map<String, Object>>
  }

  @Nonnull
  static Fhir4ScriptRunner createRunner(@Nonnull final String groovyPath) {
    final FileInputStream is = new FileInputStream(groovyPath)
    return getFhir4ScriptRunner(is, "test")
  }

  @Nonnull
  private static Fhir4ScriptRunner getFhir4ScriptRunner(final InputStream is, final String className) throws UnsupportedEncodingException {
    final InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)
    final Fhir4ScriptEngine engine = new Fhir4ScriptEngine()
    return engine.create(reader, className)
  }

  protected Stream<Arguments> getTestData() {
    return mappingResults.stream()
  }

  @Nullable
  private static FhirValidator setUpValidator(final Validate validate) {

    if (validate == null) {
      return null
    }

    final FhirContext context = FhirContext.forR4()


    final NpmPackageValidationSupport npmPackageValidationSupport = new NpmPackageValidationSupport(context)


    final File packageDirFile = new File(validate.packageDir())
    packageDirFile.eachFile { final file ->
      npmPackageValidationSupport.loadPackageFromClasspath("${packageDirFile.name}/${file.name}")
    }

    final ValidationSupportChain supportChain = new ValidationSupportChain(
        npmPackageValidationSupport,
        new DefaultProfileValidationSupport(context),
        new InMemoryTerminologyServerValidationSupport(context),
        new SnapshotGeneratingValidationSupport(context))

    final CachingValidationSupport validationSupport = new CachingValidationSupport(supportChain);

    final FhirValidator validator = context.newValidator()

    final FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupport);
    validator.registerValidatorModule(instanceValidator)
    instanceValidator.setNoTerminologyChecks(true)

    return validator
  }
}
