package projects.mii_bielefeld

import common.AbstractGroovyScriptTest
import common.GroovyScriptTest
import common.TestResources
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Observation
import org.junit.jupiter.api.Assumptions

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii_bielefeld/vitalstatus.groovy",
    contextMapsPath = "src/test/resources/projects/mii_bielefeld/VitalSignFinding.json"
)
class VitalsSignExportScriptTest extends AbstractGroovyScriptTest<Observation> {

  @GroovyScriptTest
  void testThatSatusIsSet(final Context context, final Observation resource) {
    assertEquals(Observation.ObservationStatus.FINAL, resource.getStatus())
  }

  @GroovyScriptTest
  void testThatCategoryIsSet(final Context context, final Observation resource) {
    assertTrue(resource.hasCategory())

    assertTrue(resource.getCategory().any { final CodeableConcept codeableConcept ->
      codeableConcept.hasCoding("http://terminology.hl7.org/CodeSystem/observation-category", "survey")
    })
  }

  @GroovyScriptTest
  void testThatCodeIsSet(final Context context, final Observation resource) {
    assertTrue(resource.hasCode())

    assertTrue(resource.getCode().any {
      it.hasCoding("http://loinc.org", "67162-8")
    })
  }

  @GroovyScriptTest
  void testThatSubjectIsSet(final Context context, final Observation resource) {
    assertTrue(resource.hasSubject())
    assertEquals("Patient/" + context.source[laborMapping().relatedPatient().id()], resource.getSubject().getReference())
  }

  @GroovyScriptTest
  void testThatEffectiveDateTimeIsSet(final Context context, final Observation resource) {
    Assumptions.assumeTrue(context.source[laborMapping().laborFinding().findingDate()] &&
        context.source[laborMapping().laborFinding().findingDate().date()])

    assertTrue(resource.hasEffectiveDateTimeType())

    assertEquals(
        new DateTimeType(context.source[laborMapping().laborFinding().findingDate().date()] as String).getValue(),
        resource.getEffectiveDateTimeType().getValue()
    )
  }

  @GroovyScriptTest
  void testThatValueIsSet(final Context context, final Observation resource) {
    final lflvVS = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == "Vitalstatus.valueCodeableConcept.coding.code"
    }

    Assumptions.assumingThat(lflvVS && lflvVS[LaborFindingLaborValue.CATALOG_ENTRY_VALUE],
        { ->
          assertTrue(resource.hasValueCodeableConcept())
          assertTrue(resource.getValueCodeableConcept().hasCoding(
              "https://www.medizininformatik-initiative.de/fhir/core/modul-person/CodeSystem/Vitalstatus",
              lflvVS[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].find().getAt(CatalogEntry.CODE) as String
          ))
        }
    )

    Assumptions.assumingThat(!lflvVS || !lflvVS[LaborFindingLaborValue.CATALOG_ENTRY_VALUE],
        {
          assertTrue(resource.hasValueCodeableConcept())
          assertTrue(resource.getValueCodeableConcept().hasCoding(
              "https://www.medizininformatik-initiative.de/fhir/core/modul-person/CodeSystem/Vitalstatus",
              "X"
          ))
        })
  }

}
