package customexport.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import common.utils.LflvUtils
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Identifier

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/customexport/mii/bielefeld/encounter.groovy",
    contextMapsPath = "src/test/resources/customexport/mii/bielefeld/encounter"
)
class EpisodeExportScriptTest extends AbstractExportScriptTest<Encounter> {

  private static final LM_CODE = "MP_Encounter"

  final String STATUS = "Encounter.status"
  final String AUFNAHME_GRUND_12 = "Encounter.extension:Aufnahmegrund.extension:ErsteUndZweiteStelle.value[x]"
  final String AUFNAHME_GRUND_3 = "Encounter.extension:Aufnahmegrund.extension:DritteStelle.value[x]"
  final String AUFNAHME_GRUND_4 = "Encounter.extension:Aufnahmegrund.extension:VierteStelle.value[x]"
  final String ADMIT_SOURCE = "Encounter.hospitalization.admitSource"
  final String ENT_GRUND_12 = "Encounter.hospitalization.dischargeDisposition.extension:Entlassungsgrund.extension:ErsteUndZweiteStelle.value[x]"
  final String ENT_GRUND_3 = "Encounter.hospitalization.dischargeDisposition.extension:Entlassungsgrund.extension:DritteStelle.value[x]"
  final String KONTAKT_EBENE = "Encounter.type:KontaktEbene"
  final String KONTAKT_ART = "Versorgungsstellenkontakt_Encounter.type:KontaktArt"


  final Map PROFILE_TYPES = [
      (STATUS)           : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (AUFNAHME_GRUND_12): LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (AUFNAHME_GRUND_3) : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (AUFNAHME_GRUND_4) : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (ENT_GRUND_12)     : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (ENT_GRUND_3)      : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (KONTAKT_EBENE)    : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (KONTAKT_ART)      : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (ADMIT_SOURCE)     : LaborFindingLaborValue.CATALOG_ENTRY_VALUE
  ]


  @ExportScriptTest
  void validateResourceStructures(final Context context, final Encounter resource) {
    getValidator("fhirpackages/mii").validate(resource)
  }

  @ExportScriptTest
  void testThatIdentifiersAreSet(final Context context, final Encounter resource) {

    assumeTrue((context.source[episode().idContainer()] as List).size() > 0)

    assertTrue(resource.hasIdentifier())
    assertEquals((context.source[episode().idContainer()] as List).size(), resource.getIdentifier().size())

    resource.getIdentifier()
        .collect { it.getType() }
        .each {
          assertNotNull(it)
          assertTrue(it.hasCoding("http://terminology.hl7.org/CodeSystem/v2-0203", "VN"))
        }

    resource.getIdentifier().each { final Identifier fhirIdentifier ->
      final def idc = context.source[episode().idContainer()].find {
        "https://fhir.centraxx.de/system/idContainer/psn".equals(fhirIdentifier.getSystem())
      }

      assertNotNull(idc)
      assertEquals(idc[IdContainer.PSN], fhirIdentifier.getValue())
    }
  }

  @ExportScriptTest
  void testThatClassIsSet(final Context context, final Encounter resource) {
    assumeTrue(context.source[episode().stayType()] != null)

    assertTrue(resource.hasClass_())
    assertEquals("http://terminology.hl7.org/CodeSystem/v3-ActCode", resource.getClass_().getSystem())
    assertEquals(context.source[episode().stayType().code()], resource.getClass_().getCode())
  }

  @ExportScriptTest
  void testThatSubjectIsSet(final Context context, final Encounter resource) {
    assertTrue(resource.hasSubject())
    assertEquals("Patient/" + context.source[episode().patientContainer().id()], resource.getSubject().getReference())
  }

  @ExportScriptTest
  void testThatPeriodIsSet(final Context context, final Encounter resource) {

    assumeTrue(context.source[episode().validFrom()] || context.source[episode().validUntil()])

    assertTrue(resource.hasPeriod())


    assumingThat(context.source[episode().validFrom()] != null,
        { ->
          assertEquals(new DateTimeType(context.source[episode().validFrom()] as String).getValue(),
              resource.getPeriod().getStart())
        }
    )

    assumingThat(context.source[episode().validUntil()] != null,
        { ->
          assertEquals(new DateTimeType(context.source[episode().validUntil()] as String).getValue(),
              resource.getPeriod().getEnd())
        }
    )
  }

