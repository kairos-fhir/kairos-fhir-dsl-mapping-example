package customexport.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import common.utils.LflvUtils
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.MedicationStatement
import org.hl7.fhir.r4.model.Resource

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/customexport/mii/bielefeld/medicationBundle.groovy",
    contextMapsPath = "src/test/resources/customexport/mii/bielefeld/medication"
)
class MedicationStatementExportScriptTest extends AbstractExportScriptTest<Bundle> {

  private static final LM_MED_STAT_CODE = "MP_MedicationStatement"
  private static final LM_MED_AD_CODE = "MP_MedicationAdmininstration"


  final String MS_IDENTIFIER = "MedicationStatement.identifier"
  final String MS_CATEGORY = "MedicationStatement.category"
  final String MS_NOTE = "MedicationStatement.note"
  final String MS_DOSAGE_ROUTE = "MedicationStatement.dosage.route"
  final String MS_DOSAGE_DOSEANDRATE_RATE_RATERATIO_NUMERATOR_VALUE = "MedicationStatement.dosage.doseAndRate.rate[x]:rateRatio.numerator.value"
  final String MS_DOSAGE_SITE_TEXT = "MedicationStatement.dosage.site.text"
  final String MS_DOSAGE_TIMING_REPEAT_COUNTMAX = "MedicationStatement.dosage.timing.repeat.countMax"
  final String MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_VALUE = "MedicationStatement.dosage.doseAndRate.dose[x]:doseQuantity.value"
  final String MS_DOSAGE_TEXT = "MedicationStatement.dosage.text"
  final String MS_DOSAGE_TIMING_REPEAT_COUNT = "MedicationStatement.dosage.timing.repeat.count"
  final String MS_DOSAGE_TIMING_REPEAT_PERIOD = "MedicationStatement.dosage.timing.repeat.period"
  final String MS_DOSAGE_TIMING_EVENT = "MedicationStatement.dosage.timing.event"
  final String MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_UNIT = "MedicationStatement.dosage.doseAndRate.dose[x]:doseQuantity.unit"
  final String MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_CODE = "MedicationStatement.dosage.doseAndRate.dose[x]:doseQuantity.code"
  final String MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSPERIOD_START = "MedicationStatement.dosage.timing.repeat.bounds[x]:boundsPeriod.start"
  final String MS_EFFECTIVE_EFFECTIVEPERIOD_END = "MedicationStatement.effective[x]:effectivePeriod.end"
  final String MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSDURATION_VALUE = "MedicationStatement.dosage.timing.repeat.bounds[x]:boundsDuration.value"
  final String MS_MEDICATION_MEDICATIONCODEABLECONCEPT_TEXT = "MedicationStatement.medication[x]:medicationCodeableConcept.text"
  final String MS_DATEASSERTED = "MedicationStatement.dateAsserted"
  final String MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSPERIOD_END = "MedicationStatement.dosage.timing.repeat.bounds[x]:boundsPeriod.end"
  final String MS_DOSAGE_SEQUENCE = "MedicationStatement.dosage.sequence"
  final String MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSDURATION_UNIT = "MedicationStatement.dosage.timing.repeat.bounds[x]:boundsDuration.unit"
  final String MS_DOSAGE_MAXDOSEPERPERIOD_NUMERATOR_VALUE = "MedicationStatement.dosage.maxDosePerPeriod.numerator.value"
  final String MS_EFFECTIVE_EFFECTIVEPERIOD_START = "MedicationStatement.effective[x]:effectivePeriod.start"
  final String MS_DOSAGE_SITE_CODING = "MedicationStatement.dosage.site.coding"
  final String MS_DOSAGE_DOSEANDRATE_RATE_RATERANGE = "MedicationStatement.dosage.doseAndRate.rate[x]:rateRange"
  final String MS_STATUS = "MedicationStatement.status"
  final String MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSRANGE = "MedicationStatement.dosage.timing.repeat.bounds[x]:boundsRange"
  final String MS_DOSAGE_DOSEANDRATE_RATE_RATEQUANTITY = "MedicationStatement.dosage.doseAndRate.rate[x]:rateQuantity"
  final String MS_REASONCODE = "MedicationStatement.reasonCode"
  final String MS_EFFECTIVE_EFFECTIVEDATETIME = "MedicationStatement.effective[x]:effectiveDateTime"
  final String MS_DOSAGE_ASNEEDED = "MedicationStatement.dosage.asNeeded[x]"
  final String MS_DOSAGE_DOSEANDRATE_RATE_RATERATIO_NUMERATOR_UNIT = "MedicationStatement.dosage.doseAndRate.rate[x]:rateRatio.numerator.unit"

