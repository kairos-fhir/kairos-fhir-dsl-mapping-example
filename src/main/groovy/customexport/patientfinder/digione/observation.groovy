package customexport.patientfinder.digione

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.OpsEntry
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a HDRP LaborMapping
 * @author Mike Wähnert
 * @since HDRP v.2025.1.0, v.2024.5.2
 * The first code of each component represents the LaborValue.Code in HDRP. Further codes could be representations in LOINC, SNOMED-CT etc.
 * LaborValueIdContainer in HDRP are just an export example, but not intended to be imported by HDRP FHIR API yet.
 */

observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] == "BIOMARKERS") {
    return
  }

  id = "Observation/GeneralObs-" + context.source[laborMapping().laborFinding().id()]

  status = Observation.ObservationStatus.UNKNOWN

  identifier {
    value = context.source[laborMapping().laborFinding().laborFindingId()]
  }

  code {
    coding {
      code = context.source[laborMapping().laborFinding().shortName()] as String
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  if (context.source[laborMapping().mappingType()] as LaborMappingType == LaborMappingType.SAMPLELABORMAPPING) {
    specimen {
      reference = "Specimen/" + context.source[laborMapping().relatedOid()]
    }
  }

  method {
    coding {
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
      display = context.source[laborMapping().laborFinding().laborMethod().multilinguals()].find { final def ml ->
        ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
      }?.getAt(Multilingual.SHORT_NAME)
    }
  }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->

    final def laborValue = lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE] // from HDRP.v.2022.3.0

    final String laborValueCode = laborValue?.getAt(CODE) as String

    final String laborValueDisplay = laborValue?.getAt(LaborValue.MULTILINGUALS)
        ?.find { final mle -> mle[Multilingual.LANGUAGE] == "en" && mle[Multilingual.SHORT_NAME] != null }
        ?.getAt(Multilingual.SHORT_NAME) as String

    component {
      code {
        coding {
          code = laborValueCode
          display = laborValueDisplay
        }
      }

      if (isNumeric(laborValue)) {
        valueQuantity {
          value = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
          unit = laborValue?.getAt(LaborValueNumeric.UNIT)?.getAt(CODE) as String
        }
      } else if (isBoolean(laborValue)) {
        valueBoolean(lflv[LaborFindingLaborValue.BOOLEAN_VALUE] as Boolean)
      } else if (isDate(laborValue)) {
        valueDateTime {
          date = lflv[LaborFindingLaborValue.DATE_VALUE]?.getAt(PrecisionDate.DATE)
        }
      } else if (isTime(laborValue)) {
        valueTime(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
      } else if (isString(laborValue)) {
        valueString(lflv[LaborFindingLaborValue.STRING_VALUE] as String)
      } else if (isEnumeration(laborValue)) {
        valueCodeableConcept {
          lflv[LaborFindingLaborValue.MULTI_VALUE].each { final entry ->
            coding {
              code = entry[CODE] as String
              display = entry[UsageEntry.MULTILINGUALS].find { final def ml ->
                ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
              }?.getAt(Multilingual.SHORT_NAME)
            }
          }
          lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
            coding {
              code = entry[CODE] as String
              display = entry[CatalogEntry.MULTILINGUALS].find { final def ml ->
                ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
              }?.getAt(Multilingual.SHORT_NAME)
            }
          }
        }
      } else if (isOptionGroup(laborValue)) {
        valueCodeableConcept {
          lflv[LaborFindingLaborValue.MULTI_VALUE].each { final entry ->
            coding {
              code = entry[CODE] as String
              display = entry[UsageEntry.MULTILINGUALS].find { final def ml ->
                ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
              }?.getAt(Multilingual.SHORT_NAME)
            }
          }
          lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
            coding {
              code = entry[CODE] as String
              display = entry[CatalogEntry.MULTILINGUALS].find { final def ml ->
                ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
              }?.getAt(Multilingual.SHORT_NAME)
            }
          }
        }
      } else if (isCatalog(laborValue)) {
        valueCodeableConcept {
          lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
            coding {
              code = entry[CODE] as String
              display = entry[CatalogEntry.MULTILINGUALS].find { final def ml ->
                ml[Multilingual.SHORT_NAME] != null && ml[Multilingual.LANGUAGE] == "en"
              }?.getAt(Multilingual.SHORT_NAME)
            }
          }
          lflv[LaborFindingLaborValue.ICD_ENTRY_VALUE].each { final entry ->
            coding {
              code = entry[CODE] as String
              display = entry[IcdEntry.PREFERRED] as String
            }
          }
          lflv[LaborFindingLaborValue.OPS_ENTRY_VALUE].each { final entry ->
            coding {
              code = entry[CODE] as String
              display = entry[OpsEntry.PREFERRED] as String
            }
          }
        }
      }
    }
  }
}

static boolean isDTypeOf(final Object laborValue, final List<LaborValueDType> types) {
  return types.contains(laborValue?.getAt(LaborValue.D_TYPE) as LaborValueDType)
}

static boolean isBoolean(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.BOOLEAN])
}

static boolean isNumeric(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.INTEGER, LaborValueDType.DECIMAL, LaborValueDType.SLIDER])
}

static boolean isDate(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.DATE, LaborValueDType.LONGDATE])
}

static boolean isTime(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.TIME])
}

static boolean isEnumeration(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.ENUMERATION])
}

static boolean isString(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.STRING, LaborValueDType.LONGSTRING])
}

static boolean isCatalog(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.CATALOG])
}

static boolean isOptionGroup(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.OPTIONGROUP])
}


