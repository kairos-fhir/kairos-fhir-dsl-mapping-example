package customexport.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.DocumentReference
import org.hl7.fhir.r4.model.Identifier

import static de.kairos.fhir.centraxx.metamodel.RootEntities.documentMapping
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue

@TestResources(
    groovyScriptPath = "src/main/groovy/customexport/mii/bielefeld/documentReference.groovy",
    contextMapsPath = "src/test/resources/customexport/mii/bielefeld/documentReference"
)
class DocumentReferenceExportScriptTest extends AbstractExportScriptTest<DocumentReference> {

  @ExportScriptTest
  void validateResourceStructures(final Context context, final DocumentReference resource) {
    getValidator("fhirpackages/mii").validate(resource)
  }

  @ExportScriptTest
  void testThatIdentifierIsSet(final Context context, final DocumentReference resource) {
    assumeTrue(context.source[documentMapping().finding().documentId()] != null)
    assertTrue(resource.hasIdentifier())
    assertTrue(resource.getIdentifierFirstRep().hasSystem())
    assertEquals(context.source[documentMapping().finding().documentId()], resource.getIdentifierFirstRep().getValue())
    assertEquals(FhirUrls.System.Document.DOCUMENT_ID, resource.getIdentifierFirstRep().getSystem())
  }

  @ExportScriptTest
  void testThatTypeIsSet(final Context context, final DocumentReference resource) {

    final String name = context.source[documentMapping().finding().name()] as String

    assumeTrue(name != null)

    final String[] split = name.split("_")

    assumeTrue(() -> {
      return split.length > 1 && ["KDL", "LNC", "SCT", "XDS"].contains(split[0])
    })

    final String docType = split[0]

    assertTrue(resource.hasType())

    assumeTrue(resource.getType().getCodingFirstRep().hasSystem())

    final String system = resource.getType().getCodingFirstRep().getSystem()

    switch (docType) {
      case "KDL" -> assertEquals("http://dvmd.de/fhir/CodeSystem/kdl", system)
      case "LNC" -> assertEquals("http://loinc.org", system)
      case "SCT" -> assertEquals("http://snomed.info/sct", system)
      case "XDS" -> assertEquals("http://ihe-d.de/CodeSystems/IHEXDStypeCode", system)
    }

    assertEquals(
        resource.getType().getCodingFirstRep().getCode(),
        context.source[documentMapping().category().code()] as String
    )
  }

  @ExportScriptTest
  void testThatNameIsSetAsIdentifier(final Context context, final DocumentReference resource) {

    final String name = context.source[documentMapping().finding().name()] as String

    assumeTrue(name != null)

    final String[] split = name.split("_")

    final Identifier nameIdentifier = resource.getIdentifier()
        .find { (FhirUrls.System.Document.DOCUMENT_NAME == it.getSystem()) }

    assertTrue(nameIdentifier != null)

    if (split.length > 1 && ["KDL", "LNC", "SCT", "XDS"].contains(split[0])) {
      final String docType = split[0]
      assertEquals(name.substring(docType.length() + 1), nameIdentifier.getValue())
    } else {
      assertEquals(name, nameIdentifier.getValue())
    }
  }

  @ExportScriptTest
  void testThatEncounterIsSet(final Context context, final DocumentReference resource) {

    final def episode = context.source[documentMapping().episode()]

    assumeTrue(episode != null, "No Episode on mapping")

    assertTrue(resource.hasContext())
    assertTrue(resource.getContext().hasEncounter())

    assertEquals("Encounter/" + episode[Episode.ID], resource.getContext().getEncounterFirstRep().getReference())
  }

  @ExportScriptTest
  void testThatContentIsSet(final Context context, final DocumentReference resource) {

    assertTrue(resource.hasContent())
    assertTrue(resource.getContentFirstRep().hasAttachment())

    assertEquals("Internal_URL_redacted", resource.getContentFirstRep().getAttachment().getUrl())

  }

  @ExportScriptTest
  void testThatSubjectIsSet(final Context context, final DocumentReference resource) {

    final def patient = context.source[documentMapping().relatedPatient()]

    assumeTrue(patient != null, "No Patient on mapping")

    assertTrue(resource.hasSubject())

    assertEquals("Patient/" + patient[Episode.ID], resource.getSubject().getReference())
  }
}