  @ExportScriptTest
  void testThatEncounterStatusIsSet(final Context context, final Encounter resource) {

    final def encounterMapping = context.source[episode().laborMappings()]
        .find { final def lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == LM_CODE }

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(encounterMapping, PROFILE_TYPES)

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
  void testThatEncounterAfg12IsSet(final Context context, final Encounter resource) {

    final def encounterMapping = context.source[episode().laborMappings()]
        .find { final def lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == LM_CODE }

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(encounterMapping, PROFILE_TYPES)


    assumingThat(lflvMap.get(AUFNAHME_GRUND_12) != null,
        { ->
          final Extension afgExt = resource.getExtensionByUrl("http://fhir.de/StructureDefinition/Aufnahmegrund")

          assertNotNull(afgExt)

          final Extension subExt = afgExt.getExtensionByUrl("ErsteUndZweiteStelle")

          assertNotNull(subExt)

          final String expectedValue = lflvMap.get(AUFNAHME_GRUND_12).find()?.getAt(CODE)

          final String actualValue = ((Coding) subExt.getValue()).getCode()

          assertEquals(expectedValue, actualValue)
        }
    )
  }

  @ExportScriptTest
  void testThatEncounterAfg3IsSet(final Context context, final Encounter resource) {

    final def encounterMapping = context.source[episode().laborMappings()]
        .find { final def lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == LM_CODE }

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(encounterMapping, PROFILE_TYPES)

    assumingThat(lflvMap.get(AUFNAHME_GRUND_3) != null,
        { ->
          final Extension afgExt = resource.getExtensionByUrl("http://fhir.de/StructureDefinition/Aufnahmegrund")

          assertNotNull(afgExt)

          final Extension subExt = afgExt.getExtensionByUrl("DritteStelle")

          assertNotNull(subExt)

          final String expectedValue = lflvMap.get(AUFNAHME_GRUND_3).find()?.getAt(CODE)

          final String actualValue = ((Coding) subExt.getValue()).getCode()

          assertEquals(expectedValue, actualValue)
        }
    )
  }

  @ExportScriptTest
  void testThatEncounterAfg4IsSet(final Context context, final Encounter resource) {

    final def encounterMapping = context.source[episode().laborMappings()]
        .find { final def lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == LM_CODE }

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(encounterMapping, PROFILE_TYPES)

    assumingThat(lflvMap.get(AUFNAHME_GRUND_4) != null,
        { ->
          final Extension afgExt = resource.getExtensionByUrl("http://fhir.de/StructureDefinition/Aufnahmegrund")

          assertNotNull(afgExt)

          final Extension subExt = afgExt.getExtensionByUrl("VierteStelle")

          assertNotNull(subExt)

          final String expectedValue = lflvMap.get(AUFNAHME_GRUND_4).find()?.getAt(CODE)

          final String actualValue = ((Coding) subExt.getValue()).getCode()

          assertEquals(expectedValue, actualValue)
        }
    )
  }

  @ExportScriptTest
  void testThatEncounterAdmitSourceIsSet(final Context context, final Encounter resource) {

    final def encounterMapping = context.source[episode().laborMappings()]
        .find { final def lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == LM_CODE }

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(encounterMapping, PROFILE_TYPES)

    assumingThat(lflvMap.get(ADMIT_SOURCE) != null,
        { ->

          final String expectedValue = lflvMap.get(ADMIT_SOURCE).find()?.getAt(CODE)

          assertEquals(expectedValue, resource.getHospitalization()?.getAdmitSource()?.getCodingFirstRep()?.getCode())

        }
    )
  }

  @ExportScriptTest
  void testThatEncounterEg12IsSet(final Context context, final Encounter resource) {

    final def encounterMapping = context.source[episode().laborMappings()]
        .find { final def lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == LM_CODE }

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(encounterMapping, PROFILE_TYPES)

    println(lflvMap.get(ENT_GRUND_12).find()?.getAt(CODE))
    assumingThat(lflvMap.get(ENT_GRUND_12) != null,
        { ->


          final Extension egExt = resource.getHospitalization()
              ?.getDischargeDisposition()
              ?.getExtensionByUrl("http://fhir.de/StructureDefinition/Entlassungsgrund")

          assertNotNull(egExt)

          final Extension subExt = egExt.getExtensionByUrl("ErsteUndZweiteStelle")

          assertNotNull(subExt)

          final String expectedValue = lflvMap.get(ENT_GRUND_12).find()?.getAt(CODE)

          final String actualValue = ((Coding) subExt.getValue()).getCode()

          assertEquals(expectedValue, actualValue)
        }
    )
  }


  @ExportScriptTest
  void testThatEncounterEg3IsSet(final Context context, final Encounter resource) {

    final def encounterMapping = context.source[episode().laborMappings()]
        .find { final def lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == LM_CODE }

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(encounterMapping, PROFILE_TYPES)

    assumingThat(lflvMap.get(ENT_GRUND_3) != null,
        { ->

          assertTrue(resource.hasHospitalization())

          final Extension egExt = resource.getHospitalization()
              ?.getDischargeDisposition()
              ?.getExtensionByUrl("http://fhir.de/StructureDefinition/Entlassungsgrund")

          assertNotNull(egExt)

          final Extension subExt = egExt.getExtensionByUrl("DritteStelle")

          assertNotNull(subExt)

          final String expectedValue = lflvMap.get(ENT_GRUND_3).find()?.getAt(CODE)

          final String actualValue = ((Coding) subExt.getValue()).getCode()

          assertEquals(expectedValue, actualValue)
        }
    )
  }

  @ExportScriptTest
  void testThatEncounterKontaktEbeneIsSet(final Context context, final Encounter resource) {

    final def encounterMapping = context.source[episode().laborMappings()]
        .find { final def lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == LM_CODE }

    final def lflvMap = LflvUtils.getLflvMapFromLaborMapping(encounterMapping, PROFILE_TYPES)

    assumingThat(lflvMap.get(KONTAKT_EBENE) != null,
        { ->

          final String expectedValue = lflvMap.get(KONTAKT_EBENE).find()?.getAt(CODE)
          assertEquals(expectedValue, resource.getTypeFirstRep()?.getCodingFirstRep()?.getCode())
        }
    )

    assumingThat(lflvMap.get(KONTAKT_EBENE) == null,
        { -> assertEquals("einrichtungskontakt", resource.getTypeFirstRep()?.getCodingFirstRep()?.getCode()) }
    )
  }


}
