package common

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport
import ca.uhn.fhir.validation.FhirValidator
import ca.uhn.fhir.validation.ValidationResult
import org.hl7.fhir.common.hapi.validation.support.CachingValidationSupport
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport
import org.hl7.fhir.common.hapi.validation.support.NpmPackageValidationSupport
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Reference
import org.junit.jupiter.api.Test

class ValidatorSupportTest {


  @Test
  void validate() {

    final FhirContext context = FhirContext.forR4();


    final NpmPackageValidationSupport npmPackageValidationSupport = new NpmPackageValidationSupport(context)

    npmPackageValidationSupport.loadPackageFromClasspath("fhirpackages/de.medizininformatikinitiative.kerndatensatz.person-2024.0.0.tgz");
    npmPackageValidationSupport.loadPackageFromClasspath("fhirpackages/de.basisprofil.r4-1.4.0.tgz")

    final ValidationSupportChain supportChain = new ValidationSupportChain(
        npmPackageValidationSupport,
        new DefaultProfileValidationSupport(context),
        new InMemoryTerminologyServerValidationSupport(context),
        new SnapshotGeneratingValidationSupport(context));

    final CachingValidationSupport validationSupport = new CachingValidationSupport(supportChain);

    final FhirValidator validator = context.newValidator()

    final FhirInstanceValidator instanceValidator = new FhirInstanceValidator(validationSupport);
    validator.registerValidatorModule(instanceValidator)
    instanceValidator.setNoTerminologyChecks(true)

    final Patient patient = new Patient()

    patient.getMeta().addProfile("https://www.medizininformatik-initiative.de/fhir/core/modul-person/StructureDefinition/Patient")

    patient.addIdentifier(
        new Identifier()
            .setType(
                new CodeableConcept()
                    .addCoding(new Coding()
                        .setSystem("http://fhir.de/CodeSystem/identifier-type-de-basis")
                        .setCode("GKV")
                    )
            )
            .setSystem("http://fhir.de/sid/gkv/kvid-10")
            .setValue("235234")
            .setAssigner(new Reference("Organization/123"))
    )

    final ValidationResult outcome = validator.validateWithResult(patient);

    outcome.getMessages().forEach {
      println(it.toString())
    }


  }

}
