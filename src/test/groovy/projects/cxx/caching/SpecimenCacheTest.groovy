package projects.cxx.caching

import common.AbstractDslBuilderTest
import de.kairos.fhir.centraxx.metamodel.AbstractSample
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import org.hl7.fhir.r4.model.Specimen
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotEquals

/**
 * Example test to run a script multiple times for a common cache.
 */
class SpecimenCacheTest extends AbstractDslBuilderTest {

  @Test
  @Disabled("Create cache file first")
  void testThatCSortingFindsLastDate() throws IOException {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/cxx/custom/caching/specimen.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // when: run your script with test data
    final Specimen specimen1 = (Specimen) runner.run(new Context(createTestData("MPI-111")))
    final Specimen specimen2 = (Specimen) runner.run(new Context(createTestData("MPI-222")))
    final Specimen specimen3 = (Specimen) runner.run(new Context(createTestData("MPI-111")))

    // then: test your assertions
    assertEquals(specimen1.getSubject().getReference(), specimen3.getSubject().getReference())
    assertNotEquals(specimen1.getSubject().getReference(), specimen2.getSubject().getReference())
  }

  static Map<String, Object> createTestData(final String mpi) {
    return Map.of(AbstractSample.PATIENTCONTAINER,
        Map.of(PatientContainer.ID_CONTAINER,
            List.of(Map.of(IdContainer.ID_CONTAINER_TYPE,
                Map.of(IdContainerType.CODE, "MPI"),
                IdContainer.PSN, mpi))))
  }
}
