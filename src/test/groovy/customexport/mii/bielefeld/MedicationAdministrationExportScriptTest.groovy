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
import org.hl7.fhir.r4.model.MedicationAdministration

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assumptions.assumingThat

@TestResources(
    groovyScriptPath = "src/main/groovy/customexport/mii/bielefeld/medicationBundle.groovy",
    contextMapsPath = "src/test/resources/customexport/mii/bielefeld/medication"
)
class MedicationAdministrationExportScriptTest extends AbstractExportScriptTest<Bundle> {

  private static final LM_MED_AD_CODE = "MP_MedicationAdmininstration"

  final String MA_DOSAGE_RATE_RATEQUANTITY_UNIT = "MedicationAdministration.dosage.rate[x]:rateQuantity.unit"
  final String MA_DOSAGE_DOSE_VALUE = "MedicationAdministration.dosage.dose.value"
  final String MA_DOSAGE_DOSE_CODE = "MedicationAdministration.dosage.dose.code"
  final String MA_MEDICATION_MEDICATIONCODEABLECONCEPT_TEXT = "MedicationAdministration.medication[x]:medicationCodeableConcept.text"
  final String MA_EFFECTIVE_EFFECTIVEPERIOD_START = "MedicationAdministration.effective[x]:effectivePeriod.start"
  final String MA_DOSAGE_RATE_RATERATIO_NUMERATOR_CODE = "MedicationAdministration.dosage.rate[x]:rateRatio.numerator.code"
  final String MA_DOSAGE_RATE_RATERATIO_DENOMINATOR_VALUE = "MedicationAdministration.dosage.rate[x]:rateRatio.denominator.value"
  final String MA_DOSAGE_RATE_RATERATIO_NUMERATOR_UNIT = "MedicationAdministration.dosage.rate[x]:rateRatio.numerator.unit"
  final String MA_EFFECTIVE_EFFECTIVEDATETIME = "MedicationAdministration.effective[x]:effectiveDateTime"
  final String MA_DOSAGE_ROUTE = "MedicationAdministration.dosage.route"
  final String MA_DOSAGE_RATE_RATERATIO_NUMERATOR_VALUE = "MedicationAdministration.dosage.rate[x]:rateRatio.numerator.value"
  final String MA_EFFECTIVE_EFFECTIVEPERIOD_END = "MedicationAdministration.effective[x]:effectivePeriod.end"
  final String MA_REASONCODE = "MedicationAdministration.reasonCode"
  final String MA_DOSAGE_TEXT = "MedicationAdministration.dosage.text"
  final String MA_IDENTIFIER = "MedicationAdministration.identifier"
  final String MA_STATUS = "MedicationAdministration.status"
  final String MA_DOSAGE_DOSE_UNIT = "MedicationAdministration.dosage.dose.unit"
  final String MA_DOSAGE_RATE_RATERATIO_DENOMINATOR_UNIT = "MedicationAdministration.dosage.rate[x]:rateRatio.denominator.unit"
  final String MA_CATEGORY = "MedicationAdministration.category"
  final String MA_DOSAGE_RATE_RATEQUANTITY_VALUE = "MedicationAdministration.dosage.rate[x]:rateQuantity.value"
  final String MA_NOTE = "MedicationAdministration.note"


