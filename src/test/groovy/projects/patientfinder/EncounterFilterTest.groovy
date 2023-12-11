package projects.patientfinder

import common.AbstractDslBuilderTest
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import org.hl7.fhir.r4.model.Encounter
import org.junit.jupiter.api.Test

import static java.util.Collections.singletonMap
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse

class EncounterFilterTest extends AbstractDslBuilderTest {

  @Test
  void testThatFakeEpisodeIsNotFiltered() throws IOException {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/patientfinder/encounter.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createValidTestData()

    // when: run your script
    final Encounter encounter = (Encounter) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertEquals("Encounter/123", encounter.getId())
  }

  @Test
  void testThatFakeEpisodeIsFilteredByEntitySource() throws IOException {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/patientfinder/encounter.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createValidTestData()
    testDataMap.put(Episode.ENTITY_SOURCE, "SACT")

    // when: run your script
    final Encounter encounter = (Encounter) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertFalse(encounter.hasId())
  }

  @Test
  void testThatFakeEpisodeIsFilteredByIdContainerPsn() throws IOException {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/patientfinder/encounter.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createValidTestData()
    testDataMap.put(Episode.ID_CONTAINER, [singletonMap(IdContainer.PSN, "FAKE-123456")])

    // when: run your script
    final Encounter encounter = (Encounter) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertFalse(encounter.hasId())
  }

  static Map<String, Object> createValidTestData() {
    final Map<String, Object> map = new HashMap<>()
    map.put(Episode.ID, "123")
    map.put(Episode.ENTITY_SOURCE, "CENTRAXX")
    map.put(Episode.ID_CONTAINER, [singletonMap(IdContainer.PSN, "123456")])
    return map
  }
}
