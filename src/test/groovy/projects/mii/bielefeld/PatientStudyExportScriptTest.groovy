package projects.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import common.Validate
import de.kairos.fhir.centraxx.metamodel.StudyMember
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.ResearchSubject

import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ID
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientStudy
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii/bielefeld/researchSubject.groovy",
    contextMapsPath = "src/test/resources/projects/mii/bielefeld/researchSubject.json"
)
@Validate(packageDir = "src/test/resources/fhirpackages")
class PatientStudyExportScriptTest extends AbstractExportScriptTest<ResearchSubject> {

  @ExportScriptTest
  void testThatIdentifierIsSet(final Context context, final ResearchSubject resource) {

    assumeTrue(context.source[patientStudy().patientContainer().studyMembers()].find() != null, "no StudyMembers")

    final def studyMember = context.source[patientStudy().patientContainer().studyMembers()].find { final def sm ->
      sm[StudyMember.STUDY][ID] == context.source[patientStudy().flexiStudy().id()]
    }

    assumeTrue(studyMember != null, "No StudyMember for PatientStudy FlexiStudy found.")

    assertTrue(resource.hasIdentifier())
    assertTrue(resource.getIdentifierFirstRep().hasType())
    assertTrue(resource.getIdentifierFirstRep().getType().hasCoding(
        "http://terminology.hl7.org/CodeSystem/v2-0203",
        "ANON"
    ))
    assertEquals(studyMember[StudyMember.STUDY_MEMBER_ID] as String, resource.getIdentifierFirstRep().getValue())
  }

  @ExportScriptTest
  void testThatPeriodIsSet(final Context context, final ResearchSubject resource) {
    assumeTrue(context.source[patientStudy().patientContainer().studyMembers()].find() != null, "no StudyMembers")

    final def studyMember = context.source[patientStudy().patientContainer().studyMembers()].find { final def sm ->
      sm[StudyMember.STUDY][ID] == context.source[patientStudy().flexiStudy().id()]
    }

    assumeTrue(studyMember != null, "No StudyMember for PatientStudy FlexiStudy found.")
    assumeTrue(context.source[patientStudy().memberFrom()] || context.source[patientStudy().memberFrom()])

    assertTrue(resource.hasPeriod())

    assumingThat(context.source[patientStudy().memberFrom()] != null,
        { ->
          assertEquals(
              new DateTimeType(context.source[patientStudy().memberFrom()] as String).getValue(),
              resource.getPeriod().getStart())
        })

    assumingThat(context.source[patientStudy().memberUntil()] != null,
        { ->
          assertEquals(
              new DateTimeType(context.source[patientStudy().memberUntil()] as String).getValue(),
              resource.getPeriod().getEnd())
        })
  }

  @ExportScriptTest
  void testThatStudyIsSet(final Context context, final ResearchSubject resource) {
    assumeTrue(context.source[patientStudy().patientContainer().studyMembers()].find() != null, "no StudyMembers")

    final def studyMember = context.source[patientStudy().patientContainer().studyMembers()].find { final def sm ->
      sm[StudyMember.STUDY][ID] == context.source[patientStudy().flexiStudy().id()]
    }

    assumeTrue(studyMember != null, "No StudyMember for PatientStudy FlexiStudy found.")

    assertEquals(
        "ResearchStudy/" + context.source[patientStudy().flexiStudy().id()],
        resource.getStudy().getReference()
    )
  }

  @ExportScriptTest
  void testThatPatientIsSet(final Context context, final ResearchSubject resource) {
    assumeTrue(context.source[patientStudy().patientContainer().studyMembers()].find() != null, "no StudyMembers")
    final def studyMember = context.source[patientStudy().patientContainer().studyMembers()].find { final def sm ->
      sm[StudyMember.STUDY][ID] == context.source[patientStudy().flexiStudy().id()]
    }
    assumeTrue(studyMember != null, "No StudyMember for PatientStudy FlexiStudy found.")

    assertEquals(
        "Patient/" + context.source[patientStudy().patientContainer().id()],
        resource.getIndividual().getReference()
    )
  }

  @ExportScriptTest
  void testThatConsentIsSet(final Context context, final ResearchSubject resource) {
    assumeTrue(context.source[patientStudy().patientContainer().studyMembers()].find() != null, "no StudyMembers")
    final def studyMember = context.source[patientStudy().patientContainer().studyMembers()].find { final def sm ->
      sm[StudyMember.STUDY][ID] == context.source[patientStudy().flexiStudy().id()]
    }
    assumeTrue(studyMember != null, "No StudyMember for PatientStudy FlexiStudy found.")

    assertEquals(
        "Consent/" + context.source[patientStudy().consent().id()],
        resource.getConsent().getReference()
    )
  }

}
