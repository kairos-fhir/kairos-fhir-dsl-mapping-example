package projects.mii_bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.OrganisationUnit
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.ValueReference
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.DiagnosticReport
import org.junit.jupiter.api.Assumptions

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.CrfTemplateField.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.CATALOG_ENTRY_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii_bielefeld/diagnosticReport.groovy",
    contextMapsPath = "src/test/resources/projects/mii_bielefeld/diagnosticReport.json"
)
class DiagnosticReportExportScriptTest extends AbstractExportScriptTest<DiagnosticReport> {


  @ExportScriptTest
  void testThatIdentifierIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)
    Assumptions.assumeTrue(context.source[laborMapping().laborFinding().laborFindingId()] != null)

    assertTrue(resource.hasIdentifier())

    assertTrue(resource.getIdentifierFirstRep().hasType())

    assertTrue(resource.getIdentifierFirstRep().getType().hasCoding("http://terminology.hl7.org/CodeSystem/v2-0203", "FILL"))
    assertEquals(context.source[laborMapping().laborFinding().laborFindingId()], resource.getIdentifierFirstRep().getValue())
    assertEquals(FhirUrls.System.Finding.LABOR_FINDING_ID, resource.getIdentifierFirstRep().getSystem())

    final def assigner = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find { final def lflv ->
      lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == "DiagnosticReport.identifier.assigner"
    }

    Assumptions.assumeTrue(assigner != null, "No assigner is given in Finding")

    assertTrue(resource.getIdentifierFirstRep().hasAssigner())
    assertEquals("Organization/" + assigner[LaborFindingLaborValue.MULTI_VALUE_REFERENCES].find()?.getAt(ValueReference.ORGANIZATION_VALUE)[OrganisationUnit.ID],
        resource.getIdentifierFirstRep().getAssigner().getReference()
    )
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
    final List serviceRequestMappings = context.source[laborMapping().laborFinding().laborMappings()].findAll { final def mapping ->
      mapping[laborMapping().MAPPING_TYPE] == "SERVICEREQUEST"
    }

    serviceRequestMappings.forEach {
      assertTrue(resource.hasBasedOn())
      assertEquals("ServiceRequest/" + serviceRequestMappings[LaborMapping.RELATED_OID], resource.getBasedOnFirstRep().getReference())
    }
  }

  @ExportScriptTest
  void testThatEncounterIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    Assumptions.assumeTrue(context.source[laborMapping().episode()] != null, "No Episode on mapping")

    assertTrue(resource.hasEncounter())

    assertEquals("Encounter/" + context.source[laborMapping().episode().id()], resource.getEncounter().getReference())
  }

  @ExportScriptTest
  void testThatEffectiveIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    Assumptions.assumeTrue(context.source[laborMapping().laborFinding().findingDate()] &&
        context.source[laborMapping().laborFinding().findingDate().date()], "Finding date is not given")

    assertTrue(resource.hasEffectiveDateTimeType())

    assertEquals(new DateTimeType(context.source[laborMapping().laborFinding().findingDate().date()] as String).getValue(),
        resource.getEffectiveDateTimeType().getValue())

  }

  @ExportScriptTest
  void testThatIssuedIsSet(final Context context, final DiagnosticReport resource) {
    checkLaborMethodCode(context)

    final def issuedLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find { final def lflv ->
      lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == "DiagnosticReport.issued"
    }

    Assumptions.assumeTrue(
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

    context.source[laborMapping().laborFinding().laborFindingLaborValues()].findAll {
      !["DiagnosticReport.issued", "DiagnosticReport.identifier.assigner", "DiagnosticReport.status"]
          .contains(it[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE])
    }.each { final def lflv ->
      assertTrue(
          resource.getResult().any { final def obsRef ->
            obsRef.getReference() == "Observation/" + lflv[LaborFindingLaborValue.ID]
          }
      )
    }


    context.source[laborMapping().laborFinding().laborFindingLaborValues()].findAll {
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

    final def lflvStatus = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find { final def lflv ->
      lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == "DiagnosticReport.status"
    }

    Assumptions.assumingThat(lflvStatus && lflvStatus[CATALOG_ENTRY_VALUE],
        { ->
          assertEquals(
              lflvStatus[CATALOG_ENTRY_VALUE].find()?.getAt(CODE) as String,
              resource.getStatus().toCode()
          )
        })

    Assumptions.assumingThat(!lflvStatus || !lflvStatus[CATALOG_ENTRY_VALUE],
        { ->
          assertEquals(DiagnosticReport.DiagnosticReportStatus.UNKNOWN.toCode(), resource.getStatus().toCode())
        })
  }


  private static void checkLaborMethodCode(final Context context) {
    Assumptions.assumeTrue(context.source[laborMapping().laborFinding().laborMethod().code()] == "MII_MeasurementProfile", "Not a MII profile")
  }
}
