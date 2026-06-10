package customexport.izi.hannover


import common.AbstractDslBuilderTest
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import org.hl7.fhir.r4.model.Patient
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class PatientBirthDateTest extends AbstractDslBuilderTest {

  @Test
  void testThatBirthDateIsExportedCorrectly() {
    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/customexport/izi/hannover/patient.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final def testDataMap = [
        id       : 123L,
        birthdate: [
            date     : "1998-09-19T09:23:12.000+02:00",
            precision: "EXACT"
        ]
    ]

    // when: run your script
    final Patient patient = (Patient) runner.run(new Context(testDataMap))

    assertEquals("1998-09", patient.getBirthDateElement().getValueAsString())

  }

  @Test
  void testThatDeathDateIsExportedCorrectly() {
    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/customexport/izi/hannover/patient.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final def testDataMap = [
        id       : 123L,
        dateOfDeath: [
            date     : "2025-03-16T09:23:12.000+02:00",
            precision: "EXACT"
        ]
    ]

    // when: run your script
    final Patient patient = (Patient) runner.run(new Context(testDataMap))

    assertEquals("2025-03-16", patient.getDeceasedDateTimeType().getValueAsString())

  }

}
