package projects.dktk

import common.AbstractDslBuilderTest
import de.kairos.fhir.centraxx.metamodel.Diagnosis
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import org.hl7.fhir.r4.model.Condition
import org.junit.jupiter.api.Test

import static java.util.Collections.singletonMap
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

class ConditionDateTest extends AbstractDslBuilderTest {

  @Test
  void testThatSortingFindsLastDate() throws IOException {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/dktk/v2/condition.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createTestData()

    // when: run your script
    final Condition condition = (Condition) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertEquals("1799-12-31", condition.getOnsetDateTimeType().getValue().format("yyyy-MM-dd"))
  }

  static Map<String, Object> createTestData() {
    final Map<String, Object> testData = new HashMap<>();
    testData.put(Diagnosis.ICD_ENTRY, singletonMap(IcdEntry.CODE, 'C20'))
    testData.put(Diagnosis.DIAGNOSIS_DATE, singletonMap(PrecisionDate.DATE, "1799-12-31T23:53:28.000+00:53:28"))
    return testData;
  }

  @Test
  void testThatIcd10CodeFilterWorks() {
    assertTrue(hasRelevantCode("C18.1"))
    assertTrue(hasRelevantCode("D18.1"))
    assertTrue(hasRelevantCode("c18.1"))
    assertTrue(hasRelevantCode("d18.1"))
    assertFalse(hasRelevantCode("X18.1"))
    assertFalse(hasRelevantCode("y18.1"))
    assertFalse(hasRelevantCode(null))
  }

  private static boolean hasRelevantCode(final String icdCode) {
    return icdCode != null && (icdCode.toUpperCase().startsWith('C') || icdCode.toUpperCase().startsWith('D'))
  }

}
