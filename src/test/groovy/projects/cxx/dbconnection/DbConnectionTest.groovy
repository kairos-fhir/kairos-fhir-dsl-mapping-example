package projects.cxx.dbconnection

import common.AbstractDslBuilderTest
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.centraxx.metamodel.PatientMasterDataAnonymous
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Patient
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals


class DbConnectionTest extends AbstractDslBuilderTest {

  @Test
  @Disabled("Configure DB connection in the script first!")
  void testThatDbConnectionFindsGender() throws IOException {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/cxx/custom/dbconnection/patient.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // when: run your script with test data
    final Patient patient = (Patient) runner.run(new Context(createTestData("897")))

    // then: test your assertions
    assertEquals("Patient/897", patient.getId())
    assertEquals(patient.getGender(), Enumerations.AdministrativeGender.MALE)
  }

  static Map<String, Object> createTestData(final String OID) {
    return Map.of(PatientMasterDataAnonymous.PATIENTCONTAINER,
        Map.of(PatientContainer.ID, OID))
  }
}
