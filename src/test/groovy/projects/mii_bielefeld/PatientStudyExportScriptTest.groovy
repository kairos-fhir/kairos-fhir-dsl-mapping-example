package projects.mii_bielefeld

import common.AbstractGroovyScriptTest
import common.GroovyScriptTest
import common.TestResources
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.ResearchSubject
import org.junit.jupiter.api.Assumptions

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientStudy
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii_bielefeld/researchSubject.groovy",
    contextMapsPath = "src/test/resources/projects/mii_bielefeld/PatientStudy.json"
)
class PatientStudyExportScriptTest extends AbstractGroovyScriptTest<ResearchSubject> {

  @GroovyScriptTest
  void testThatPeriodIsSet(final Context context, final ResearchSubject resource) {
    Assumptions.assumeTrue(context.source[patientStudy().memberFrom()] || context.source[patientStudy().memberFrom()])

    assertTrue(resource.hasPeriod())

    Assumptions.assumingThat(context.source[patientStudy().memberFrom()] != null,
        { ->
          assertEquals(
              new DateTimeType(context.source[patientStudy().memberFrom()] as String).getValue(),
              resource.getPeriod().getStart())

        })

    Assumptions.assumingThat(context.source[patientStudy().memberUntil()] != null,
        { ->
          assertEquals(
              new DateTimeType(context.source[patientStudy().memberUntil()] as String).getValue(),
              resource.getPeriod().getEnd())
        })
  }

  @GroovyScriptTest
  void testThatStudyIsSet(final Context context, final ResearchSubject resource) {
    assertEquals(
        "ResearchStudy/" + context.source[patientStudy().flexiStudy().id()],
        resource.getStudy().getReference()
    )
  }

  @GroovyScriptTest
  void testThatPatientIsSet(final Context context, final ResearchSubject resource) {
    assertEquals(
        "Patient/" + context.source[patientStudy().patientContainer().id()],
        resource.getIndividual().getReference()
    )
  }

  @GroovyScriptTest
  void testThatConsentIsSet(final Context context, final ResearchSubject resource) {
    assertEquals(
        "Consent/" + context.source[patientStudy().consent().id()],
        resource.getConsent().getReference()
    )
  }
}
