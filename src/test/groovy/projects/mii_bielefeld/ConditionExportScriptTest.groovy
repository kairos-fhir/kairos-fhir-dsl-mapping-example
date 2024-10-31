package projects.mii_bielefeld

import common.AbstractDslBuilderTest
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import groovy.json.JsonSlurper
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.DateTimeType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class ConditionExportScriptTest extends AbstractDslBuilderTest {

  static Condition result
  static Context context

  @BeforeAll
  static void setUp() {
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/mii_bielefeld/condition.groovy");
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test");

    context = new Context(createTestData())

    result = (Condition) runner.run(context)
  }

  @Test
  void thatConditionCodeIsExported() {
    assertTrue(result.hasCode())

    final Coding icdCoding = result.getCode().getCoding().find { it.getSystem() == "http://fhir.de/CodeSystem/bfarm/icd-10-gm" }

    assertNotNull(icdCoding)

    assertEquals(context.source[diagnosis().icdEntry().code()], icdCoding.getCode())
    assertEquals(context.source[diagnosis().icdEntry().catalogue().catalogueVersion()], icdCoding.getVersion())
  }

  @Test
  void testThatRecordedDateIsSet() {
    assertTrue(result.hasRecordedDate())

    assertEquals(new DateTimeType(context.source[diagnosis().creationDate()] as String).getValue(),
        result.getRecordedDate())
  }

  @Test
  void testThatOnsetDateTimeIsSet() {
    assertTrue(result.hasOnsetDateTimeType())

    assertEquals(new DateTimeType(context.source[diagnosis().diagnosisDate().date()] as String).getValue(),
        result.getOnsetDateTimeType().getValue())
  }

  @Test
  void testThatAssertedDateIsSet() {
    assertTrue(result.hasExtension("http://hl7.org/fhir/StructureDefinition/condition-assertedDate"))

    assertEquals(new DateTimeType(context.source[diagnosis().attestationDate().date()] as String).getValue(),
        ((DateTimeType) result.getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/condition-assertedDate").getValue()).getValue())
  }


  @Test
  void testThatSubjectIsSet() {
    assertTrue(result.hasSubject())
    assertEquals("Patient/" + context.source[diagnosis().patientContainer().id()], result.getSubject().getReference())
  }


  static Map<String, Object> createTestData() throws FileNotFoundException {
    final FileInputStream is = new FileInputStream("src/test/resources/projects/mii_bielefeld/Diagnosis.json");
    return new JsonSlurper().parse(is) as Map<String, Object>
  }
}
