package projects.mii_bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.AbstractCustomCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.PatientMaster
import de.kairos.fhir.centraxx.metamodel.Unity
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import de.kairos.fhir.centraxx.metamodel.enums.CatalogCategory
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Observation
import org.junit.jupiter.api.Assumptions

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFindingLaborValue
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii_bielefeld/observation.groovy",
    contextMapsPath = "src/test/resources/projects/mii_bielefeld/observation.json"
)
class ObservationExportScriptTest extends AbstractExportScriptTest<Observation> {

  // the code of the MII common measurement profile
  final static String laborMethodName = "MII_MeasurementProfile"

  // the code of the FHIR DiagnosticReport.status laborValue
  final static String statusLvCode = "DiagnosticReport.status"

  // the issued Date laborValue
  final static String issuedLvCode = "DiagnosticReport.issued"

  // the identifier.assigner laborValue
  final static String assignerLvCode = "DiagnosticReport.identifier.assigner"


  @ExportScriptTest
  void testThatIdentifierIsSet(final Context context, final Observation observation) {
    assumeExportable(context)

    assertTrue(observation.hasIdentifier())

    assertTrue(observation.getIdentifierFirstRep().hasType())

    assertTrue(observation.getIdentifierFirstRep().getType()
        .hasCoding("http://terminology.hl7.org/CodeSystem/v2-0203",
            "OBI")
    )

    assertEquals(context.source[laborFindingLaborValue().crfTemplateField().laborValue().code()] + "_" + context.source[laborFindingLaborValue().laborFinding().laborFindingId()],
        observation.getIdentifierFirstRep().getValue()
    )

  }

  @ExportScriptTest
  void testThatStatusIsSet(final Context context, final Observation observation) {
    assumeExportable(context)

    assertEquals(Observation.ObservationStatus.FINAL, observation.getStatus())
  }

  @ExportScriptTest
  void testThatCategoryIsSet(final Context context, final Observation observation) {
    assumeExportable(context)
    assertTrue(observation.hasCategory())

    assertTrue(observation.getCategoryFirstRep()
        .hasCoding("http://loinc.org", "26436-6"))

    assertTrue(observation.getCategoryFirstRep()
        .hasCoding("http://terminology.hl7.org/CodeSystem/observation-category", "laboratory"))
  }

  @ExportScriptTest
  void testThatLoincCodeIsExported(final Context context, final Observation observation) {
    assumeExportable(context)

    final def idContainer = context.source[laborFindingLaborValue().crfTemplateField().laborValue().idContainers()].find { final def idc ->
      idc[IdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE] == "LOINC"
    }

    Assumptions.assumeTrue(idContainer != null, "No LOINC IdContainer given for LaborValue.")

    assertTrue(observation.hasCode())

    assertTrue(observation.getCode()
        .hasCoding("http://loinc.org", idContainer[IdContainer.PSN] as String)
    )
  }

  @ExportScriptTest
  void testThatLaborValueCodeIsExported(final Context context, final Observation observation) {
    assumeExportable(context)

    assertTrue(observation.hasCode())

    assertTrue(observation.getCode()
        .hasCoding(FhirUrls.System.LaborValue.BASE_URL,
            context.source[laborFindingLaborValue().crfTemplateField().laborValue().code()] as String)
    )
  }

  @ExportScriptTest
  void testThatSubjectIsSet(final Context context, final Observation observation) {
    assumeExportable(context)
    assertEquals(
        "Patient/" + context.source[laborFindingLaborValue().laborFinding().laborMappings()].find()[LaborMapping.RELATED_PATIENT][PatientMaster.ID],
        observation.getSubject().getReference()
    )
  }

  @ExportScriptTest
  void testThatEffectiveIsSet(final Context context, final Observation observation) {
    assumeExportable(context)
    Assumptions.assumeTrue(context.source[laborFindingLaborValue().laborFinding().findingDate()] &&
        context.source[laborFindingLaborValue().laborFinding().findingDate().date()])

    assertEquals(new DateTimeType(context.source[laborFindingLaborValue().laborFinding().findingDate().date()] as String).getValue(),
        observation.getEffectiveDateTimeType().getValue()
    )
  }

  @ExportScriptTest
  void testThatNonExportableValuesAreNotExported(final Context context,
                                                 final Observation observation) {
    Assumptions.assumeFalse(isExportable(context))

    assertFalse(observation.hasId())
  }

  @ExportScriptTest
  void testThatNumericalValuesAreExported(final Context context,
                                          final Observation observation) {
    Assumptions.assumeTrue(isExportable(context))
    assumeNumeric(context)

    Assumptions.assumeTrue(context.source[laborFindingLaborValue().numericValue()] != null, "LFLV has no numeric value")

    assertTrue(observation.hasValueQuantity())

    assertEquals(0, (context.source[laborFindingLaborValue().numericValue()] as BigDecimal) <=> observation.getValueQuantity().getValue())

  }

