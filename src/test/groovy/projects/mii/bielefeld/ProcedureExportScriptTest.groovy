package projects.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import common.Validate
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.CrfTemplateField.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.LaborFinding.LABOR_FINDING_LABOR_VALUES
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.LaborMapping.LABOR_FINDING
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.DATE_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medProcedure
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii/bielefeld/procedure.groovy",
    contextMapsPath = "src/test/resources/projects/mii/bielefeld/procedure.json"
)
@Validate(packageDir = "src/test/resources/fhirpackages")
class ProcedureExportScriptTest extends AbstractExportScriptTest<Procedure> {

  @ExportScriptTest
  void testThatSubjectIsSet(final Context context, final Procedure resource) {
    assertTrue(resource.hasSubject())
    assertEquals("Patient/" + context.source[medProcedure().patientContainer().id()], resource.getSubject().getReference())
  }

  @ExportScriptTest
  void testThatStatusIsSet(final Context context, final Procedure resource) {
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
        resource.getStatus().toCode()
    )
  }

  @ExportScriptTest
  void testThatCodeIsSet(final Context context, final Procedure resource) {
    assertTrue(resource.hasCode());

    final Coding opsCoding = resource.getCode().getCoding().find { it.getSystem() == "http://fhir.de/CodeSystem/bfarm/ops" }

    assertNotNull(opsCoding)

    assertEquals(context.source[medProcedure().opsEntry().code()], opsCoding.getCode())
    assertEquals(context.source[medProcedure().opsEntry().catalogue().catalogueVersion()], opsCoding.getVersion())
  }

  @ExportScriptTest
  void testThatPerformedPeriodIsExported(final Context context, final Procedure resource) {
    final def mapping = context.source[medProcedure().laborMappings()].find { final def lm ->
      lm[LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "ProcedureProfile"
    }

    assumeTrue(mapping != null)


    final def performedPeriodEnd = mapping[LABOR_FINDING][LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][LaborValue.CODE] == "Procedure.performedPeriod.end"
    }

    assumeTrue(performedPeriodEnd && performedPeriodEnd[DATE_VALUE])

    assertNotNull(performedPeriodEnd)

    assertEquals(new DateTimeType(context.source[medProcedure().procedureDate().date()] as String).getValue(),
        resource.getPerformedPeriod().getStart())

    assertEquals(new DateTimeType(performedPeriodEnd[DATE_VALUE][PrecisionDate.DATE] as String).getValue(),
        resource.getPerformedPeriod().getEnd())
  }
}
