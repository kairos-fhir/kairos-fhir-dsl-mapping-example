package projects.mii.greifswald

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.enums.SampleCategory
import de.kairos.fhir.dsl.r4.context.BuiltinConcept
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Range
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Specimen

import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeFalse
import static org.junit.jupiter.api.Assumptions.assumeTrue
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii/greifswald/specimen.groovy",
    contextMapsPath = "src/test/resources/projects/mii/greifswald/specimen.json"
)
//@Validate(packageDir = "src/test/resources/fhirpackages")
class SpecimenExportScriptTest extends AbstractExportScriptTest<Specimen> {

  @ExportScriptTest
  void testThatDiagnosisIsSet(final Context context, final Specimen resource) {

    assumeTrue(context.source[sample().diagnosis()] != null, "No Diagnosis present on sample.")

    final def url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Diagnose"
    assertTrue(resource.hasExtension(url))

    assertEquals("Condition/" + context.source[sample().diagnosis().id()],
        ((Reference) resource.getExtensionByUrl(url).getValue()).getReference())
  }

  @ExportScriptTest
  void testThatOrganizationUnitIsSet(final Context context, final Specimen resource) {

    assumeTrue(context.source[sample().organisationUnit()] != null, "No orgUnit present on sample")


    final def url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/VerwaltendeOrganisation"
    assertTrue(resource.hasExtension(url))

    assertEquals("Organization/" + context.source[sample().organisationUnit().id()],
        ((Reference) resource.getExtensionByUrl(url).getValue()).getReference())
  }

  @ExportScriptTest
  void testThatIdContainersAreExported(final Context context, final Specimen resource) {

    context.source[sample().idContainer()].each { final def idc ->

      final String idctCode = idc[IdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE]
      final String idcPsn = idc[IdContainer.PSN]

      assertTrue(
          resource.getIdentifier().any { final def fhirIdentifier ->
            fhirIdentifier.hasType() &&
                fhirIdentifier.getType().hasCoding(FhirUrls.System.IdContainerType.BASE_URL, idctCode) &&
                fhirIdentifier.value == idcPsn
          }
      )
    }
  }

  @ExportScriptTest
  void testThatSampleTypeIsSet(final Context context, final Specimen resource) {

    final List<Coding> translationResult = context.translateBuiltinConceptToAll(BuiltinConcept.MII_SPREC_SNOMED_SAMPLETYPE,
        context.source[sample().sampleType().sprecCode()])

    assumeFalse(translationResult.isEmpty(), "No translation in concept for sampleType sprec code: "
        + context.source[sample().sampleType().sprecCode()])

    assertTrue(resource.hasType())

    translationResult.each {
      assertTrue(resource.getType().hasCoding("http://snomed.info/sct", it.code))
    }
  }

  @ExportScriptTest
  void testThatPatientIsSet(final Context context, final Specimen resource) {
    assertTrue(resource.hasSubject())

    assertEquals("Patient/" + context.source[sample().patientContainer().id()],
        resource.getSubject().getReference())
  }

  @ExportScriptTest
  void testThatParentIsSetWhenSampleParentCategoryIsNotAliquotGroup(final Context context, final Specimen resource) {

    assumeTrue(context.source[sample().parent()] != null, "Sample has no parent")
    assumeTrue((context.source[sample().parent().sampleCategory()] as SampleCategory) != SampleCategory.ALIQUOTGROUP,
        "Sample parent is an AliquotGroup")

    assertEquals("Specimen/" + context.source[sample().parent().id()], resource.getParentFirstRep().getReference())
  }

  @ExportScriptTest
  void testThatParentIsSetWhenSampleParentCategoryIsAliquotGroup(final Context context, final Specimen resource) {

    assumeTrue(context.source[sample().parent()] != null, "Sample has no parent")
    assumeTrue((context.source[sample().parent().sampleCategory()] as SampleCategory) == SampleCategory.ALIQUOTGROUP,
        "Sample parent is not an AliquotGroup")

    assertEquals("Specimen/" + context.source[sample().parent().parent().id()], resource.getParentFirstRep().getReference())
  }

  @ExportScriptTest
  void testThatCollectionDateTimeIsSet(final Context context, final Specimen resource) {

    assertTrue(resource.hasCollection())

    assertEquals(new DateTimeType(context.source[sample().samplingDate().date()] as String).getValue(),
        resource.getCollection().getCollectedDateTimeType().getValue())

  }

  @ExportScriptTest
  void testThatCollectionBodySiteIsSet(final Context context, final Specimen resource) {

    assumeTrue(context.source[sample().orgSample()] != null, "Sample has no Organ set.")
    assertTrue(resource.hasCollection())

    assertEquals(new DateTimeType(context.source[sample().samplingDate().date()] as String).getValue(),
        resource.getCollection().getCollectedDateTimeType().getValue())

    assertTrue(
        resource.getCollection()
            .getBodySite()
            .hasCoding(FhirUrls.System.Organ.BASE_URL, context.source[sample().orgSample().code()] as String)
    )
  }

  @ExportScriptTest
  void ProcessingTemperatureIsSet(final Context context, final Specimen resource) {
    assumeTrue(context.source[sample().sampleLocation()] != null && !context.source[sample().sampleLocation().locationSchema().workspace()],
    "location is not set or is a Workspace")

    final def url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Temperaturbedingungen"

    assertTrue(resource.hasProcessing())
    assertTrue(resource.getProcessingFirstRep().hasExtension(url))

    assertEquals(0, (context.source[sample().sampleLocation().temperature()] as BigDecimal).compareTo(
        ((Range) resource.getProcessingFirstRep().getExtensionByUrl(url).getValue()).getLow().getValue()))

  }