  final Map MED_ADMIN_PROFILE_TYPES = [
      (MA_DOSAGE_RATE_RATEQUANTITY_UNIT)            : LaborFindingLaborValue.STRING_VALUE,
      (MA_DOSAGE_DOSE_VALUE)                        : LaborFindingLaborValue.NUMERIC_VALUE,
      (MA_DOSAGE_DOSE_CODE)                         : LaborFindingLaborValue.STRING_VALUE,
      (MA_MEDICATION_MEDICATIONCODEABLECONCEPT_TEXT): LaborFindingLaborValue.STRING_VALUE,
      (MA_EFFECTIVE_EFFECTIVEPERIOD_START)          : LaborFindingLaborValue.DATE_VALUE,
      (MA_DOSAGE_RATE_RATERATIO_NUMERATOR_CODE)     : LaborFindingLaborValue.STRING_VALUE,
      (MA_DOSAGE_RATE_RATERATIO_DENOMINATOR_VALUE)  : LaborFindingLaborValue.NUMERIC_VALUE,
      (MA_DOSAGE_RATE_RATERATIO_NUMERATOR_UNIT)     : LaborFindingLaborValue.STRING_VALUE,
      (MA_EFFECTIVE_EFFECTIVEDATETIME)              : LaborFindingLaborValue.DATE_VALUE,
      (MA_DOSAGE_ROUTE)                             : LaborFindingLaborValue.STRING_VALUE,
      (MA_DOSAGE_RATE_RATERATIO_NUMERATOR_VALUE)    : LaborFindingLaborValue.NUMERIC_VALUE,
      (MA_EFFECTIVE_EFFECTIVEPERIOD_END)            : LaborFindingLaborValue.DATE_VALUE,
      (MA_REASONCODE)                               : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (MA_DOSAGE_TEXT)                              : LaborFindingLaborValue.STRING_VALUE,
      (MA_IDENTIFIER)                               : LaborFindingLaborValue.STRING_VALUE,
      (MA_STATUS)                                   : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (MA_DOSAGE_DOSE_UNIT)                         : LaborFindingLaborValue.STRING_VALUE,
      (MA_DOSAGE_RATE_RATERATIO_DENOMINATOR_UNIT)   : LaborFindingLaborValue.STRING_VALUE,
      (MA_CATEGORY)                                 : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
      (MA_DOSAGE_RATE_RATEQUANTITY_VALUE)           : LaborFindingLaborValue.NUMERIC_VALUE,
      (MA_NOTE)                                     : LaborFindingLaborValue.STRING_VALUE
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

    final List<Map<String, Object>> medAdminMappings = laborMappingsByMethodCode(context, LM_MED_AD_CODE)
    assertEquals(Bundle.BundleType.COLLECTION, resource.getType())
    assertEquals(medAdminMappings.size(), getMedicationAdministrations(resource).size())
  }

