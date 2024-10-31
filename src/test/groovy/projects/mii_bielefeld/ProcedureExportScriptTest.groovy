package projects.mii_bielefeld

import common.AbstractDslBuilderTest
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import groovy.json.JsonSlurper
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Procedure
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import javax.annotation.Nonnull

import static de.kairos.fhir.centraxx.metamodel.CrfTemplateField.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.LaborFinding.LABOR_FINDING_LABOR_VALUES
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.LaborMapping.LABOR_FINDING
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medProcedure
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class ProcedureExportScriptTest extends AbstractDslBuilderTest {
  static Procedure result
  static Context context

  @BeforeAll
  static void setUp() {
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/mii_bielefeld/procedure.groovy");
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test");

    context = new Context(createTestData())

    result = (Procedure) runner.run(context)
  }

  @Test
  void testThatSubjectIsSet() {
    assertTrue(result.hasSubject())
    assertEquals("Patient/" + context.source[medProcedure().patientContainer().id()], result.getSubject().getReference())
  }

  @Test
  void testThatStatusIsSet() {
    final def mapping = context.source[medProcedure().laborMappings()].find { final def lm ->
      lm[LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "ProcedureProfile"
    }

    assertNotNull(mapping)

    final def procedureStatus = mapping[LABOR_FINDING][LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][LaborValue.CODE] == "Procedure.status"
    }

    assertNotNull(procedureStatus)

    assertEquals(
        procedureStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].find()[CatalogEntry.CODE],
        result.getStatus().toCode()
    )
  }

  @Test
  void testThatCodeIsSet() {
    assertTrue(result.hasCode());

    final Coding opsCoding = result.getCode().getCoding().find { it.getSystem() == "http://fhir.de/CodeSystem/bfarm/ops" }

    assertNotNull(opsCoding)

    assertEquals(context.source[medProcedure().opsEntry().code()], opsCoding.getCode())
    assertEquals(context.source[medProcedure().opsEntry().catalogue().catalogueVersion()], opsCoding.getVersion())
  }

  @Test
  void testThatPerformedPeriodIsExported() {
    final def mapping = context.source[medProcedure().laborMappings()].find { final def lm ->
      lm[LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "ProcedureProfile"
    }

    assertNotNull(mapping)


    final def performedPeriodEnd = mapping[LABOR_FINDING][LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][LaborValue.CODE] == "Procedure.performedPeriod.end"
    }

    assertNotNull(performedPeriodEnd)

    assertEquals(new DateTimeType(context.source[medProcedure().procedureDate().date()] as String).getValue(),
        result.getPerformedPeriod().getStart())

    assertEquals(new DateTimeType(performedPeriodEnd[LaborFindingLaborValue.DATE_VALUE][PrecisionDate.DATE] as String).getValue(),
        result.getPerformedPeriod().getEnd())
  }

  @Nonnull
  static Map<String, Object> createTestData() throws FileNotFoundException {
    final FileInputStream is = new FileInputStream("src/test/resources/projects/mii_bielefeld/MedProcedure.json");
    return new JsonSlurper().parse(is) as Map<String, Object>
  }
}