  @ExportScriptTest
  void ProcessingDateTimeIsSet(final Context context, final Specimen resource) {

    assumeTrue(context.source[sample().sampleLocation()] != null, "SampleLocation is not set.")
    assumeFalse(context.source[sample().sampleLocation().locationSchema().workspace()] as boolean, "SampleLocation is Workspace.")

    assumeTrue(context.source[sample().sampleLocation()] != null, "SampleLocation is not set.")

    assumeTrue(context.source[sample().repositionDate()] != null && context.source[sample().repositionDate().date()] != null)

    assertEquals(
        new DateTimeType(context.source[sample().repositionDate().date()] as String).getValue(),
        resource.getProcessingFirstRep().getTimePeriod().getStart()
    )
  }

  @ExportScriptTest
  void testThatContainerTypeSnomedIsSet(final Context context, final Specimen resource) {
    assumeTrue(context.source[sample().receptable()] != null, "SampleReceptacle is not set.")

    final List<Coding> translationResult = context.translateBuiltinConceptToAll(BuiltinConcept.MII_SPREC_SNOMED_LONGTERMSTORAGE,
        context.source[sample().receptable().sprecCode()])

    assumeFalse(translationResult.isEmpty(), "No translation found for SampleReceptacle SPREC code "
        + context.source[sample().receptable().sprecCode()])

    assertTrue(resource.hasType())

    translationResult.each {
      assertTrue(resource.getType().hasCoding("http://snomed.info/sct", it.getCode()))
    }
  }

  @ExportScriptTest
  void testThatContainerSizeIsSet(final Context context, final Specimen resource) {
    assumeTrue(context.source[sample().receptable()] != null, "SampleReceptacle is not set.")

    assumeTrue(context.source[sample().receptable().size()] != null, "SampleReceptacle size is not set.")

    assertTrue(resource.getContainerFirstRep().hasCapacity())

    assertEquals(0, (context.source[sample().receptable().size()] as BigDecimal) <=> resource.getContainerFirstRep().getCapacity().getValue())

    final Coding translationResult = context.translateBuiltinConcept(
        BuiltinConcept.CXX_UCUM,
        context.source[sample().receptable().volume()]
    )

    assumingThat(!translationResult.isEmpty(), {
      assertEquals("http://unitsofmeasure.org", resource.getContainerFirstRep().getCapacity().getSystem())
      assertEquals(translationResult.getCode(), resource.getContainerFirstRep().getCapacity().getCode())
    })

    assumingThat(translationResult == null, {
      assertEquals(FhirUrls.System.AmountUnit.BASE_URL, resource.getContainerFirstRep().getCapacity().getSystem())
      assertEquals(context.source[sample().receptable().volume()], resource.getContainerFirstRep().getCapacity().getCode())
    })
  }

  @ExportScriptTest
  void testThatSpecimenQuantityIsSet(final Context context, final Specimen resource) {
    assumeTrue(context.source[sample().restAmount()] != null, "Sample restAmount is not set.")

    assertTrue(resource.getContainerFirstRep().hasSpecimenQuantity())

    assertEquals(0, (context.source[sample().restAmount().amount()] as BigDecimal) <=>
        resource.getContainerFirstRep().getSpecimenQuantity().getValue())

    final Coding translationResult = context.translateBuiltinConcept(
        BuiltinConcept.CXX_UCUM,
        context.source[sample().restAmount().unit()]
    )

    assumingThat(!translationResult.isEmpty(), {
      assertEquals("http://unitsofmeasure.org", resource.getContainerFirstRep().getSpecimenQuantity().getSystem())
      assertEquals(translationResult.getCode(), resource.getContainerFirstRep().getSpecimenQuantity().getCode())
    })

    assumingThat(translationResult == null, {
      assertEquals(FhirUrls.System.AmountUnit.BASE_URL, resource.getContainerFirstRep().getSpecimenQuantity().getSystem())
      assertEquals(context.source[sample().receptable().volume()], resource.getContainerFirstRep().getSpecimenQuantity().getCode())
    })
  }

  @ExportScriptTest
  void testThatPrimarySampleContainerAdditiveIsSet(final Context context, final Specimen resource) {
    assumeTrue(context.source[sample().sprecPrimarySampleContainer()] != null, "SPREC primary sample containe is not set.")

    final List<Coding> translationResult = context.translateBuiltinConceptToAll(
        BuiltinConcept.MII_SPREC_SNOMED_PRIMARYCONTAINER,
        context.source[sample().sprecPrimarySampleContainer().sprecCode()]
    )

    assumeFalse(translationResult.isEmpty(), "No translation found for SPREC code "
        + context.source[sample().sprecPrimarySampleContainer().sprecCode()])
    assertTrue(resource.getContainerFirstRep().hasAdditiveCodeableConcept())
    translationResult.each {
      assertTrue(resource.getContainerFirstRep().getAdditiveCodeableConcept().hasCoding("http://snomed.info/sct", it.getCode()))

    }
  }

  @ExportScriptTest
  void testThatStockTypeAdditiveIsSet(final Context context, final Specimen resource) {
    assumeTrue(context.source[sample().stockType()] != null, "SPREC stockType (fixation type) is not set.")

    final List<Coding> translationResult = context.translateBuiltinConceptToAll(
        BuiltinConcept.MII_SPREC_SNOMED_FIXATIONTYPE,
        context.source[sample().stockType().sprecCode()]
    )

    assumeTrue(!translationResult.isEmpty(), "No translation found for SPREC code " + context.source[sample().stockType().sprecCode()])

    assertTrue(resource.getContainerFirstRep().hasAdditiveCodeableConcept())
    translationResult.each {
      assertTrue(resource.getContainerFirstRep().getAdditiveCodeableConcept().hasCoding("http://snomed.info/sct", it.getCode()))
    }
  }
}