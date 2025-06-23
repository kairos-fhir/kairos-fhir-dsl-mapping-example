package projects.mii.bielefeld


import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import common.Validate
import de.kairos.fhir.centraxx.metamodel.Country
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.InsuranceCompany
import de.kairos.fhir.centraxx.metamodel.PatientAddress
import de.kairos.fhir.centraxx.metamodel.PatientInsurance
import de.kairos.fhir.centraxx.metamodel.enums.CoverageType
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient

import javax.annotation.Nonnull

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous
import static org.apache.commons.lang3.StringUtils.isBlank
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/projects/mii/bielefeld/patient.groovy",
    contextMapsPath = "src/test/resources/projects/mii/bielefeld/patient.json"
)
@Validate(packageDir = "src/test/resources/fhirpackages")
class PatientExportScriptTest extends AbstractExportScriptTest<Patient> {

  @ExportScriptTest
  void testThatGKVIdentifierIsSet(final Context context, final Patient resource) {
    assumeTrue(!isPseudo(context), "Patient is pseudonymized")

    final def gkvInsurance = context.source[patient().patientContainer().patientInsurances()]?.find {
      CoverageType.T == it[PatientInsurance.COVERAGE_TYPE] as CoverageType
    }

    assumeTrue(gkvInsurance != null)

    final Identifier identifier = resource.getIdentifier().find {
      it.getType().hasCoding("http://fhir.de/CodeSystem/identifier-type-de-basis", "KVZ10")
    }

    assertNotNull(identifier)

    assertEquals("http://fhir.de/sid/gkv/kvid-10", identifier.getSystem())
    assertEquals(gkvInsurance[PatientInsurance.POLICE_NUMBER], identifier.getValue())

    assertTrue(identifier.hasAssigner() && identifier.getAssigner().hasIdentifier())

    assertEquals(gkvInsurance[PatientInsurance.INSURANCE_COMPANY][InsuranceCompany.COMPANY_ID],
        identifier.getAssigner().getIdentifier().getValue())

  }

  @ExportScriptTest
  void testThatPKVIdentifierIsSet(final Context context, final Patient resource) {
    assumeTrue(!isPseudo(context), "Patient is pseudonymized")

    final def gkvInsurance = context.source[patient().patientContainer().patientInsurances()]?.find {
      CoverageType.T == it[PatientInsurance.COVERAGE_TYPE] as CoverageType
    }

    assumeTrue(gkvInsurance == null, "If GKV is present it will be exported and PKV will be ignored");

    final def pkvInsurance = context.source[patient().patientContainer().patientInsurances()]?.find {
      CoverageType.C == it[PatientInsurance.COVERAGE_TYPE] as CoverageType || CoverageType.P == it[PatientInsurance.COVERAGE_TYPE] as CoverageType
    }

    assumeTrue(pkvInsurance != null)

    final Identifier identifier = resource.getIdentifier().find {
      it.getType().hasCoding("http://fhir.de/CodeSystem/identifier-type-de-basis", "KVZ10")
    }

    assertNotNull(identifier)

    assertEquals(pkvInsurance[PatientInsurance.POLICE_NUMBER], identifier.getValue())

    assertTrue(identifier.hasAssigner() && identifier.getAssigner().hasIdentifier())

    assertEquals(pkvInsurance[PatientInsurance.INSURANCE_COMPANY][InsuranceCompany.COMPANY_ID],
        identifier.getAssigner().getIdentifier().getValue())

  }

  @ExportScriptTest
  void testThatPseudoPatientsSetsIdentifierPattern(final Context context, final Patient resource) {
    assumeTrue(isPseudo(context), "Patient is not pseudonymized")

    assertTrue(resource.getIdentifierFirstRep()
        .getType()
        .hasCoding("http://terminology.hl7.org/CodeSystem/v3-ObservationValue", "PSEUDED"))
  }


  @ExportScriptTest
  void testThatIdContainerIdentifiersAreExported(final Context context, final Patient resource) {
    assumeTrue(!isPseudo(context), "Patient is pseudonymized")

    context.source[patientMasterDataAnonymous().patientContainer().idContainer()].each { final def idContainer ->
      final def identifier = resource.getIdentifier().find {
        it.hasSystem() && "https://fhir.centraxx.de/system/idContainer/psn" == it.getSystem() && (idContainer[IdContainer.PSN] as String) == it.getValue()
      }


      assertNotNull(identifier)
      assertTrue(identifier.hasType())
      assertTrue(identifier.getType().hasCoding("http://fhir.de/CodeSystem/identifier-type-de-basis", "MR"))

      assertEquals(idContainer[IdContainer.PSN], identifier.getValue())
    }
  }

