package customexport.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import common.utils.LflvUtils
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.DiagnosticReport
import org.hl7.fhir.r4.model.Extension

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/customexport/mii/bielefeld/microbioDiagnosticReport.groovy",
    contextMapsPath = "src/test/resources/customexport/mii/bielefeld/microbioDiagnosticReport"
)
class MikrobioDiagnosticReportExportScriptTest extends AbstractExportScriptTest<DiagnosticReport> {

  private static final String LM_CODE = "MP_Mikrobio_DiagnosticReport"

  private static final String STATUS = "DiagnosticReport.status"
  private static final String ISSUED = "DiagnosticReport.issued"
  private static final String EFFECTIVE = "DiagnosticReport.effective[x]"
  private static final String KL_BEZUGS_DATUM = "DiagnosticReport.effective[x].extension:QuelleKlinischesBezugsdatum.value[x].code"

  private static final Map PROFILE_TYPES = [
      (STATUS)         : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (ISSUED)         : LaborFindingLaborValue.DATE_VALUE,
      (EFFECTIVE)      : LaborFindingLaborValue.DATE_VALUE,
      (KL_BEZUGS_DATUM): LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
  ]

  @ExportScriptTest
  void validateResourceStructures(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)
    getValidator("fhirpackages/mii").validate(resource)
  }

  @ExportScriptTest
  void testThatIdentifierIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)
    assumeTrue(context.source[laborFinding().laborFindingId()] != null)

    assertTrue(resource.hasIdentifier())
    assertTrue(resource.getIdentifierFirstRep().hasType())
    assertTrue(resource.getIdentifierFirstRep().getType()
        .hasCoding("http://terminology.hl7.org/CodeSystem/v2-0203", "FILL"))

    assertEquals("https://ukowl.de/sid/mii-exa-mikrobio-diagnosticreport", resource.getIdentifierFirstRep().getSystem())
    assertEquals(context.source[laborFinding().laborFindingId()], resource.getIdentifierFirstRep().getValue())

    assertTrue(resource.getIdentifierFirstRep().hasAssigner())
    assertEquals(
        "https://www.medizininformatik-initiative.de/fhir/core/CodeSystem/core-location-identifier",
        resource.getIdentifierFirstRep().getAssigner().getIdentifier().getSystem()
    )
    assertEquals("ukowl.de", resource.getIdentifierFirstRep().getAssigner().getIdentifier().getValue())
  }

  @ExportScriptTest
  void testThatStatusIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    final Map<String, Object> lflvMap = getProfileLflvMap(context)
    final String expected = lflvMap.get(STATUS)?.find()?.getAt(CODE) as String

    assumingThat(expected != null,
        {
          ->
          assertEquals(expected.toLowerCase(), resource.getStatus().toCode().toLowerCase())
        })

    assumingThat(expected == null,
        {
          ->
          assertEquals(DiagnosticReport.DiagnosticReportStatus.UNKNOWN.toCode(), resource.getStatus().toCode())
        })
  }

  @ExportScriptTest
  void testThatCategoryIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    assertTrue(resource.hasCategory())
    assertTrue(resource.getCategoryFirstRep().hasCoding("http://loinc.org", "26436-6"))
    assertTrue(resource.getCategoryFirstRep().hasCoding("http://terminology.hl7.org/CodeSystem/v2-0074", "LAB"))
    assertTrue(resource.getCategoryFirstRep().hasCoding("http://snomed.info/sct", "4341000179107"))
  }

  @ExportScriptTest
  void testThatCodeIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    assertTrue(resource.hasCode())
    assertTrue(resource.getCode().hasCoding("http://loinc.org", "11502-2"))
  }

  @ExportScriptTest
  void testThatSubjectIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    final def lm = context.source[laborFinding().laborMappings()].find {
      it[LaborMapping.RELATED_PATIENT] != null
    }

    assumeTrue(lm != null, "No patient labor mapping")

    assertTrue(resource.hasSubject())
    assertEquals(
        "Patient/" + lm[LaborMapping.RELATED_PATIENT][PatientContainer.ID],
        resource.getSubject().getReference()
    )
  }

  @ExportScriptTest
  void testThatEffectiveIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    final Map<String, Object> lflvMap = getProfileLflvMap(context)
    final def effectivePD = lflvMap.get(EFFECTIVE) as Map

    assumeTrue(effectivePD != null && effectivePD[PrecisionDate.DATE] != null, "No effective date given")

    assertTrue(resource.hasEffectiveDateTimeType())
    assertEquals(
        new DateTimeType(effectivePD[PrecisionDate.DATE] as String).getValue(),
        resource.getEffectiveDateTimeType().getValue()
    )
  }

  @ExportScriptTest
  void testThatKlinischesBezugsdatumExtensionIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    final Map<String, Object> lflvMap = getProfileLflvMap(context)
    final def effectivePD = lflvMap.get(EFFECTIVE) as Map
    final String expectedCode = lflvMap.get(KL_BEZUGS_DATUM)?.find()?.getAt(CODE) as String

    assumeTrue(effectivePD != null && effectivePD[PrecisionDate.DATE] != null, "No effective date given")
    assumeTrue(expectedCode != null, "No KlinischesBezugsdatum given")

    final Extension extension = resource.getEffectiveDateTimeType().getExtensionByUrl(
        "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum"
    )

    assertNotNull(extension)
    assertEquals(expectedCode, ((Coding) extension.getValue()).getCode())
    assertEquals(
        "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/ValueSet/QuelleKlinischesBezugsdatum",
        ((Coding) extension.getValue()).getSystem()
    )
  }

  @ExportScriptTest
  void testThatIssuedIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    final Map<String, Object> lflvMap = getProfileLflvMap(context)
    final def issuedPD = lflvMap.get(ISSUED) as Map

    assumeTrue(issuedPD != null && issuedPD[PrecisionDate.DATE] != null, "No issued date given")

    assertTrue(resource.hasIssued())
    assertEquals(
        new DateTimeType(issuedPD[PrecisionDate.DATE] as String).getValue(),
        resource.getIssued()
    )
  }

  @ExportScriptTest
  void testThatObservationReferencesAreSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    final List children = context.source[laborFinding().children()] as List
    assumeTrue(children != null && !children.isEmpty(), "No child findings")

    assertEquals(children.size(), resource.getResult().size())

    children.each { final def child ->
      assertTrue(
          resource.getResult().any { final def obsRef ->
            obsRef.getReference() == "Observation/" + child[LaborFinding.ID]
          }
      )
    }
  }

  @ExportScriptTest
  void testThatIdIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    assertEquals("DiagnosticReport/" + context.source[laborFinding().id()], resource.getId())
  }

  private static Map<String, Object> getProfileLflvMap(final Context context) {
    return LflvUtils.getLflvMap(
        context.source[laborFinding().laborFindingLaborValues()] as List,
        PROFILE_TYPES
    )
  }

  private static void checkLaborMethodCode(final Context context) {
    assumeTrue(context.source[laborFinding().laborMethod().code()] == LM_CODE, "Not a Mikrobio DiagnosticReport profile")
  }
}
