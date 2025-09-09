package customexport.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.CrfTemplateField.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.CATALOG_ENTRY_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/customexport/mii/bielefeld/diagnosticReport.groovy",
    contextMapsPath = "src/test/resources/customexport/mii/bielefeld/diagnosticReport"
)
class DiagnosticReportExportScriptTest extends AbstractExportScriptTest<DiagnosticReport> {

  @ExportScriptTest
  void validateResourceStructures(final Context context, final DiagnosticReport resource){
    getValidator("fhirpackages/mii").validate(resource)
  }

  @ExportScriptTest
  void testThatIdentifierIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)
    assumeTrue(context.source[laborFinding().laborFindingId()] != null)

    assertTrue(resource.hasIdentifier())

    assertTrue(resource.getIdentifierFirstRep().hasType())

    assertTrue(resource.getIdentifierFirstRep().getType().hasCoding("http://terminology.hl7.org/CodeSystem/v2-0203", "FILL"))
    assertEquals(context.source[laborFinding().shortName()], resource.getIdentifierFirstRep().getValue())
    assertEquals(FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME, resource.getIdentifierFirstRep().getSystem())


    assertTrue(resource.getIdentifierFirstRep().hasAssigner())
    assertEquals("ukowl.de", resource.getIdentifierFirstRep().getAssigner().getIdentifier().getValue())
  }

  @ExportScriptTest
  void testThatCategoryIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)
    assertTrue(resource.hasCategory())
    assertTrue(resource.getCategoryFirstRep().hasCoding("http://loinc.org", "26436-6"))
    assertTrue(resource.getCategoryFirstRep().hasCoding("http://terminology.hl7.org/CodeSystem/v2-0074", "LAB"))
  }

  @ExportScriptTest
  void testThatBasedOnIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    final List serviceRequestMappings = context.source[laborFinding().laborMappings()].findAll { final def mapping ->
      mapping[LaborMapping.MAPPING_TYPE] as LaborMappingType == LaborMappingType.SERVICEREQUEST
    }

    serviceRequestMappings.forEach {
      assertTrue(resource.hasBasedOn())
      assertEquals("ServiceRequest/" + serviceRequestMappings[LaborMapping.RELATED_OID], resource.getBasedOnFirstRep().getReference())
    }
  }

  @ExportScriptTest
  void testThatEncounterIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    final def lmEpisode = context.source[laborFinding().laborMappings()]
        .find { final def lm -> lm[LaborMapping.EPISODE] != null }

    assumeTrue(lmEpisode != null, "No Episode on mapping")

    assertTrue(resource.hasEncounter())

    assertEquals("Encounter/" + lmEpisode[LaborMapping.EPISODE][Episode.ID], resource.getEncounter().getReference())
  }

  @ExportScriptTest
  void testThatEffectiveIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    assumeTrue(context.source[laborFinding().findingDate()] &&
        context.source[laborFinding().findingDate().date()], "Finding date is not given")

    assertTrue(resource.hasEffectiveDateTimeType())

    assertEquals(new DateTimeType(context.source[laborFinding().findingDate().date()] as String).getValue(),
        resource.getEffectiveDateTimeType().getValue())

  }

  @ExportScriptTest
  void testThatIssuedIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    final def issuedLv = context.source[laborFinding().laborFindingLaborValues()].find { final def lflv ->
      lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == "DiagnosticReport.issued"
    }

    assumeTrue(
        issuedLv &&
            issuedLv[LaborFindingLaborValue.DATE_VALUE] &&
            issuedLv[LaborFindingLaborValue.DATE_VALUE][PrecisionDate.DATE],
        "No issued date given in finding")


    assertTrue(resource.hasIssued())

    assertEquals(new DateTimeType(issuedLv[LaborFindingLaborValue.DATE_VALUE][PrecisionDate.DATE] as String).getValue(),
        resource.getIssued())

  }

  @ExportScriptTest
  void testThatObservationReferencesAreSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    context.source[laborFinding().laborFindingLaborValues()].findAll {
      !["DiagnosticReport.issued", "DiagnosticReport.identifier.assigner", "DiagnosticReport.status"]
          .contains(it[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE])
    }.each { final def lflv ->
      assertTrue(
          resource.getResult().any { final def obsRef ->
            obsRef.getReference() == "Observation/" + lflv[LaborFindingLaborValue.ID]
          }
      )
    }


    context.source[laborFinding().laborFindingLaborValues()].findAll {
      ["DiagnosticReport.issued", "DiagnosticReport.identifier.assigner", "DiagnosticReport.status"]
          .contains(it[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] as String)
    }.each { final def lflv ->
      assertFalse(
          resource.getResult().any { final def obsRef ->
            obsRef.getReference() == "Observation/" + lflv[LaborFindingLaborValue.ID]
          }
      )
    }
  }

  @ExportScriptTest
  void testThatStatusIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    final def lflvStatus = context.source[laborFinding().laborFindingLaborValues()].find { final def lflv ->
      lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == "DiagnosticReport.status"
    }

    assumingThat(lflvStatus && lflvStatus[CATALOG_ENTRY_VALUE],
        { ->
          assertEquals(
              (lflvStatus[CATALOG_ENTRY_VALUE].find()?.getAt(CODE) as String).toLowerCase(),
              resource.getStatus().toCode().toLowerCase()
          )
        })

    assumingThat(!lflvStatus || !lflvStatus[CATALOG_ENTRY_VALUE],
        { ->
          assertEquals(DiagnosticReport.DiagnosticReportStatus.UNKNOWN.toCode(), resource.getStatus().toCode())
        })
  }

  private static void checkLaborMethodCode(final Context context) {
    assumeTrue(context.source[laborFinding().laborMethod().code()] == "MP_DiagnosticReportLab", "Not a MII profile")
  }
}
