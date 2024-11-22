package projects.patientfinder

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.DiagnosticReport
import org.junit.jupiter.api.Assumptions

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/patientfinder/diagnosticReportHull.groovy",
    contextMapsPath = "src/test/resources/projects/patientfinder/diagnosticReportHull.json"
)
class DiagnosticReportHullScriptTest extends AbstractExportScriptTest<DiagnosticReport>{

  @ExportScriptTest
  void testThatIdentifierIsExported(final Context context, final DiagnosticReport resource) {

    Assumptions.assumeTrue(context.source[laborMapping().laborFinding().laborFindingId()] != null)

    assertTrue(resource.hasIdentifier())

    assertEquals(1, resource.getIdentifier().size())

    assertEquals(context.source[laborMapping().laborFinding().laborFindingId()], resource.getIdentifierFirstRep().getValue())
  }

  @ExportScriptTest
  void testThatCategoryIsExported(final Context context, final DiagnosticReport resource) {

    assertTrue(resource.hasCategory())
    assertEquals(1, resource.getCategory().size())

    assertTrue(resource.getCategoryFirstRep().hasCoding(null, context.source[laborMapping().laborFinding().laborMethod().code()] as String))

    final def multilingualEntry = context.source[laborMapping().laborFinding().laborMethod().nameMultilingualEntries()].find {final def me ->
      me[MultilingualEntry.LANG] == "en"
    }

    Assumptions.assumeTrue(multilingualEntry != null)

    assertEquals(multilingualEntry[MultilingualEntry.VALUE], resource.getCategoryFirstRep().getCodingFirstRep().getDisplay())
  }
}
