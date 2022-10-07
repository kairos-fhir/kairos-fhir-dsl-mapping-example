package projects.gecco.crf

import common.AbstractDslBuilderTest
import de.kairos.fhir.centraxx.metamodel.Crf
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.StudyVisitItem
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import org.hl7.fhir.r4.model.Immunization
import org.junit.jupiter.api.Test

import static java.util.Arrays.asList
import static java.util.Collections.singletonMap
import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * Example test to run groovy mapping scripts with assumed test data.
 */
class ImmunizationExampleTest extends AbstractDslBuilderTest {

  @Test
  void testThatSortingFindsLastDate() throws IOException {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/test/resources/projects/gecco/crf/immunizationTest.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createTestData()

    // when: run your script
    final Immunization immunization = (Immunization) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertEquals("2021-07-04", immunization.getOccurrenceDateTimeType().getValue().format("yyyy-MM-dd"))
  }

  static Map<String, Object> createTestData() {
    final List<Map<String, Object>> list = asList(
        singletonMap(CrfItem.DATE_VALUE,
            singletonMap(PrecisionDate.DATE, "2021-07-03")),
        singletonMap(CrfItem.DATE_VALUE,
            singletonMap(PrecisionDate.DATE, "2021-07-02")),
        singletonMap(CrfItem.DATE_VALUE,
            singletonMap(PrecisionDate.DATE, "2021-07-04")),
        singletonMap(CrfItem.DATE_VALUE,
            singletonMap(PrecisionDate.DATE, "2021-07-01"))
    )
    return singletonMap(StudyVisitItem.CRF, singletonMap(Crf.ITEMS, list))
  }
}
