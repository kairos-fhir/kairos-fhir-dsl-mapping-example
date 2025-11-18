package common

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport
import ca.uhn.fhir.validation.FhirValidator
import ca.uhn.fhir.validation.ResultSeverityEnum
import ca.uhn.fhir.validation.ValidationResult
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator
import org.hl7.fhir.r4.model.DomainResource

import javax.annotation.Nonnull
import javax.annotation.Nullable
import java.nio.file.Paths

import static org.junit.jupiter.api.Assertions.fail

/**
 * This class lazy initializes a FHIR Validator that can be used to validate HAPI resources against FHIR packages.
 * The FHIR packages must be added in a directory in 'src/test/resources' and the relative path must be supplied when constructing an instance
 * For example: 'source/test/resources/fhirpackages/mii' will contain the package files. The the Constructor must be called as follows:
 * <pre>
 *   {@code
 *   final def validator = new FhirResourceValidator("fhirpackages/mii")
 *   }
 * </pre>
 */
class FhirResourceValidator {

  private final FhirValidator validator

  FhirResourceValidator(@Nonnull final String packagePath) {
    this.validator = setUpValidator(packagePath)
  }

  @Nullable
  private static FhirValidator setUpValidator(@Nonnull final String packagePath) {

    final URL resourceUrl = FhirResourceValidator.class.classLoader.getResource(packagePath)

    if (resourceUrl == null){
      throw new IllegalStateException("The provided path $packagePath could not be found. " +
          "Please specify a path relative to 'src/test/resources' directory.")
    }

    final File packageDirFile = new File(resourceUrl.toURI())

    if (!packageDirFile.exists() || !packageDirFile.isDirectory()) {
      throw new IllegalStateException("The provided path for the FHIR packages is invalid. Path: ${packageDirFile.path}")
    }

    final FhirContext context = FhirContext.forR4()


    final NpmPackageValidationSupport npmPackageValidationSupport = new NpmPackageValidationSupport(context)

    packageDirFile.eachFile { final file ->
      npmPackageValidationSupport.loadPackageFromClasspath(Paths.get(packagePath).resolve(file.name).toString())
    }

    final ValidationSupportChain supportChain = new ValidationSupportChain(
        npmPackageValidationSupport,
        new DefaultProfileValidationSupport(context),
        new InMemoryTerminologyServerValidationSupport(context),
        new SnapshotGeneratingValidationSupport(context))

    final CachingValidationSupport validationSupport = new CachingValidationSupport(supportChain)

    final FhirValidator validator = context.newValidator()

    final FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupport)
    validator.registerValidatorModule(instanceValidator)
    instanceValidator.setNoTerminologyChecks(true)
    validator
  }

  void validate(@Nonnull final DomainResource resource) {
    final ValidationResult result = validator.validateWithResult(resource)

    final List<String> errors = []

    if (!result.isSuccessful()) {
      result.getMessages()
          .findAll { it.getSeverity() == ResultSeverityEnum.ERROR }
          .each { errors.add(it.toString()) }
    }

    if (!errors.isEmpty()) {
      final String message = errors.join("\n")
      fail("Resource Validation failed for entries:\n" + message)
    }
  }
}
