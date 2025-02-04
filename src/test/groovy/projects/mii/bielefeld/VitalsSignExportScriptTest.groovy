package projects.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import common.Validate
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii/bielefeld/vitalstatus.groovy",
    contextMapsPath = "src/test/resources/projects/mii/bielefeld/vitalstatus.json"
)
@Validate(packageDir = "src/test/resources/fhirpackages")
class VitalsSignExportScriptTest extends AbstractExportScriptTest<Observation> {

  @ExportScriptTest
  void testThatSatusIsSet(final Context context, final Observation resource) {
    checkLaborMethod(context)

    assertEquals(Observation.ObservationStatus.FINAL, resource.getStatus())
  }

  @ExportScriptTest
  void testThatCategoryIsSet(final Context context, final Observation resource) {
    checkLaborMethod(context)

    assertTrue(resource.hasCategory())

    assertTrue(resource.getCategory().any { final CodeableConcept codeableConcept ->
      codeableConcept.hasCoding("http://terminology.hl7.org/CodeSystem/observation-category", "survey")
    })
  }

  @ExportScriptTest
  void testThatCodeIsSet(final Context context, final Observation resource) {
    checkLaborMethod(context)

    assertTrue(resource.hasCode())

    assertTrue(resource.getCode().any {
      it.hasCoding("http://loinc.org", "67162-8")
    })
  }

  @ExportScriptTest
  void testThatSubjectIsSet(final Context context, final Observation resource) {
    checkLaborMethod(context)

    assertTrue(resource.hasSubject())
    assertEquals("Patient/" + context.source[laborMapping().relatedPatient().id()], resource.getSubject().getReference())
  }

  @ExportScriptTest
  void testThatEffectiveDateTimeIsSet(final Context context, final Observation resource) {
    checkLaborMethod(context)

    assumeTrue(context.source[laborMapping().laborFinding().findingDate()] &&
        context.source[laborMapping().laborFinding().findingDate().date()])

    assertTrue(resource.hasEffectiveDateTimeType())

    assertEquals(
        new DateTimeType(context.source[laborMapping().laborFinding().findingDate().date()] as String).getValue(),
        resource.getEffectiveDateTimeType().getValue()
    )
  }

  @ExportScriptTest
  void testThatValueIsSet(final Context context, final Observation resource) {
    checkLaborMethod(context)

    final lflvVS = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == "Vitalstatus.valueCodeableConcept.coding.code"
    }

    assumingThat(lflvVS && lflvVS[LaborFindingLaborValue.CATALOG_ENTRY_VALUE],
        { ->
          assertTrue(resource.hasValueCodeableConcept())
          assertTrue(resource.getValueCodeableConcept().hasCoding(
              "https://www.medizininformatik-initiative.de/fhir/core/modul-person/CodeSystem/Vitalstatus",
              lflvVS[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].find().getAt(CatalogEntry.CODE) as String
          ))
        }
    )

    assumingThat(!lflvVS || !lflvVS[LaborFindingLaborValue.CATALOG_ENTRY_VALUE],
        {
          assertTrue(resource.hasValueCodeableConcept())
          assertTrue(resource.getValueCodeableConcept().hasCoding(
              "https://www.medizininformatik-initiative.de/fhir/core/modul-person/CodeSystem/Vitalstatus",
              "X"
          ))
        })
  }

  private static void checkLaborMethod(final Context context) {
    assumeTrue(context.source[laborMapping().laborFinding().laborMethod().code()] == "MiiVitalstatus", "Not a MII VitalStatus profile")
  }
}
