package projects.mii_bielefeld

import common.AbstractGroovyScriptTest
import common.GroovyScriptTest
import common.TestResources
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.DateTimeType

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii_bielefeld/condition.groovy",
    contextMapsPath = "src/test/resources/projects/mii_bielefeld/Diagnosis.json"
)
class ConditionExportScriptTest extends AbstractGroovyScriptTest<Condition> {

  @GroovyScriptTest
  void thatConditionCodeIsExported(final Context context, final Condition resource) {
    assertTrue(resource.hasCode())

    final Coding icdCoding = resource.getCode().getCoding().find { it.getSystem() == "http://fhir.de/CodeSystem/bfarm/icd-10-gm" }

    assertNotNull(icdCoding)

    assertEquals(context.source[diagnosis().icdEntry().code()], icdCoding.getCode())
    assertEquals(context.source[diagnosis().icdEntry().catalogue().catalogueVersion()], icdCoding.getVersion())
  }

  @GroovyScriptTest
  void testThatRecordedDateIsSet(final Context context, final Condition resource) {
    assertTrue(resource.hasRecordedDate())

    assertEquals(new DateTimeType(context.source[diagnosis().creationDate()] as String).getValue(),
        resource.getRecordedDate())
  }

  @GroovyScriptTest
  void testThatOnsetDateTimeIsSet(final Context context, final Condition resource) {
    assertTrue(resource.hasOnsetDateTimeType())

    assertEquals(new DateTimeType(context.source[diagnosis().diagnosisDate().date()] as String).getValue(),
        resource.getOnsetDateTimeType().getValue())
  }

  @GroovyScriptTest
  void testThatAssertedDateIsSet(final Context context, final Condition resource) {
    assertTrue(resource.hasExtension("http://hl7.org/fhir/StructureDefinition/condition-assertedDate"))

    assertEquals(new DateTimeType(context.source[diagnosis().attestationDate().date()] as String).getValue(),
        ((DateTimeType) resource.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/condition-assertedDate").getValue()).getValue())
  }


  @GroovyScriptTest
  void testThatSubjectIsSet(final Context context, final Condition resource) {
    assertTrue(resource.hasSubject())
    assertEquals("Patient/" + context.source[diagnosis().patientContainer().id()], resource.getSubject().getReference())
  }
}
