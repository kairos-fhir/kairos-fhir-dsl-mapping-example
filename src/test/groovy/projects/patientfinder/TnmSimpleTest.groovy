package projects.patientfinder

import common.AbstractDslBuilderTest
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.Tnm
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import org.hl7.fhir.r4.model.Observation
import org.junit.jupiter.api.Test

import static java.util.Collections.singletonMap
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse

class TnmSimpleTest extends AbstractDslBuilderTest {

  @Test
  void testThatFakeEpisodeIsNotFiltered() throws IOException {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/patientfinder/tnmSimple.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createValidTestData()

    // when: run your script
    final Observation observation = (Observation) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertEquals("Observation/Tnm-123", observation.getId())
    assertEquals("Encounter/456", observation.getEncounter().getReference())
  }

  @Test
  void testThatFakeEpisodeIsFilteredByEntitySource() throws IOException {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/patientfinder/tnmSimple.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createValidTestData()
    final Map<String, Object> episodeMap = new HashMap()
    episodeMap.put(Episode.ID, "456")
    episodeMap.put(Episode.ENTITY_SOURCE, "COSD")
    episodeMap.put(Episode.ID_CONTAINER, [singletonMap(IdContainer.PSN, "123456")])
    testDataMap.put(Tnm.EPISODE, episodeMap)

    // when: run your script
    final Observation observation = (Observation) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertEquals("Observation/Tnm-123", observation.getId())
    assertFalse(observation.hasEncounter())
  }

  @Test
  void testThatFakeEpisodeIsFilteredByIdContainerPsn() throws IOException {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/patientfinder/tnmSimple.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createValidTestData()
    final Map<String, Object> episodeMap = new HashMap()
    episodeMap.put(Episode.ID, "456")
    episodeMap.put(Episode.ENTITY_SOURCE, "CENTRAXX")
    episodeMap.put(Episode.ID_CONTAINER, [singletonMap(IdContainer.PSN, "FAKE-123456")])
    testDataMap.put(Tnm.EPISODE, episodeMap)

    // when: run your script
    final Observation observation = (Observation) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertEquals("Observation/Tnm-123", observation.getId())
    assertFalse(observation.hasEncounter())
  }

  static Map<String, Object> createValidTestData() {
    final Map<String, Object> episodeMap = new HashMap()
    episodeMap.put(Episode.ID, "456")
    episodeMap.put(Episode.ENTITY_SOURCE, "CENTRAXX")
    episodeMap.put(Episode.ID_CONTAINER, [singletonMap(IdContainer.PSN, "123456")])

    final Map<String, Object> map = new HashMap<>()
    map.put(Tnm.ID, "123")
    map.put(Tnm.EPISODE, episodeMap)
    return map
  }
}
