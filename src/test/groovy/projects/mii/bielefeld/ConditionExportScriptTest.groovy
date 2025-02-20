package projects.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import common.Validate
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.DateTimeType

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii/bielefeld/condition.groovy",
    contextMapsPath = "src/test/resources/projects/mii/bielefeld/condition.json"
)
@Validate(packageDir = "src/test/resources/fhirpackages")
class ConditionExportScriptTest extends AbstractExportScriptTest<Condition> {

  @ExportScriptTest
  void thatConditionCodeIsExported(final Context context, final Condition resource) {
    assertTrue(resource.hasCode())

    final Coding icdCoding = resource.getCode().getCoding().find { it.getSystem() == "http://fhir.de/CodeSystem/bfarm/icd-10-gm" }

    assertNotNull(icdCoding)

    assertEquals(context.source[diagnosis().icdEntry().code()], icdCoding.getCode())
    assertEquals(context.source[diagnosis().icdEntry().catalogue().catalogueVersion()], icdCoding.getVersion())
  }

  @ExportScriptTest
  void testThatRecordedDateIsSet(final Context context, final Condition resource) {
    assertTrue(resource.hasRecordedDate())

    assertEquals(new DateTimeType(context.source[diagnosis().creationDate()] as String).getValue(),
        resource.getRecordedDate())
  }

  @ExportScriptTest
  void testThatOnsetDateTimeIsSet(final Context context, final Condition resource) {
    assertTrue(resource.hasOnsetDateTimeType())

    assertEquals(new DateTimeType(context.source[diagnosis().diagnosisDate().date()] as String).getValue(),
        resource.getOnsetDateTimeType().getValue())
  }

  @ExportScriptTest
  void testThatAssertedDateIsSet(final Context context, final Condition resource) {

    assumeTrue(context.source[diagnosis().attestationDate()] && context.source[diagnosis().attestationDate().date()])

    assertTrue(resource.hasExtension("http://hl7.org/fhir/StructureDefinition/condition-assertedDate"))

    assertEquals(new DateTimeType(context.source[diagnosis().attestationDate().date()] as String).getValue(),
        ((DateTimeType) resource.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/condition-assertedDate").getValue()).getValue())
  }


  @ExportScriptTest
  void testThatSubjectIsSet(final Context context, final Condition resource) {
    assertTrue(resource.hasSubject())
    assertEquals("Patient/" + context.source[diagnosis().patientContainer().id()], resource.getSubject().getReference())
  }
}
