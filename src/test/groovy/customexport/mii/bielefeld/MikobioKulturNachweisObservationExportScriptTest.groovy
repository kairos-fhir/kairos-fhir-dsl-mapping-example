package customexport.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/customexport/mii/bielefeld/microBioKulturNachweisObservation.groovy",
    contextMapsPath = "src/test/resources/customexport/mii/bielefeld/microBioKulturNachweisObservation"
)
class MikobioKulturNachweisObservationExportScriptTest extends AbstractExportScriptTest<Bundle> {

  private static final String LM_CODE = "MP_Mikrobio_KulturNachweis"

  private static final String OBS_STATUS = "Observation.status"
  private static final String OBS_KL_BEZUGS_DATUM = "Observation.effective[x].extension:QuelleKlinischesBezugsdatum.value[x].code"
  private static final String OBS_EFFECTIVE = "Observation.effective[x]"
  private static final String OBS_VAL_CODEABLE_CONCEPT = "Observation.value[x]:valueCodeableConcept"
  private static final String EMP_CODE = "Observation.code_Empfindlichkeit"
  private static final String EMP_INTERPRETATION = "Observation.interpretation:EUCAST"

  private static final Map PROFILE_TYPES = [
      (OBS_STATUS)              : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (OBS_KL_BEZUGS_DATUM)     : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (OBS_EFFECTIVE)           : LaborFindingLaborValue.DATE_VALUE,
      (OBS_VAL_CODEABLE_CONCEPT): LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (EMP_CODE)                : LaborFindingLaborValue.STRING_VALUE,
      (EMP_INTERPRETATION)      : LaborFindingLaborValue.CATALOG_ENTRY_VALUE
  ]

  @ExportScriptTest
  void validateResourceStructures(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final def validator = getValidator("fhirpackages/mii")
    resource.getEntry().each {
      validator.validate(it.getResource())
    }
  }

  @ExportScriptTest
  void testThatBundleContainsExpectedObservations(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final Map<Integer, Map<String, Object>> lflvMap = getIndexedLflvMap(context)

    // one Empfindlichkeit Observation per valueIndex + one Kulturnachweis Observation
    assertEquals(lflvMap.size() + 1, getObservations(resource).size())
  }

  @ExportScriptTest
  void testThatKulturNachweisIdentifierIsSet(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final Observation observation = getKulturNachweisObservation(context, resource)
    assertNotNull(observation)

    assertTrue(observation.hasIdentifier())
    assertTrue(observation.getIdentifierFirstRep().getType()
        .hasCoding("http://terminology.hl7.org/CodeSystem/v2-0203", "OBI"))
    assertEquals("https://ukowl.de/sid/mii-exa-mikrobio-kulturnachweis", observation.getIdentifierFirstRep().getSystem())
    assertEquals(context.source[laborFinding().laborFindingId()], observation.getIdentifierFirstRep().getValue())
    assertEquals("ukowl.de", observation.getIdentifierFirstRep().getAssigner().getIdentifier().getValue())
  }

  @ExportScriptTest
  void testThatKulturNachweisStatusIsSet(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final Map<String, Object> obsLflvMap = getIndexedLflvMap(context).get(0)
    final Observation observation = getKulturNachweisObservation(context, resource)
    final String expected = obsLflvMap?.get(OBS_STATUS)?.find()?.getAt(CODE) as String

    assumingThat(expected != null,
        {
          ->
          assertEquals(expected.toLowerCase(), observation.getStatus().toCode().toLowerCase())
        })

    assumingThat(expected == null,
        {
          ->
          assertEquals(Observation.ObservationStatus.UNKNOWN.toCode(), observation.getStatus().toCode())
        })
  }

  @ExportScriptTest
  void testThatKulturNachweisCategoryIsSet(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final Observation observation = getKulturNachweisObservation(context, resource)

    assertTrue(observation.hasCategory())
    assertTrue(observation.getCategoryFirstRep().hasCoding("http://loinc.org", "26436-6"))
    assertTrue(observation.getCategoryFirstRep()
        .hasCoding("http://terminology.hl7.org/CodeSystem/observation-category", "laboratory"))
    assertTrue(observation.getCategoryFirstRep().hasCoding("http://loinc.org", "18725-2"))
  }

