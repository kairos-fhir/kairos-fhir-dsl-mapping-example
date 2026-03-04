package customexport.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import common.utils.LflvUtils
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Encounter

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/customexport/mii/bielefeld/encounter_abt_kontakt.groovy",
    contextMapsPath = "src/test/resources/customexport/mii/bielefeld/encounter_abt_kontakt"
)
class EncounterAbtKontaktExportScriptTest extends AbstractExportScriptTest<Encounter> {

  final String STATUS = "Encounter.status"
  private static final LM_CODE = "MP_Encounter_Abteilungskontakt"


  final String FAS_CODE = "Encounter.serviceType.coding:fachabteilungsschluessel.code"
  final String FAS_CODE2 = "Encounter.serviceType.coding:fachabteilungsschluessel.code_2"
  final String FAS_CODE3 = "Encounter.serviceType.coding:fachabteilungsschluessel.code_3"

  final String KONTAKT_EBENE = "Encounter.type:KontaktEbene"
  final String KONTAKT_ART = "Versorgungsstellenkontakt_Encounter.type:KontaktArt"
  final String PERIOD_START = "Encounter.period.start"
  final String PERIOD_END = "Encounter.period.end"
  final String ENCOUNTER_ID = "Encounter_Fachabteilung_KIS_Bezeichnung"


  final Map PROFILE_TYPES = [
      (STATUS)       : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,

      (FAS_CODE)     : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (FAS_CODE2)    : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (FAS_CODE3)    : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,

      (KONTAKT_EBENE): LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (KONTAKT_ART)  : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (PERIOD_START) : LaborFindingLaborValue.DATE_VALUE,
      (PERIOD_END)   : LaborFindingLaborValue.DATE_VALUE,
      (ENCOUNTER_ID) : LaborFindingLaborValue.STRING_VALUE
  ]


  @ExportScriptTest
  void validateResourceStructures(final Context context, final Encounter resource) {
    getValidator("fhirpackages/mii").validate(resource)
  }

  @ExportScriptTest
  void testThatIdentifiersAreSet(final Context context, final Encounter resource) {

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(context.source, PROFILE_TYPES)

    final String expected = lflvMap.get(ENCOUNTER_ID)

    assumeTrue(expected != null)

    assertTrue(resource.hasIdentifier())
    assertEquals(1, resource.getIdentifier().size())

    resource.getIdentifier()
        .collect { it.getType() }
        .each {
          assertNotNull(it)
          assertTrue(it.hasCoding("http://terminology.hl7.org/CodeSystem/v2-0203", "VN"))
        }

    assertEquals("https://fhir.centraxx.de/system/idContainer/psn", resource.getIdentifierFirstRep().getSystem())
    assertEquals(expected, resource.getIdentifierFirstRep().getValue())

  }

/*  @ExportScriptTest
  void testThatClassIsSet(final Context context, final Encounter resource) {
    assumeTrue(context.source[episode().stayType()] != null)

    assertTrue(resource.hasClass_())
    assertEquals("http://terminology.hl7.org/CodeSystem/v3-ActCode", resource.getClass_().getSystem())
    assertEquals(context.source[episode().stayType().code()], resource.getClass_().getCode())
  }*/

  @ExportScriptTest
  void testThatSubjectIsSet(final Context context, final Encounter resource) {
    assertTrue(resource.hasSubject())
    assertEquals("Patient/" + context.source[laborMapping().relatedPatient().id()], resource.getSubject().getReference())
  }

  @ExportScriptTest
  void testThatPeriodStartIsSet(final Context context, final Encounter resource) {

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(context.source, PROFILE_TYPES)

    final def expected = lflvMap.get(PERIOD_START)

    assumeTrue(expected != null && expected[PrecisionDate.DATE] != null)

    assertTrue(resource.hasPeriod())

    assertEquals(new DateTimeType(expected[PrecisionDate.DATE] as String).getValue(),
        resource.getPeriod().getStart())
  }

  @ExportScriptTest
  void testThatPeriodEndIsSet(final Context context, final Encounter resource) {

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(context.source, PROFILE_TYPES)

    final def expected = lflvMap.get(PERIOD_END)

    assumeTrue(expected != null && expected[PrecisionDate.DATE] != null)

    assertTrue(resource.hasPeriod())

    assertEquals(new DateTimeType(expected[PrecisionDate.DATE] as String).getValue(),
        resource.getPeriod().getEnd())

  }

  @ExportScriptTest
  void testThatEncounterStatusIsSet(final Context context, final Encounter resource) {

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(context.source, PROFILE_TYPES)

    final String expected = lflvMap.get(STATUS)?.find()?.getAt(CODE)
    assumingThat(lflvMap.get(STATUS) != null,
        { ->
          assertEquals(expected, resource.getStatus().toCode())
        }
    )

    assumingThat(lflvMap.get(STATUS) == null,
        { ->
          assertEquals(Encounter.EncounterStatus.UNKNOWN.toCode(), resource.getStatus().toCode())
        }
    )
  }

  @ExportScriptTest
  void testThatEncounterKontaktEbeneIsSet(final Context context, final Encounter resource) {

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(context.source, PROFILE_TYPES)

    assumingThat(lflvMap.get(KONTAKT_EBENE) != null,
        { ->
          final String expectedValue = lflvMap.get(KONTAKT_EBENE).find()?.getAt(CODE)
          assertEquals(expectedValue, resource.getTypeFirstRep()?.getCodingFirstRep()?.getCode())
        }
    )

    assumingThat(lflvMap.get(KONTAKT_EBENE) == null,
        { -> assertEquals("abteilungskontakt", resource.getTypeFirstRep()?.getCodingFirstRep()?.getCode()) }
    )
  }

  @ExportScriptTest
  void testThatEncounterFasIsSet(final Context context, final Encounter resource) {

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(context.source, PROFILE_TYPES)

    final String fas = lflvMap.get(FAS_CODE)?.find()?.getAt(CODE)
    final String fas2 = lflvMap.get(FAS_CODE2)?.find()?.getAt(CODE)
    final String fas3 = lflvMap.get(FAS_CODE3)?.find()?.getAt(CODE)

    assumeTrue([fas, fas2, fas3].any { it != null })


    assertTrue(resource.hasServiceType())

    assumingThat(fas != null,
        { ->
          assertTrue(resource.getServiceType().hasCoding(
              "http://fhir.de/CodeSystem/dkgev/Fachabteilungsschluessel",
              fas
          ))
        }
    )

    assumingThat(fas2 != null,
        { ->
          assertTrue(resource.getServiceType().hasCoding(
              "http://fhir.de/CodeSystem/dkgev/Fachabteilungsschluessel-erweitert",
              fas2
          ))
        }
    )

    assumingThat(fas2 == null && fas3 != null,
        { ->
          assertTrue(resource.getServiceType().hasCoding(
              "http://fhir.de/CodeSystem/dkgev/Fachabteilungsschluessel-erweitert",
              fas3
          ))
        }
    )
  }
}