  @ExportScriptTest
  void testThatPatientAddressesAreSet(final Context context, final Patient resource) {
    assumeTrue(!isPseudo(context), "Patient is pseudonymized")

    context.source[patient().addresses()].each { final def patAd ->
      assumingThat(patAd[PatientAddress.STREET] != null, {
        final Address address = resource.getAddress().find {
          it.hasLine() &&
              (!patAd[PatientAddress.STREET] || it.getLine()?.get(0)?.getValue()?.contains(patAd[PatientAddress.STREET] as String)) &&
              (!patAd[PatientAddress.STREETNO] || it.getLine()?.get(0)?.getValue()?.contains(patAd[PatientAddress.STREETNO] as String)) &&
              (!patAd[PatientAddress.COUNTRY] || patAd[PatientAddress.COUNTRY][Country.ISO2_CODE] == it.getCountry()) &&
              (!patAd[PatientAddress.ZIPCODE] || patAd[PatientAddress.ZIPCODE] == it.getPostalCode()) &&
              (!patAd[PatientAddress.CITY] || patAd[PatientAddress.CITY] == it.getCity())
        }

        assertNotNull(address)
        assertEquals(Address.AddressType.BOTH, address.getType())

      })

      assumingThat(patAd[PatientAddress.PO_BOX] != null, {
        final Address address = resource.getAddress().find {
          it.hasLine() &&
              (!patAd[PatientAddress.PO_BOX] in it.getLine().get(0)) &&
              (!patAd[PatientAddress.COUNTRY] || patAd[PatientAddress.COUNTRY][Country.ISO2_CODE] == it.getCountry()) &&
              (!patAd[PatientAddress.ZIPCODE] || patAd[PatientAddress.ZIPCODE] == it.getPostalCode()) &&
              (!patAd[PatientAddress.CITY] || patAd[PatientAddress.CITY] == it.getCity())
        }
        assertNotNull(address)
        assertEquals(Address.AddressType.POSTAL, address.getType())
      })
    }
  }

  @ExportScriptTest
  void testThatPatientAddressSetsOnlyZipCodeAndCountryForPseudoPatient(final Context context, final Patient resource) {
    assumeTrue(isPseudo(context), "Patient is not pseudonymized")

    context.source[patient().addresses()].each { final def patAd ->
      final Address address = resource.getAddress().find {
            (!patAd[PatientAddress.COUNTRY] || patAd[PatientAddress.COUNTRY][Country.ISO2_CODE] == it.getCountry()) &&
            (!patAd[PatientAddress.ZIPCODE] || patAd[PatientAddress.ZIPCODE] == it.getPostalCode())
      }

      assertNotNull(address)
      assertEquals(Address.AddressType.BOTH, address.getType())
      assertFalse(address.hasLine())
      assertFalse(address.hasCity())
    }
  }

  @ExportScriptTest
  void testThatBirthDateIsSet(final Context context, final Patient resource) {
    assumeTrue(!isPseudo(context), "Patient is pseudonymized")

    assumeTrue(context.source[patient().birthdate()] && context.source[patient().birthdate().date()])
    assertNotNull(resource.getBirthDate())
    assertEquals(new DateTimeType(context.source[patient().birthdate().date()] as String).getValue(),
        resource.getBirthDate())
  }

  /**
   * Date must be rounded to first day of month, first month of quarter. Year must be the same.
   * only checking year. The Month should be validated by the constraint on the element in the structure defintion
   */
  @ExportScriptTest
  void testThatBirthDateIsSetForPseudo(final Context context, final Patient resource) {
    assumeTrue(isPseudo(context), "Patient is not pseudonymized")

    assumeTrue(context.source[patient().birthdate()] && context.source[patient().birthdate().date()])
    assertNotNull(resource.getBirthDate())

    // year must be equal
    assertEquals(new DateTimeType(context.source[patient().birthdate().date()] as String).getYear(),
        resource.getBirthDateElement().getYear())
  }

  @ExportScriptTest
  void testThatDeceasedDateIsSet(final Context context, final Patient resource) {
    assumeTrue(!isPseudo(context), "Patient is pseudonymized")

    assumeTrue(context.source[patient().dateOfDeath()] && context.source[patient().dateOfDeath().date()])
    assertNotNull(resource.getDeceasedDateTimeType())
    assertEquals(new DateTimeType(context.source[patient().dateOfDeath().date()] as String).getValue(),
        resource.getDeceasedDateTimeType().getValue())
  }

  @ExportScriptTest
  void testThatNamesIsSet(final Context context, final Patient resource) {
    assumeTrue(!isPseudo(context), "Patient is pseudonymized")

    assumeTrue(context.source[patient().firstName()] || context.source[patient().lastName()])

    final HumanName name = resource.getName().find {
      it.use == HumanName.NameUse.OFFICIAL
    }

    assertNotNull(name)

    assumeTrue(context.source[patient().firstName()] != name)
    assertEquals(context.source[patient().firstName()], name.getGivenAsSingleString())

    assumeTrue(context.source[patient().lastName()] != null)
    assertEquals(context.source[patient().lastName()], name.getFamily())
  }

  @ExportScriptTest
  void testThatBirthNamesIsSet(final Context context, final Patient resource) {
    assumeTrue(!isPseudo(context), "Patient is pseudonymized")

    assumeTrue(context.source[patient().birthName()] != null)

    final HumanName name = resource.getName().find {
      it.use == HumanName.NameUse.MAIDEN
    }

    assertNotNull(name)
    assertEquals(context.source[patient().birthName()], name.getFamily())
  }


  private static boolean isPseudo(@Nonnull final Context context) {
    return isBlank(context.source[patient().firstName()] as String) && isBlank(context.source[patient().lastName()] as String)
  }

}