  final Map MED_STAT_PROFILE_TYPE = [
      (MS_IDENTIFIER)                                       : LaborFindingLaborValue.STRING_VALUE,
      (MS_CATEGORY)                                         : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (MS_NOTE)                                             : LaborFindingLaborValue.STRING_VALUE,
      (MS_DOSAGE_ROUTE)                                     : LaborFindingLaborValue.STRING_VALUE,
      (MS_DOSAGE_DOSEANDRATE_RATE_RATERATIO_NUMERATOR_VALUE): LaborFindingLaborValue.NUMERIC_VALUE,
      (MS_DOSAGE_SITE_TEXT)                                 : LaborFindingLaborValue.STRING_VALUE,
      (MS_DOSAGE_TIMING_REPEAT_COUNTMAX)                    : LaborFindingLaborValue.NUMERIC_VALUE,
      (MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_VALUE)       : LaborFindingLaborValue.NUMERIC_VALUE,
      (MS_DOSAGE_TEXT)                                      : LaborFindingLaborValue.STRING_VALUE,
      (MS_DOSAGE_TIMING_REPEAT_COUNT)                       : LaborFindingLaborValue.NUMERIC_VALUE,
      (MS_DOSAGE_TIMING_REPEAT_PERIOD)                      : LaborFindingLaborValue.NUMERIC_VALUE,
      (MS_DOSAGE_TIMING_EVENT)                              : LaborFindingLaborValue.DATE_VALUE,
      (MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_UNIT)        : LaborFindingLaborValue.STRING_VALUE,
      (MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_CODE)        : LaborFindingLaborValue.STRING_VALUE,
      (MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSPERIOD_START)   : LaborFindingLaborValue.DATE_VALUE,
      (MS_EFFECTIVE_EFFECTIVEPERIOD_END)                    : LaborFindingLaborValue.DATE_VALUE,
      (MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSDURATION_VALUE) : LaborFindingLaborValue.NUMERIC_VALUE,
      (MS_MEDICATION_MEDICATIONCODEABLECONCEPT_TEXT)        : LaborFindingLaborValue.STRING_VALUE,
      (MS_DATEASSERTED)                                     : LaborFindingLaborValue.DATE_VALUE,
      (MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSPERIOD_END)     : LaborFindingLaborValue.DATE_VALUE,
      (MS_DOSAGE_SEQUENCE)                                  : LaborFindingLaborValue.STRING_VALUE,
      (MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSDURATION_UNIT)  : LaborFindingLaborValue.STRING_VALUE,
      (MS_DOSAGE_MAXDOSEPERPERIOD_NUMERATOR_VALUE)          : LaborFindingLaborValue.NUMERIC_VALUE,
      (MS_EFFECTIVE_EFFECTIVEPERIOD_START)                  : LaborFindingLaborValue.DATE_VALUE,
      (MS_DOSAGE_SITE_CODING)                               : LaborFindingLaborValue.STRING_VALUE,
      (MS_DOSAGE_DOSEANDRATE_RATE_RATERANGE)                : LaborFindingLaborValue.STRING_VALUE,
      (MS_STATUS)                                           : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSRANGE)          : LaborFindingLaborValue.STRING_VALUE,
      (MS_DOSAGE_DOSEANDRATE_RATE_RATEQUANTITY)             : LaborFindingLaborValue.STRING_VALUE,
      (MS_REASONCODE)                                       : LaborFindingLaborValue.STRING_VALUE,
      (MS_EFFECTIVE_EFFECTIVEDATETIME)                      : LaborFindingLaborValue.DATE_VALUE,
      (MS_DOSAGE_ASNEEDED)                                  : LaborFindingLaborValue.BOOLEAN_VALUE,
      (MS_DOSAGE_DOSEANDRATE_RATE_RATERATIO_NUMERATOR_UNIT) : LaborFindingLaborValue.STRING_VALUE
  ]


  @ExportScriptTest
  void validateResourceStructures(final Context context, final Bundle resource) {

    final def validator = getValidator("fhirpackages/mii")
    resource.getEntry().each {
      validator.validate(it.getResource())
    }
  }

  @ExportScriptTest
  void testBundleContainsExpectedResourceCounts(final Context context, final Bundle resource) {

    final List<Map<String, Object>> medStatementMappings = laborMappingsByMethodCode(context, LM_MED_STAT_CODE)

    assertEquals(Bundle.BundleType.COLLECTION, resource.getType())
    assertEquals(medStatementMappings.size(), getMedicationStatements(resource).size())
  }


  @ExportScriptTest
  void testMedicationStatementSubjectRefIsSet(final Context context, final Bundle resource) {
    assertNotNull(resource, "No bundle generated")

    final String patientRef = "Patient/" + context.source[medication().patientContainer().id()]

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assertEquals(patientRef, medStatement.getSubject().getReference())
    }
  }

  @ExportScriptTest
  void testMedicationStatementId(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assertEquals("MedicationStatement/"+medStatementMapping[LaborMapping.ID], medStatement.getId())
    }
  }

  @ExportScriptTest
  void testMedicationStatementPartOf(final Context context, final Bundle resource) {

    final List<Map<String, Object>> medAdminMappings = laborMappingsByMethodCode(context, LM_MED_AD_CODE)
    final Set<String> expectedPartOfReferences = medAdminMappings
        .collect { "MedicationAdministration/" + it[LaborMapping.ID] }
        .toSet()

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assertEquals(expectedPartOfReferences, medStatement.getPartOf().collect { it.getReference() }.toSet())
    }
  }

  @ExportScriptTest
  void testMedicationStatementMedicationCodeableConcept(final Context context, final Bundle resource) {
    final String sourceCode = context.source[medication().code()] as String

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      if (sourceCode.startsWith("ATC_") || sourceCode.startsWith("PZN_")) {
        assertEquals(sourceCode.substring(4), medStatement.getMedicationCodeableConcept().getCodingFirstRep().getCode())
      } else {
        assertEquals(sourceCode, medStatement.getMedicationCodeableConcept().getText())
      }
    }
  }

  @ExportScriptTest
  void testMedicationStatementIdentifier(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_IDENTIFIER),
          { -> assertEquals(lflvMap.get(MS_IDENTIFIER), medStatement.getIdentifierFirstRep().getValue()) }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementStatus(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_STATUS),
          {
            -> assertEquals(firstCatalogCode(lflvMap.get(MS_STATUS) as List<Map<String, Object>>), medStatement.getStatus().toCode())
          }
      )

      assumingThat(!lflvMap.containsKey(MS_STATUS),
          { -> assertEquals(MedicationStatement.MedicationStatementStatus.UNKNOWN.toCode(), medStatement.getStatus().toCode()) }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementCategory(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_CATEGORY),
          {
            ->
            assertEquals("http://terminology.hl7.org/CodeSystem/medication-statement-category",
                medStatement.getCategory().getCodingFirstRep().getSystem())
            assertEquals(firstCatalogCode(lflvMap.get(MS_CATEGORY) as List<Map<String, Object>>),
                medStatement.getCategory().getCodingFirstRep().getCode())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementContext(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(medStatementMapping[LaborMapping.EPISODE] != null,
          { -> assertEquals("Encounter/" + medStatementMapping[LaborMapping.EPISODE], medStatement.getContext().getReference()) }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementDateAsserted(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_DATEASSERTED),
          {
            ->
            assertEquals(dateValue(lflvMap.get(MS_DATEASSERTED) as Map<String, Object>),
                medStatement.getDateAssertedElement().getValueAsString())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementNote(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_NOTE),
          { -> assertEquals(lflvMap.get(MS_NOTE), medStatement.getNoteFirstRep().getText()) }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementReasonCode(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_REASONCODE),
          {
            -> assertEquals(lflvMap.get(MS_REASONCODE), medStatement.getReasonCodeFirstRep().getCodingFirstRep().getCode())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementEffective(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_EFFECTIVE_EFFECTIVEDATETIME),
          {
            ->
            assertEquals(dateValue(lflvMap.get(MS_EFFECTIVE_EFFECTIVEDATETIME) as Map<String, Object>),
                medStatement.getEffectiveDateTimeType().getValueAsString())
          }
      )

      assumingThat(!lflvMap.containsKey(MS_EFFECTIVE_EFFECTIVEDATETIME)
          && (lflvMap.containsKey(MS_EFFECTIVE_EFFECTIVEPERIOD_START) || lflvMap.containsKey(MS_EFFECTIVE_EFFECTIVEPERIOD_END)),
          {
            ->
            assertEquals(dateValue(lflvMap.get(MS_EFFECTIVE_EFFECTIVEPERIOD_START) as Map<String, Object>),
                medStatement.getEffectivePeriod().getStartElement().getValueAsString())
            assertEquals(dateValue(lflvMap.get(MS_EFFECTIVE_EFFECTIVEPERIOD_END) as Map<String, Object>),
                medStatement.getEffectivePeriod().getEndElement().getValueAsString())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementDosageSequence(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_DOSAGE_SEQUENCE),
          {
            -> assertEquals((lflvMap.get(MS_DOSAGE_SEQUENCE) as String).toInteger(), medStatement.getDosageFirstRep().getSequence())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementDosageText(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_DOSAGE_TEXT),
          { -> assertEquals(lflvMap.get(MS_DOSAGE_TEXT), medStatement.getDosageFirstRep().getText()) }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementDosageTimingEvent(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_DOSAGE_TIMING_EVENT),
          {
            ->
            assertEquals(dateValue(lflvMap.get(MS_DOSAGE_TIMING_EVENT) as Map<String, Object>),
                medStatement.getDosageFirstRep().getTiming().getEvent().find()?.getValueAsString())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementDosageTimingRepeatCountAndPeriod(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_DOSAGE_TIMING_REPEAT_COUNT),
          {
            ->
            final Integer expectedCount = (lflvMap.get(MS_DOSAGE_TIMING_REPEAT_COUNT) as Number).intValue()
            assertEquals(expectedCount, medStatement.getDosageFirstRep().getTiming().getRepeat().getCount())
            assertEquals(expectedCount, medStatement.getDosageFirstRep().getTiming().getRepeat().getCountMax())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementDosageTimingRepeatBoundsDuration(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSDURATION_VALUE),
          {
            ->
            assertEquals((lflvMap.get(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSDURATION_VALUE) as Number).doubleValue(),
                medStatement.getDosageFirstRep().getTiming().getRepeat().getBoundsDuration().getValue().doubleValue())
            assertEquals(lflvMap.get(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSDURATION_UNIT),
                medStatement.getDosageFirstRep().getTiming().getRepeat().getBoundsDuration().getCode())
            assertEquals("http://unitsofmeasure.org",
                medStatement.getDosageFirstRep().getTiming().getRepeat().getBoundsDuration().getSystem())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementDosageTimingRepeatBoundsPeriod(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(!lflvMap.containsKey(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSDURATION_VALUE)
          && (lflvMap.containsKey(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSPERIOD_START)
          || lflvMap.containsKey(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSPERIOD_END)),
          {
            ->
            assertEquals(dateValue(lflvMap.get(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSPERIOD_START) as Map<String, Object>),
                medStatement.getDosageFirstRep().getTiming().getRepeat().getBoundsPeriod().getStartElement().getValueAsString())
            assertEquals(dateValue(lflvMap.get(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSPERIOD_END) as Map<String, Object>),
                medStatement.getDosageFirstRep().getTiming().getRepeat().getBoundsPeriod().getEndElement().getValueAsString())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementDosageAsNeeded(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_DOSAGE_ASNEEDED),
          {
            -> assertEquals(lflvMap.get(MS_DOSAGE_ASNEEDED), medStatement.getDosageFirstRep().getAsNeededBooleanType().getValue())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementDosageSite(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_DOSAGE_SITE_TEXT),
          {
            -> assertEquals(lflvMap.get(MS_DOSAGE_SITE_TEXT), medStatement.getDosageFirstRep().getSite().getText())
          }
      )

      assumingThat(lflvMap.containsKey(MS_DOSAGE_SITE_CODING),
          {
            ->
            assertEquals("http://snomed.info/sct", medStatement.getDosageFirstRep().getSite().getCodingFirstRep().getSystem())
            assertEquals(lflvMap.get(MS_DOSAGE_SITE_CODING), medStatement.getDosageFirstRep().getSite().getCodingFirstRep().getCode())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementDosageRoute(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_DOSAGE_ROUTE),
          {
            ->
            assertEquals("http://snomed.info/sct", medStatement.getDosageFirstRep().getRoute().getCodingFirstRep().getSystem())
            assertEquals(lflvMap.get(MS_DOSAGE_ROUTE), medStatement.getDosageFirstRep().getRoute().getCodingFirstRep().getCode())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationStatementDosageDoseAndRateDoseQuantity(final Context context, final Bundle resource) {

    withMedicationStatement(context, resource) { final MedicationStatement medStatement,
                                                 final Map<String, Object> lflvMap,
                                                 final Map<String, Object> medStatementMapping ->
      assumingThat(lflvMap.containsKey(MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_VALUE),
          {
            ->
            assertEquals((lflvMap.get(MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_VALUE) as Number).doubleValue(),
                medStatement.getDosageFirstRep().getDoseAndRateFirstRep().getDoseQuantity().getValue().doubleValue())
            assertEquals(lflvMap.get(MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_CODE),
                medStatement.getDosageFirstRep().getDoseAndRateFirstRep().getDoseQuantity().getCode())
            assertEquals(lflvMap.get(MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_UNIT),
                medStatement.getDosageFirstRep().getDoseAndRateFirstRep().getDoseQuantity().getUnit())
            assertEquals("http://unitsofmeasure.org",
                medStatement.getDosageFirstRep().getDoseAndRateFirstRep().getDoseQuantity().getSystem())
          }
      )
    }
  }

  private void withMedicationStatement(final Context context,
                                       final Bundle resource,
                                       final Closure<Void> assertion) {
    final List<Map<String, Object>> medStatementMappings = laborMappingsByMethodCode(context, LM_MED_STAT_CODE)

    medStatementMappings.each { final Map<String, Object> medStatementMapping ->
      final MedicationStatement medStatement = findMedicationStatement(resource, medStatementMapping[LaborMapping.ID])
      final Map<String, Object> lflvMap = LflvUtils.getLflvMapFromLaborMapping(medStatementMapping, MED_STAT_PROFILE_TYPE)

      assertNotNull(medStatement)
      assertion.call(medStatement, lflvMap, medStatementMapping)
    }
  }

  private static List<Map<String, Object>> laborMappingsByMethodCode(final Context context, final String methodCode) {
    return (context.source[medication().laborMappings()] as List<Map<String, Object>>)
        .findAll { final Map<String, Object> lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == methodCode }
  }

  private static List<MedicationStatement> getMedicationStatements(final Bundle bundle) {
    return bundle.getEntry().collect { it.getResource() }
        .findAll { it instanceof MedicationStatement } as List<MedicationStatement>
  }

  private static MedicationStatement findMedicationStatement(final Bundle bundle, final def laborMappingId) {
    return getMedicationStatements(bundle)
        .find { final MedicationStatement medStatement -> medStatement.getIdElement().getIdPart() == laborMappingId?.toString() }
  }

  private static String firstCatalogCode(final List<Map<String, Object>> entries) {
    return entries?.collect { final Map<String, Object> it -> it[CODE] }?.find()
  }

  private static String dateValue(final Map<String, Object> precisionDateMap) {
    return precisionDateMap?.get(PrecisionDate.DATE)
  }

}