  @ExportScriptTest
  void testMedicationAdministrationSubject(final Context context, final Bundle resource) {
    final String patientRef = "Patient/" + context.source[medication().patientContainer().id()]

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assertEquals(patientRef, medAdmin.getSubject().getReference())
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationId(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assertEquals(("MedicationAdministration/${medAdminMapping[LaborMapping.ID]}").toString(), medAdmin.getId())
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationMedicationCodeableConcept(final Context context, final Bundle resource) {
    final String sourceCode = context.source[medication().code()] as String

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      if (sourceCode.startsWith("ATC_")) {
        assertEquals("http://fhir.de/CodeSystem/ifa/pzn", medAdmin.getMedicationCodeableConcept().getCodingFirstRep().getSystem())
        assertEquals(sourceCode.substring(4), medAdmin.getMedicationCodeableConcept().getCodingFirstRep().getCode())
      } else if (sourceCode.startsWith("PZN_")) {
        assertEquals("http://fhir.de/CodeSystem/bfarm/atc", medAdmin.getMedicationCodeableConcept().getCodingFirstRep().getSystem())
        assertEquals(sourceCode.substring(4), medAdmin.getMedicationCodeableConcept().getCodingFirstRep().getCode())
      } else {
        assertEquals(sourceCode, medAdmin.getMedicationCodeableConcept().getText())
      }
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationIdentifier(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assumingThat(lflvMap.containsKey(MA_IDENTIFIER),
          { -> assertEquals(lflvMap.get(MA_IDENTIFIER), medAdmin.getIdentifierFirstRep().getValue()) }
      )
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationStatus(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assumingThat(lflvMap.containsKey(MA_STATUS),
          {
            -> assertEquals(firstCatalogCode(lflvMap.get(MA_STATUS) as List<Map<String, Object>>), medAdmin.getStatus().toCode())
          }
      )

      assumingThat(!lflvMap.containsKey(MA_STATUS),
          { -> assertEquals(MedicationAdministration.MedicationAdministrationStatus.UNKNOWN.toCode(), medAdmin.getStatus().toCode()) }
      )
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationCategory(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assumingThat(lflvMap.containsKey(MA_CATEGORY),
          {
            ->
              assertEquals("http://terminology.hl7.org/CodeSystem/medication-statement-category",
                  medAdmin.getCategory().getCodingFirstRep().getSystem())
              assertEquals(firstCatalogCode(lflvMap.get(MA_CATEGORY) as List<Map<String, Object>>),
                  medAdmin.getCategory().getCodingFirstRep().getCode())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationNote(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assumingThat(lflvMap.containsKey(MA_NOTE),
          { -> assertEquals(lflvMap.get(MA_NOTE), medAdmin.getNoteFirstRep().getText()) }
      )
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationReasonCode(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assumingThat(lflvMap.containsKey(MA_REASONCODE),
          {
            ->
            assertEquals(firstCatalogCode(lflvMap.get(MA_REASONCODE) as List<Map<String, Object>>),
                medAdmin.getReasonCodeFirstRep().getCodingFirstRep().getCode())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationDosageText(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assumingThat(lflvMap.containsKey(MA_DOSAGE_TEXT),
          { -> assertEquals(lflvMap.get(MA_DOSAGE_TEXT), medAdmin.getDosage().getText()) }
      )
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationDosageRoute(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assumingThat(lflvMap.containsKey(MA_DOSAGE_ROUTE),
          {
            ->
              assertEquals("http://snomed.info/sct", medAdmin.getDosage().getRoute().getCodingFirstRep().getSystem())
              assertEquals(lflvMap.get(MA_DOSAGE_ROUTE), medAdmin.getDosage().getRoute().getCodingFirstRep().getCode())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationDosageDose(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assumingThat(lflvMap.containsKey(MA_DOSAGE_DOSE_VALUE),
          {
            ->
              assertEquals((lflvMap.get(MA_DOSAGE_DOSE_VALUE) as Number).doubleValue(),
                  medAdmin.getDosage().getDose().getValue().doubleValue())
              assertEquals(lflvMap.get(MA_DOSAGE_DOSE_UNIT), medAdmin.getDosage().getDose().getCode())
              assertEquals("http://unitsofmeasure.org", medAdmin.getDosage().getDose().getSystem())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationDosageRateRatio(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assumingThat(lflvMap.containsKey(MA_DOSAGE_RATE_RATERATIO_NUMERATOR_VALUE),
          {
            ->
              assertEquals((lflvMap.get(MA_DOSAGE_RATE_RATERATIO_NUMERATOR_VALUE) as Number).doubleValue(),
                  medAdmin.getDosage().getRateRatio().getNumerator().getValue().doubleValue())
              assertEquals(lflvMap.get(MA_DOSAGE_RATE_RATERATIO_NUMERATOR_UNIT),
                  medAdmin.getDosage().getRateRatio().getNumerator().getCode())
              assertEquals("http://unitsofmeasure.org", medAdmin.getDosage().getRateRatio().getNumerator().getSystem())
          }
      )

      assumingThat(lflvMap.containsKey(MA_DOSAGE_RATE_RATERATIO_DENOMINATOR_VALUE),
          {
            ->
            assertEquals((lflvMap.get(MA_DOSAGE_RATE_RATERATIO_DENOMINATOR_VALUE) as Number).doubleValue(),
                medAdmin.getDosage().getRateRatio().getDenominator().getValue().doubleValue())
            assertEquals(lflvMap.get(MA_DOSAGE_RATE_RATERATIO_DENOMINATOR_UNIT),
                medAdmin.getDosage().getRateRatio().getDenominator().getCode())
            assertEquals("http://unitsofmeasure.org", medAdmin.getDosage().getRateRatio().getDenominator().getSystem())
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationDosageRateQuantity(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assumingThat(lflvMap.containsKey(MA_DOSAGE_RATE_RATEQUANTITY_VALUE),
          {
            ->
              assumingThat(!lflvMap.containsKey(MA_DOSAGE_RATE_RATERATIO_NUMERATOR_VALUE)
                  && !lflvMap.containsKey(MA_DOSAGE_RATE_RATERATIO_DENOMINATOR_VALUE),
                  {
                    ->
              assertEquals((lflvMap.get(MA_DOSAGE_RATE_RATEQUANTITY_VALUE) as Number).doubleValue(),
                  medAdmin.getDosage().getRateQuantity().getValue().doubleValue())
              assertEquals(lflvMap.get(MA_DOSAGE_RATE_RATEQUANTITY_UNIT), medAdmin.getDosage().getRateQuantity().getCode())
              assertEquals("http://unitsofmeasure.org", medAdmin.getDosage().getRateQuantity().getSystem())
                  }
              )
          }
      )
    }
  }

  @ExportScriptTest
  void testMedicationAdministrationEffective(final Context context, final Bundle resource) {

    withMedicationAdministration(context, resource) { final MedicationAdministration medAdmin,
                                                      final Map<String, Object> lflvMap,
                                                      final Map<String, Object> medAdminMapping ->
      assumingThat(lflvMap.containsKey(MA_EFFECTIVE_EFFECTIVEDATETIME),
          {
            ->
            assertEquals(dateValue(lflvMap.get(MA_EFFECTIVE_EFFECTIVEDATETIME) as Map<String, Object>),
                medAdmin.getEffectiveDateTimeType().getValueAsString())
          }
      )

      assumingThat(!lflvMap.containsKey(MA_EFFECTIVE_EFFECTIVEDATETIME)
          && (lflvMap.containsKey(MA_EFFECTIVE_EFFECTIVEPERIOD_START) || lflvMap.containsKey(MA_EFFECTIVE_EFFECTIVEPERIOD_END)),
          {
            ->
            assertEquals(dateValue(lflvMap.get(MA_EFFECTIVE_EFFECTIVEPERIOD_START) as Map<String, Object>),
                medAdmin.getEffectivePeriod().getStartElement().getValueAsString())
            assertEquals(dateValue(lflvMap.get(MA_EFFECTIVE_EFFECTIVEPERIOD_END) as Map<String, Object>),
                medAdmin.getEffectivePeriod().getEndElement().getValueAsString())
          }
      )
    }
  }

  private void withMedicationAdministration(final Context context,
                                            final Bundle resource,
                                            final Closure<Void> assertion) {
    final List<Map<String, Object>> medAdminMappings = laborMappingsByMethodCode(context, LM_MED_AD_CODE)

    medAdminMappings.each { final Map<String, Object> medAdminMapping ->
      final MedicationAdministration medAdmin = findMedicationAdministration(resource, medAdminMapping[LaborMapping.ID])
      final Map<String, Object> lflvMap = LflvUtils.getLflvMapFromLaborMapping(medAdminMapping, MED_ADMIN_PROFILE_TYPES)

      assertNotNull(medAdmin)
      assertion.call(medAdmin, lflvMap, medAdminMapping)
    }
  }

  private static List<Map<String, Object>> laborMappingsByMethodCode(final Context context, final String methodCode) {
    return (context.source[medication().laborMappings()] as List<Map<String, Object>>)
        .findAll { final Map<String, Object> lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == methodCode }
  }

  private static List<MedicationAdministration> getMedicationAdministrations(final Bundle bundle) {
    return bundle.getEntry().collect { it.getResource() }
        .findAll { it instanceof MedicationAdministration } as List<MedicationAdministration>
  }

  private static MedicationAdministration findMedicationAdministration(final Bundle bundle, final def laborMappingId) {
    return getMedicationAdministrations(bundle)
        .find { final MedicationAdministration medAdmin -> medAdmin.getIdElement().getIdPart() == laborMappingId?.toString() }
  }

  private static String firstCatalogCode(final List<Map<String, Object>> entries) {
    return entries?.collect { final Map<String, Object> it -> it[CODE] }?.find()
  }

  private static String dateValue(final Map<String, Object> precisionDateMap) {
    return precisionDateMap?.get(PrecisionDate.DATE)
  }


}
