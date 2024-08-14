package projects.izi.frankfurt

import common.AbstractDslBuilderTest
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import groovy.json.JsonSlurper
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.Consent
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * Example test to run groovy mapping scripts with assumed test data.
 */
class ConsentTest extends AbstractDslBuilderTest {

  /**
   * If provision closures are nested inside a .each closure, the sub provisions are not populated.
   */
  @Test
  void testThatConsentIsExported() {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/izi/frankfurt/consent.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createTestData()

    // when: run your script
    final Consent consent = (Consent) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertEquals("unknown", ((CodeType) consent.getProvision().getPeriod().getEndElement()
        .getExtensionByUrl(FhirUrls.Extension.FhirDefaults.DATA_ABSENT_REASON).getValue()).getCode())
  }

  static Map<String, Object> createTestData() {
    final FileInputStream is = new FileInputStream("src/test/resources/projects/izi/frankfurt/testConsent.json")
    return new JsonSlurper().parse(is) as Map<String, Object>
  }
}
