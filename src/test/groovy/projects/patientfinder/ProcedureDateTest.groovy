package projects.patientfinder

import common.AbstractDslBuilderTest
import de.kairos.fhir.centraxx.metamodel.MedProcedure
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import org.hl7.fhir.r4.model.Procedure
import org.junit.jupiter.api.Test

import static java.util.Collections.singletonMap
import static org.junit.jupiter.api.Assertions.assertEquals

class ProcedureDateTest extends AbstractDslBuilderTest{

  @Test
  void testThatTimezoneIsRemoved() throws IOException {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/patientfinder/procedure.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createTestDataWithInvalidDate()

    // when: run your script
    final Procedure procedure = (Procedure) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertEquals("Procedure/999", procedure.getId())
    assertEquals("1799-12-31", procedure.getPerformedDateTimeType().getValueAsString())
  }

  static Map<String, Object> createTestDataWithInvalidDate() {
    final Map<String, Object> map = new HashMap()
    map.put(MedProcedure.ID, "999")
    map.put(MedProcedure.PROCEDURE_DATE, singletonMap(PrecisionDate.DATE, "1799-12-31T23:58:45.000-00:01:15"))
    return map
  }

}