  @ExportScriptTest
  void testThatKulturNachweisCodeIsSet(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final Observation observation = getKulturNachweisObservation(context, resource)

    assertTrue(observation.hasCode())
    assertTrue(observation.getCode().hasCoding("http://loinc.org", "11475-1"))
  }

  @ExportScriptTest
  void testThatKulturNachweisSubjectIsSet(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final def lm = context.source[laborFinding().laborMappings()].find {
      it[LaborMapping.RELATED_PATIENT] != null
    }
    assumeTrue(lm != null, "No patient labor mapping")

    final Observation observation = getKulturNachweisObservation(context, resource)
    assertEquals(
        "Patient/" + lm[LaborMapping.RELATED_PATIENT][PatientContainer.ID],
        observation.getSubject().getReference()
    )
  }

  @ExportScriptTest
  void testThatKulturNachweisEffectiveIsSet(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final Map<String, Object> obsLflvMap = getIndexedLflvMap(context).get(0)
    final def effectivePD = obsLflvMap?.get(OBS_EFFECTIVE) as Map

    assumeTrue(effectivePD != null && effectivePD[PrecisionDate.DATE] != null, "No effective date given")

    final Observation observation = getKulturNachweisObservation(context, resource)
    assertTrue(observation.hasEffectiveDateTimeType())
    assertEquals(
        new DateTimeType(effectivePD[PrecisionDate.DATE] as String).getValue(),
        observation.getEffectiveDateTimeType().getValue()
    )
  }

  @ExportScriptTest
  void testThatKulturNachweisKlinischesBezugsdatumIsSet(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final Map<String, Object> obsLflvMap = getIndexedLflvMap(context).get(0)
    final def effectivePD = obsLflvMap?.get(OBS_EFFECTIVE) as Map
    final String expectedCode = obsLflvMap?.get(OBS_KL_BEZUGS_DATUM)?.find()?.getAt(CODE) as String

    assumeTrue(effectivePD != null && effectivePD[PrecisionDate.DATE] != null, "No effective date given")
    assumeTrue(expectedCode != null, "No KlinischesBezugsdatum given")

    final Observation observation = getKulturNachweisObservation(context, resource)
    final Extension extension = observation.getEffectiveDateTimeType().getExtensionByUrl(
        "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum"
    )

    assertNotNull(extension)
    assertEquals(expectedCode, ((Coding) extension.getValue()).getCode())
  }

  @ExportScriptTest
  void testThatKulturNachweisValueIsSet(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final Map<String, Object> obsLflvMap = getIndexedLflvMap(context).get(0)
    final String expectedCode = obsLflvMap?.get(OBS_VAL_CODEABLE_CONCEPT)?.find()?.getAt(CODE) as String

    assumeTrue(expectedCode != null, "No valueCodeableConcept given")

    final Observation observation = getKulturNachweisObservation(context, resource)
    assertTrue(observation.hasValueCodeableConcept())
    assertTrue(observation.getValueCodeableConcept().hasCoding("http://snomed.info/sct", expectedCode))
  }

  @ExportScriptTest
  void testThatKulturNachweisHasMembersAreSet(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final Map<Integer, Map<String, Object>> lflvMap = getIndexedLflvMap(context)
    final Observation observation = getKulturNachweisObservation(context, resource)

    assertEquals(lflvMap.size(), observation.getHasMember().size())

    lflvMap.keySet().each { final Integer valueIndex ->
      final String expectedRef = "Observation/" + context.source[laborFinding().id()] + "-" + valueIndex
      assertTrue(observation.getHasMember().any { it.getReference() == expectedRef })
    }
  }

