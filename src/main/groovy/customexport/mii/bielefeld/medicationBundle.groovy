package customexport.mii.bielefeld

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MedicationAdministration
import org.hl7.fhir.r4.model.MedicationStatement

import javax.annotation.Nullable

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

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

bundle {

  type = Bundle.BundleType.COLLECTION

  final List<String> adminRefs = []


  String medSystem = null

  final String sourceCode = context.source[medication().code()]
  String medCode = null

  if (sourceCode.startsWith("ATC_")) {
    medCode = sourceCode.substring(4)
    medSystem = "http://fhir.de/CodeSystem/ifa/pzn"
  } else if (sourceCode.startsWith("PZN_")) {
    medCode = sourceCode.substring(4)
    medSystem = "http://fhir.de/CodeSystem/bfarm/atc"
  }


  context.source[medication().laborMappings()]
      .findAll { final def lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == "MP_MedicationAdmininstration" }
      .each { final def medAdLm ->
        entry {
          resource {
            medicationAdministration {

              meta {
                profile("https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/MedicationAdministration")
              }
              final String adminRef = "MedicationAdministration/" + medAdLm[LaborMapping.ID]
              adminRefs.add(adminRef)

              id = adminRef

              final lflvMap = getLflvMap(medAdLm, MED_ADMIN_PROFILE_TYPES)

              if (lflvMap.containsKey(MA_IDENTIFIER)) {
                identifier {
                  value = lflvMap.get(MA_IDENTIFIER)
                }
              }

              if (lflvMap.containsKey(MA_STATUS)) {
                status = getCatalogEntryValue(lflvMap.get(MA_STATUS) as List)
              } else {
                status = MedicationAdministration.MedicationAdministrationStatus.UNKNOWN.toCode()
              }

              if (lflvMap.containsKey(MA_CATEGORY)) {
                category {
                  coding {
                    system = "http://terminology.hl7.org/CodeSystem/medication-statement-category"
                    code = getCatalogEntryValue(lflvMap.get(MA_CATEGORY) as List)
                  }
                }
              }

              medicationCodeableConcept {
                if (medSystem != null) {
                  coding {
                    system = medSystem
                    code = medCode
                  }
                } else {
                  text = sourceCode as String
                }
              }

              subject {
                reference = "Patient/" + context.source[medication().patientContainer().id()]
              }

              if (lflvMap.containsKey(MA_EFFECTIVE_EFFECTIVEDATETIME)) {
                effectiveDateTime = getDate(lflvMap.get(MA_EFFECTIVE_EFFECTIVEDATETIME) as Map)
              } else if (lflvMap.containsKey(MA_EFFECTIVE_EFFECTIVEPERIOD_START) ||
                  lflvMap.containsKey(MA_EFFECTIVE_EFFECTIVEPERIOD_END)) {
                effectivePeriod {
                  start = getDate(lflvMap.get(MA_EFFECTIVE_EFFECTIVEPERIOD_START) as Map)
                  end = getDate(lflvMap.get(MA_EFFECTIVE_EFFECTIVEPERIOD_END) as Map)
                }
              }

              if (lflvMap.containsKey(MA_REASONCODE)) {
                reasonCode {
                  coding {
                    code = getCatalogEntryValue(lflvMap.get(MA_REASONCODE) as List)
                  }
                }
              }

              if (lflvMap.containsKey(MA_NOTE)) {
                note {
                  text = lflvMap.get(MA_NOTE) as String
                }
              }

              dosage {
                if (lflvMap.containsKey(MA_DOSAGE_TEXT)) {
                  text = lflvMap.get(MA_DOSAGE_TEXT) as String
                }

                if (lflvMap.containsKey(MA_DOSAGE_ROUTE)) {
                  route {
                    coding {
                      system = "http://snomed.info/sct"
                      code = lflvMap.get(MA_DOSAGE_ROUTE)
                    }
                  }
                }

                if (lflvMap.containsKey(MA_DOSAGE_DOSE_VALUE)) {
                  dose {
                    value = lflvMap.get(MA_DOSAGE_DOSE_VALUE)
                    code = lflvMap.get(MA_DOSAGE_DOSE_UNIT)
                    system = "http://unitsofmeasure.org"
                  }
                }

                rateRatio {
                  if (lflvMap.containsKey(MA_DOSAGE_RATE_RATERATIO_NUMERATOR_VALUE)) {
                    numerator {
                      value = lflvMap.get(MA_DOSAGE_RATE_RATERATIO_NUMERATOR_VALUE)
                      code = lflvMap.get(MA_DOSAGE_RATE_RATERATIO_NUMERATOR_UNIT)
                      system = "http://unitsofmeasure.org"
                    }
                  }
                  if (lflvMap.containsKey(MA_DOSAGE_RATE_RATERATIO_DENOMINATOR_VALUE)) {
                    denominator {
                      value = lflvMap.get(MA_DOSAGE_RATE_RATERATIO_DENOMINATOR_VALUE)
                      code = lflvMap.get(MA_DOSAGE_RATE_RATERATIO_DENOMINATOR_UNIT)
                      system = "http://unitsofmeasure.org"
                    }
                  }
                }
                if (lflvMap.containsKey(MA_DOSAGE_RATE_RATEQUANTITY_VALUE)) {
                  rateQuantity {
                    value = lflvMap.get(MA_DOSAGE_RATE_RATEQUANTITY_VALUE)
                    code = lflvMap.get(MA_DOSAGE_RATE_RATEQUANTITY_UNIT)
                    system = "http://unitsofmeasure.org"
                  }
                }
              }

            }
          }
        }
      }

  context.source[medication().laborMappings()]
      .findAll { final def lm -> lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][CODE] == "MP_MedicationStatement" }
      .each { final def medStatLm ->
        entry {
          resource {
            medicationStatement {

              id = "MedicationStatement/" + medStatLm[LaborMapping.ID]

              meta {
                profile("https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/MedicationStatement")
              }

              final lflvMap = getLflvMap(medStatLm, MED_STAT_PROFILE_TYPE)

              if (lflvMap.containsKey(MS_IDENTIFIER)) {
                identifier {
                  value = lflvMap.get(MS_IDENTIFIER)
                }
              }

              adminRefs.each { final def adminRef ->
                partOf {
                  reference = adminRef
                }
              }

              if (lflvMap.containsKey(MS_STATUS)) {
                status = getCatalogEntryValue(lflvMap.get(MS_STATUS) as List)
              } else {
                status = MedicationStatement.MedicationStatementStatus.UNKNOWN.toCode()
              }

              if (lflvMap.containsKey(MS_CATEGORY)) {
                category {
                  coding {
                    system = "http://terminology.hl7.org/CodeSystem/medication-statement-category"
                    code = getCatalogEntryValue(lflvMap.get(MS_CATEGORY) as List)
                  }
                }
              }

              medicationCodeableConcept {
                if (medSystem != null) {
                  coding {
                    system = medSystem
                    code = medCode
                  }
                } else {
                  text = sourceCode
                }
              }

              subject {
                reference = "Patient/" + context.source[medication().patientContainer().id()]
              }

              if (lflvMap.containsKey(MS_EFFECTIVE_EFFECTIVEDATETIME)) {
                effectiveDateTime = getDate(lflvMap.get(MS_EFFECTIVE_EFFECTIVEDATETIME) as Map)
              } else if (lflvMap.containsKey(MS_EFFECTIVE_EFFECTIVEPERIOD_START) ||
                  lflvMap.containsKey(MS_EFFECTIVE_EFFECTIVEPERIOD_END)) {
                effectivePeriod {
                  start = getDate(lflvMap.get(MS_EFFECTIVE_EFFECTIVEPERIOD_START) as Map)
                  end = getDate(lflvMap.get(MS_EFFECTIVE_EFFECTIVEPERIOD_END) as Map)
                }
              }

              if (medStatLm[LaborMapping.EPISODE] != null) {
                context_ {
                  reference = "Encounter/" + medStatLm[LaborMapping.EPISODE]
                }
              }

              if (lflvMap.containsKey(MS_DATEASSERTED)) {
                dateAsserted = getDate(lflvMap.get(MS_DATEASSERTED) as Map)
              }

              if (lflvMap.containsKey(MS_REASONCODE)) {
                reasonCode {
                  coding {
                    system = "http://snomed.info/sct"
                    code = lflvMap.get(MS_REASONCODE)
                  }
                }
              }

              if (lflvMap.containsKey(MS_NOTE)) {
                note {
                  text = lflvMap.get(MS_NOTE) as String
                }
              }

              dosage {
                sequence = lflvMap.get(MS_DOSAGE_SEQUENCE) as Integer
                text = lflvMap.get(MS_DOSAGE_TEXT) as String

                timing {

                  event(getDate(lflvMap.get(MS_DOSAGE_TIMING_EVENT) as Map))

                  repeat {
                    if (lflvMap.containsKey(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSDURATION_VALUE)) {
                      boundsDuration {
                        value = lflvMap.get(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSDURATION_VALUE)
                        code = lflvMap.get(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSDURATION_UNIT) as String
                        system = "http://unitsofmeasure.org"
                      }
                    } else {
                      boundsPeriod {
                        start = getDate(lflvMap.get(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSPERIOD_START) as Map)
                        end = getDate(lflvMap.get(MS_DOSAGE_TIMING_REPEAT_BOUNDS_BOUNDSPERIOD_END) as Map)
                      }
                    }
                    count = lflvMap.get(MS_DOSAGE_TIMING_REPEAT_COUNT) as Integer
                    countMax = lflvMap.get(MS_DOSAGE_TIMING_REPEAT_COUNT) as Integer
                  }

                  asNeededBoolean = lflvMap.get(MS_DOSAGE_ASNEEDED)

                  site {
                    if (lflvMap.containsKey(MS_DOSAGE_SITE_CODING)) {
                      coding {
                        system = "http://snomed.info/sct"
                        code = lflvMap.get(MS_DOSAGE_SITE_CODING) as String
                      }
                    }
                    text = lflvMap.get(MS_DOSAGE_SITE_TEXT) as String
                  }

                  if (lflvMap.containsKey(MS_DOSAGE_ROUTE)) {
                    route {
                      coding {
                        system = "http://snomed.info/sct"
                        code = lflvMap.get(MS_DOSAGE_ROUTE) as String
                      }
                    }
                  }

                  doseAndRate {
                    if (lflvMap.containsKey(MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_VALUE)) {
                      doseQuantity {
                        value = lflvMap.get(MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_VALUE)
                        code = lflvMap.get(MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_CODE) as String
                        unit = lflvMap.get(MS_DOSAGE_DOSEANDRATE_DOSE_DOSEQUANTITY_UNIT) as String
                        system = "http://unitsofmeasure.org"
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
}

static Map<String, Object> getLflvMap(final def mapping, final Map<String, String> types) {
  final Map<String, Object> lflvMap = [:]
  if (!mapping) {
    return lflvMap
  }

  types.each { final String lvCode, final String lvType ->
    final def lflvForLv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == lvCode
    }

    if (lflvForLv && lflvForLv[lvType]) {
      lflvMap[(lvCode)] = lflvForLv[lvType]
    }
  }
  return lflvMap
}

static String getCatalogEntryValue(final List<Map<String, Object>> entries) {
  return entries.collect { final def entry -> entry[CODE] }
      .find()
}

@Nullable
static String getDate(final Map<String, Object> dateMap) {
  return dateMap != null ? dateMap[PrecisionDate.DATE] : null
}


