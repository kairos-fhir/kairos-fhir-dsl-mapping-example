package projects.izi.frankfurt

import common.AbstractDslBuilderTest
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.PatientMasterDataAnonymous
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.DatePrecision
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.Patient
import org.junit.jupiter.api.Test

import static java.util.Collections.singletonMap
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
/**
 * Example test to run groovy mapping scripts with assumed test data.
 */
class PatientBirthdateTest extends AbstractDslBuilderTest {

  @Test
  void testThatBirthdateIsPopulated() {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/izi/hannover/patient.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createBirthdayWithPrecisionDay()

    // when: run your script
    final Patient patient = (Patient) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertTrue(patient.hasBirthDateElement())
    assertEquals("2024-01", patient.getBirthDateElement().getValueAsString())
  }

  @Test
  void testThatBirthDateIsIgnoredIfUnknown() {

    // given: select your groovy script to test
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/izi/hannover/patient.groovy")
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test")

    // put your test data here
    final Map<String, Object> testDataMap = createBirthdayWithPrecisionUnknown()

    // when: run your script
    final Patient patient = (Patient) runner.run(new Context(testDataMap))

    // then: test your assertions
    assertTrue(patient.hasBirthDateElement())
    final CodeType codeType = (CodeType) patient.getBirthDateElement().getExtensionByUrl(FhirUrls.Extension.FhirDefaults.DATA_ABSENT_REASON).getValue()
    assertEquals("unknown",codeType.getValue())
  }

  static Map<String, Object> createBirthdayWithPrecisionDay() {
    final Map<String, Object> dateMap = new HashMap<>();
    dateMap.put(PrecisionDate.DATE, "2024-01-22");
    dateMap.put(PrecisionDate.PRECISION, DatePrecision.DAY.name());
    return singletonMap(PatientMasterDataAnonymous.BIRTHDATE, dateMap)
  }

  static Map<String, Object> createBirthdayWithPrecisionUnknown() {
    final Map<String, Object> dateMap = new HashMap<>();
    dateMap.put(PrecisionDate.DATE, null);
    dateMap.put(PrecisionDate.PRECISION, DatePrecision.UNKNOWN.name());
    return singletonMap(PatientMasterDataAnonymous.BIRTHDATE, dateMap)
  }
}
