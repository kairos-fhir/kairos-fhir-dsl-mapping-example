package projects.mii_bielefeld

import common.AbstractDslBuilderTest
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.dsl.r4.context.Context
import de.kairos.fhir.dsl.r4.execution.Fhir4ScriptRunner
import groovy.json.JsonSlurper
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Identifier
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class EpisodeExportScriptTest extends AbstractDslBuilderTest {
  static Encounter result
  static Context context

  @BeforeAll
  static void setUp() {
    final FileInputStream is = new FileInputStream("src/main/groovy/projects/mii_bielefeld/encounter.groovy");
    final Fhir4ScriptRunner runner = getFhir4ScriptRunner(is, "test");

    context = new Context(createTestData());

    result = (Encounter) runner.run(context)
  }

  static Map<String, Object> createTestData() throws FileNotFoundException {
    final FileInputStream is = new FileInputStream("src/test/resources/projects/mii_bielefeld/Episode.json");
    return new JsonSlurper().parse(is) as Map<String, Object>
  }

  @Test
  void testThatIdentifiersAreSet() {
    assertTrue(result.hasIdentifier())
    assertEquals((context.source[episode().idContainer()] as List).size(), result.getIdentifier().size())

    result.getIdentifier()
        .collect { it.getType() }
        .each {
          assertNotNull(it)
          assertTrue(it.hasCoding("http://terminology.hl7.org/CodeSystem/v2-0203", "VN"))
        }


    result.getIdentifier().each { final Identifier fhirIdentifier ->
      final def idc = context.source[episode().idContainer()].find {
        it[IdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE].equals(fhirIdentifier.getSystem())
      }

      assertNotNull(idc)
      assertEquals(idc[IdContainer.PSN], fhirIdentifier.getValue())
    }
  }

  @Test
  void testThatClassIsSet() {
    assertTrue(result.hasClass_())
    assertEquals("http://terminology.hl7.org/CodeSystem/v3-ActCode", result.getClass_().getSystem())
    assertEquals(context.source[episode().stayType().code()], result.getClass_().getCode())
  }

  @Test
  void testThatSubjectIsSet() {
    assertTrue(result.hasSubject())
    assertEquals("Patient/" + context.source[episode().patientContainer().id()], result.getSubject().getReference())
  }

  @Test
  void testThatPeriodIsSet() {
    assertTrue(result.hasPeriod())
    assertEquals(new DateTimeType(context.source[episode().validFrom()] as String).getValue(),
        result.getPeriod().getStart())
    assertEquals(new DateTimeType(context.source[episode().validUntil()] as String).getValue(),
        result.getPeriod().getEnd())
  }
}