  @ExportScriptTest
  void testThatEmpfindlichkeitObservationsAreMapped(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final Map<Integer, Map<String, Object>> lflvMap = getIndexedLflvMap(context)
    final def lm = context.source[laborFinding().laborMappings()].find {
      it[LaborMapping.RELATED_PATIENT] != null
    }

    lflvMap.each { final Integer valueIndex, final Map<String, Object> empLflvMap ->
      final Observation observation = getEmpfindlichkeitObservation(context, resource, valueIndex)
      assertNotNull(observation, "Missing Empfindlichkeit Observation for valueIndex " + valueIndex)

      assertTrue(observation.getIdentifierFirstRep().getType()
          .hasCoding("http://terminology.hl7.org/CodeSystem/v2-0203", "OBI"))
      assertEquals("https://ukowl.de/sid/mii-exa-mikrobio-empfindlichkeit", observation.getIdentifierFirstRep().getSystem())
      assertEquals(context.source[laborFinding().laborFindingId()], observation.getIdentifierFirstRep().getValue())

      final String expectedEmpCode = empLflvMap.get(EMP_CODE) as String
      assumingThat(expectedEmpCode != null,
          {
            ->
            assertTrue(observation.getCode().hasCoding("http://loinc.org", expectedEmpCode))
          })

      if (lm != null) {
        assertEquals(
            "Patient/" + lm[LaborMapping.RELATED_PATIENT][PatientContainer.ID],
            observation.getSubject().getReference()
        )
      }

      final String expectedInterpretation = empLflvMap.get(EMP_INTERPRETATION)?.find()?.getAt(CODE) as String
      assumingThat(expectedInterpretation != null,
          {
            ->
            assertTrue(observation.hasInterpretation())
            assertTrue(observation.getInterpretationFirstRep()
                .hasCoding("http://snomed.info/sct", expectedInterpretation))
          })
    }
  }

  @ExportScriptTest
  void testThatEmpfindlichkeitStatusAndEffectiveFollowKulturNachweis(final Context context, final Bundle resource) {
    checkLaborMethodCode(context)

    final Map<Integer, Map<String, Object>> lflvMap = getIndexedLflvMap(context)
    final Map<String, Object> obsLflvMap = lflvMap.get(0)
    final String expectedStatus = obsLflvMap?.get(OBS_STATUS)?.find()?.getAt(CODE) as String
    final def effectivePD = obsLflvMap?.get(OBS_EFFECTIVE) as Map

    lflvMap.keySet().each { final Integer valueIndex ->
      final Observation observation = getEmpfindlichkeitObservation(context, resource, valueIndex)

      assumingThat(expectedStatus != null,
          {
            ->
            assertEquals(expectedStatus.toLowerCase(), observation.getStatus().toCode().toLowerCase())
          })

      assumingThat(effectivePD != null && effectivePD[PrecisionDate.DATE] != null,
          {
            ->
            assertEquals(
                new DateTimeType(effectivePD[PrecisionDate.DATE] as String).getValue(),
                observation.getEffectiveDateTimeType().getValue()
            )
          })
    }
  }

  private static Observation getKulturNachweisObservation(final Context context, final Bundle resource) {
    final String expectedId = "Observation/" + context.source[laborFinding().id()]
    return getObservations(resource).find { it.getId() == expectedId }
  }

  private static Observation getEmpfindlichkeitObservation(final Context context,
                                                          final Bundle resource,
                                                          final Integer valueIndex) {
    final String expectedId = "Observation/" + context.source[laborFinding().id()] + "-" + valueIndex
    return getObservations(resource).find { it.getId() == expectedId }
  }

  private static List<Observation> getObservations(final Bundle bundle) {
    return bundle.getEntry().collect { it.getResource() }
        .findAll { it instanceof Observation } as List<Observation>
  }

  private static Map<Integer, Map<String, Object>> getIndexedLflvMap(final Context context) {
    final Map<Integer, Map<String, Object>> lflvMap = [:]
    final List lflvs = context.source[laborFinding().laborFindingLaborValues()] as List

    PROFILE_TYPES.each { final String lvCode, final String lvType ->
      final def lflvsForLv = lflvs.findAll { final def lflv ->
        lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == lvCode
      }

      lflvsForLv.each {
        if (it[lvType]) {
          final Integer valueIndex = it[LaborFindingLaborValue.VALUE_INDEX] as Integer
          lflvMap.computeIfAbsent(valueIndex, { k -> new HashMap<>() }).put(lvCode, it[lvType])
        }
      }
    }

    return lflvMap
  }

  private static void checkLaborMethodCode(final Context context) {
    assumeTrue(context.source[laborFinding().laborMethod().code()] == LM_CODE, "Not a Mikrobio KulturNachweis profile")
  }
}
