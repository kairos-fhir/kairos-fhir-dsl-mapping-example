package projects.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import common.Validate
import de.kairos.fhir.centraxx.metamodel.ConsentPolicy
import de.kairos.fhir.centraxx.metamodel.ConsentableAction
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Consent
import org.hl7.fhir.r4.model.DateTimeType

import static de.kairos.fhir.centraxx.metamodel.RootEntities.consent
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeFalse
import static org.junit.jupiter.api.Assumptions.assumeTrue
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii/bielefeld/consent.groovy",
    contextMapsPath = "src/test/resources/projects/mii/bielefeld/consent.json"
)
@Validate(packageDir = "src/test/resources/fhirpackages")
class ConsentExportScriptTest extends AbstractExportScriptTest<Consent> {

  private final Map<String, String> consentMiiCodeMap = [
      m_bc_patdat   : "2.16.840.1.113883.3.1937.777.24.5.3.1",
      m_bc_ins_retro: "2.16.840.1.113883.3.1937.777.24.5.3.10",
      m_bc_ins_prosp: "2.16.840.1.113883.3.1937.777.24.5.3.14",
      m_bc_recon_res: "2.16.840.1.113883.3.1937.777.24.5.3.26",
      m_bc_recon_med: "2.16.840.1.113883.3.1937.777.24.5.3.30"
  ]

  @ExportScriptTest
  void testThatConsentStateIsSet(final Context context, final Consent resource) {
    assumingThat(context.source[consent().declined()] as boolean,
        {
          assertEquals(Consent.ConsentState.REJECTED, resource.getStatus())
        }
    )

    assumingThat(!(context.source[consent().declined()] as boolean),
        {
          assertEquals(Consent.ConsentState.ACTIVE, resource.getStatus())
        }
    )
  }

  @ExportScriptTest
  void thatPatientIsSet(final Context context, final Consent resource) {
    assertEquals("Patient/" + context.source[consent().patientContainer().id()], resource.getPatient().getReference())
  }

  @ExportScriptTest
  void thatDateIsSet(final Context context, final Consent resource) {
    assertEquals(new DateTimeType(context.source[consent().creationDate()] as String).getValue(), resource.getDateTime())
  }

  @ExportScriptTest
  void testThatPolicyIsSet(final Context context, final Consent resource) {
    final boolean notRevoked = context.source[consent().revocation()] == null

    assertTrue(resource.hasPolicy())

    assumingThat(notRevoked,
        {
          assertEquals("2.16.840.1.113883.3.1937.777.24.2.1790", resource.getPolicyFirstRep().getUri())
        })

    final boolean partiallyRevoked = context.source[consent().revocation()] != null && context.source[consent().revocation().revokePartsOnly()]

    assumingThat(partiallyRevoked,
        {
          assertEquals("2.16.840.1.113883.3.1937.777.24.2.2719", resource.getPolicyFirstRep().getUri())
        })

    final boolean fullyRevoked = context.source[consent().revocation()] != null && !context.source[consent().revocation().revokePartsOnly()]
    assumingThat(fullyRevoked,
        {
          assertEquals("2.16.840.1.113883.3.1937.777.24.2.2718", resource.getPolicyFirstRep().getUri())
        })

  }

  @ExportScriptTest
  void testThatProvisionIsEmptyWhenDeclined(final Context context, final Consent resource) {
    assumingThat(context.source[consent().declined()] as boolean,
        {
          assertTrue(resource.getProvision().isEmpty())
        }
    )
  }

  @ExportScriptTest
  void testThatProvisionIsEmptyWhenElementsAreEmpty(final Context context, final Consent resource) {
    assumingThat(
        (context.source[consent().consentElements()] as List).isEmpty() && (context.source[consent().consentType().policies()] as List).isEmpty(),
        {
          assertTrue(resource.getProvision().isEmpty())
        }
    )
  }

  @ExportScriptTest
  void testThatAllProvisionsWhenFullyConsented(final Context context, final Consent resource) {

    assumeTrue(resource.hasProvision() && !(context.source[consent().consentPartsOnly()] as boolean))

    assertEquals((context.source[consent().consentType().policies()] as List).size(),
        resource.getProvision().getProvisionFirstRep().getCode().size())

    resource.getProvision().getProvisionFirstRep().getCode().forEach { final def pc ->
      assertTrue(context.source[consent().consentType().policies()].any { final def pol ->
        pc.getCodingFirstRep().getSystem() == "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3" &&
            pc.getCodingFirstRep().getCode() == consentMiiCodeMap[pol[ConsentPolicy.CONSENTABLE_ACTION][ConsentableAction.CODE] as String]
      })
    }
  }

  @ExportScriptTest
  void testThatProvisionsAllProvisionsAreExportedWhenPartiallyConsented(final Context context, final Consent resource) {

    assumeTrue(context.source[consent().consentPartsOnly()] as boolean, "Only for partially consented consents")

    assertEquals((context.source[consent().consentElements()] as List).size(),
        resource.getProvision().getProvisionFirstRep().getCode().size())

    resource.getProvision().getProvisionFirstRep().getCode().forEach { final def pc ->
      assertTrue(
          context.source[consent().consentElements()].any { final def pol ->
            pc.getCodingFirstRep().getSystem() == "urn:oid:2.16.840.1.113883.3.1937.777.24.5.3" &&
                pc.getCodingFirstRep().getCode() == consentMiiCodeMap[pol[ConsentPolicy.CONSENTABLE_ACTION][ConsentableAction.CODE] as String]
          }
      )
    }
  }

  @ExportScriptTest
  void testThatProvisionsProvisionPeriodsAreSet(final Context context, final Consent resource) {

    assumeTrue(resource.hasProvision(), "Only applicable when provision is present")

    assertEquals(new DateTimeType(context.source[consent().validFrom().date()] as String).getValue(), resource.getProvision().getPeriod().getStart())

    assumingThat(context.source[consent().validUntil()] != null && context.source[consent().validUntil().date()] != null,
        {
          assertEquals(new DateTimeType(context.source[consent().validUntil().date()] as String).getValue(),
              resource.getProvision().getPeriod().getEnd())
        }
    )

    assertTrue(resource.getProvision().hasProvision())

    assertEquals(new DateTimeType(context.source[consent().validFrom().date()] as String).getValue(),
        resource.getProvision().getProvisionFirstRep().getPeriod().getStart())

    assumingThat(context.source[consent().validUntil()] != null && context.source[consent().validUntil().date()] != null,
        {
          assertEquals(new DateTimeType(context.source[consent().validUntil().date()] as String).getValue(),
              resource.getProvision().getProvisionFirstRep().getPeriod().getEnd())
        }
    )

  }

  @ExportScriptTest
  void testThatProvisionsProvisionTypesAreSet(final Context context, final Consent resource) {

    assumeTrue(resource.hasProvision(), "Only applicable when provision is present")

    assertEquals(Consent.ConsentProvisionType.DENY, resource.getProvision().getType())
    assertEquals(Consent.ConsentProvisionType.PERMIT, resource.getProvision().getProvisionFirstRep().getType())
  }
}