  @ExportScriptTest
  void testThatNumericalValueUnitsAreExported(final Context context,
                                              final Observation observation) {
    Assumptions.assumeTrue(isExportable(context))
    assumeNumeric(context)
    Assumptions.assumeTrue(context.source[laborFindingLaborValue().numericValue()] != null, "LFLV has no numeric value")

    assertTrue(observation.hasValueQuantity())
    final def lvUnit = context.source[laborFindingLaborValue().crfTemplateField().laborValueDecimal().unit()]

    Assumptions.assumeTrue(lvUnit != null, "LFLV has no unit")

    assertEquals(lvUnit[Unity.CODE], observation.getValueQuantity().getCode())
    assertEquals(lvUnit[Unity.CODE], observation.getValueQuantity().getUnit())

    assertEquals("http://unitsofmeasure.org", observation.getValueQuantity().getSystem())
  }

  @ExportScriptTest
  void testThatCatalogValuesAreSet(final Context context,
                                   final Observation observation) {
    Assumptions.assumeTrue(isExportable(context))
    assumeCatalog(context)

    Assumptions.assumingThat(isValueList(context),
        { ->
          assertTrue(observation.hasValueCodeableConcept())

          context.source[laborFindingLaborValue().catalogEntryValue()].each { final def catalogEntry ->
            observation.getValueCodeableConcept().hasCoding(
                FhirUrls.System.Catalogs.VALUE_LIST + "/" + catalogEntry[CatalogEntry.CATALOG][AbstractCustomCatalog.CODE],
                catalogEntry[CatalogEntry.CODE] as String
            )
          }
        })

    Assumptions.assumingThat(isCustomCatalog(context),
        { ->
          assertTrue(observation.hasValueCodeableConcept())

          context.source[laborFindingLaborValue().catalogEntryValue()].each { final def catalogEntry ->
            observation.getValueCodeableConcept().hasCoding(
                FhirUrls.System.Catalogs.CUSTOM_CATALOG + "/" + catalogEntry[CatalogEntry.CATALOG][AbstractCustomCatalog.CODE],
                catalogEntry[CatalogEntry.CODE] as String
            )
          }
        })

    Assumptions.assumingThat(isUsageEntry(context),
        { ->
          assertTrue(observation.hasValueCodeableConcept())

          context.source[laborFindingLaborValue().multiValue()].each { final def usageEntry ->
            observation.getValueCodeableConcept().hasCoding(
                "https://fhir.centraxx.de/system/catalogs/usageEntry",
                usageEntry[UsageEntry.CODE] as String
            )
          }
        })

  }

  private static boolean isValueList(final Context context) {

    final def entry = context.source[laborFindingLaborValue().catalogEntryValue()].find()

    if (!entry) {
      return false
    }

    return (entry[CatalogEntry.CATALOG][AbstractCustomCatalog.CATALOG_CATEGORY] as CatalogCategory) == CatalogCategory.VALUELIST

  }

  private static boolean isCustomCatalog(final Context context) {

    final def entry = context.source[laborFindingLaborValue().catalogEntryValue()].find()

    if (!entry) {
      return false
    }

    return (entry[CatalogEntry.CATALOG][AbstractCustomCatalog.CATALOG_CATEGORY] as CatalogCategory) == CatalogCategory.CUSTOM
  }

  private static boolean isUsageEntry(final Context context) {
    final def entry = context.source[laborFindingLaborValue().multiValue()].find()

    return entry != null
  }

  private static void assumeCatalog(final Context context) {
    final LaborValueDType dType = context.source[laborFindingLaborValue().crfTemplateField().laborValue().dType()] as LaborValueDType

    Assumptions.assumeTrue(dType in [LaborValueDType.CATALOG, LaborValueDType.OPTIONGROUP, LaborValueDType.ENUMERATION],
    "The LFLV is not a Catalog, OptionGroup or Enumeration type.")
  }

  private static void assumeNumeric(final Context context) {
    final LaborValueDType dType = context.source[laborFindingLaborValue().crfTemplateField().laborValue().dType()] as LaborValueDType

    Assumptions.assumeTrue(dType in [LaborValueDType.DECIMAL, LaborValueDType.INTEGER])
  }

  private static void assumeExportable(final Context context) {
    Assumptions.assumeTrue(isExportable(context), "The LFLV is not to be exported")
  }

  private static boolean isExportable(final Context context) {
    final def isMiiProfile = context.source[laborFindingLaborValue().laborFinding().laborMethod().code()] == laborMethodName

    final def isAdditionalDataLv = ((context.source[laborFindingLaborValue().crfTemplateField().laborValue().code()] as String)
        in [statusLvCode, issuedLvCode, assignerLvCode])
    return isMiiProfile && !isAdditionalDataLv
  }

}