package customexport.izi.hannover.hdrp4

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.ValueReference
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.AbstractSample.ID_CONTAINER
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding

/**
 * Represented by a HDRP LaborFinding
 * @author Jonas Küttner
 * @since kairos-fhir-dsl.v.1.12.0, HDRP.v.3.18.1.19, HDRP.v.3.18.2
 * @since v.2024.5.8, v.2025.1.0 (requires changes develeoped in CENTRAXX-21618, CENTRAXX-21615)
 *
 */

observation {

  final String laborMethodCode = context.source[laborFinding().laborMethod().code()]
  final def isIziRelevant = ["ITEM_KLINISCHE_DATEN", "CIMD_ABWEICHUNGEN"].contains(laborMethodCode)
  if (!(isIziRelevant)) {
    return
  }

  id = "Observation/LaborFinding-" + context.source[laborFinding().id()]

  meta {
    tag {
      system = FhirUrls.System.CXX_ENTITY
      valueString = "LaborFinding"
    }
  }

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      system = "urn:centraxx"
      code = context.source[laborFinding().shortName()] as String
    }
  }

  effectiveDateTime {
    if ("UNKNOWN" == context.source[laborFinding().findingDate().precision()]) {
      extension {
        url = FhirUrls.Extension.FhirDefaults.DATA_ABSENT_REASON
        valueCode = "unknown"
      }
    } else {
      date = context.source[laborFinding().findingDate().date()]
    }
  }

  // add all the mappings
  context.source[laborFinding().laborMappings()]
      .findAll { final def lm ->
        [LaborMappingType.PATIENTLABORMAPPING, LaborMappingType.SAMPLELABORMAPPING].contains(lm[LaborMapping.MAPPING_TYPE] as LaborMappingType)
      }
      .each { final def lm ->

        final def patIdContainer = lm[LaborMapping.RELATED_PATIENT][PatientContainer.ID_CONTAINER]?.find {
          "SID" == it[ID_CONTAINER_TYPE]?.getAt(CODE)
        }

        extension {
          url = FhirUrls.Extension.LABOR_MAPPING
          extension {
            url = FhirUrls.Extension.LaborMapping.LABOR_MAPPING_TYPE
            valueString = lm[LaborMapping.MAPPING_TYPE] as String
          }
          extension {
            url = FhirUrls.Extension.LaborMapping.PATIENT
            valueReference {
              identifier {
                value = patIdContainer[PSN] as String
                type {
                  coding {
                    system = FhirUrls.System.IdContainerType.BASE_URL
                    code = patIdContainer[ID_CONTAINER_TYPE]?.getAt(CODE) as String
                  }
                }
              }
            }
          }

          extension {
            url = FhirUrls.Extension.LaborMapping.RELATED_REFERENCE
            if (lm[LaborMapping.MAPPING_TYPE] as LaborMappingType == LaborMappingType.SAMPLELABORMAPPING) {

              final def sampleIdc = lm[LaborMapping.SAMPLE][ID_CONTAINER]?.find {
                "SAMPLEID" == it[ID_CONTAINER_TYPE]?.getAt(CODE)
              }

              if (sampleIdc) {
                valueReference {
                  identifier {
                    value = sampleIdc[PSN] as String
                    type {
                      coding {
                        system = FhirUrls.System.IdContainerType.BASE_URL
                        code = sampleIdc[ID_CONTAINER_TYPE]?.getAt(CODE) as String
                      }
                    }
                  }
                }
              }
            } else {
              if (patIdContainer) {
                extension {
                  url = FhirUrls.Extension.LaborMapping.PATIENT
                  valueReference {
                    identifier {
                      value = patIdContainer[PSN] as String
                      type {
                        coding {
                          system = FhirUrls.System.IdContainerType.BASE_URL
                          code = patIdContainer[ID_CONTAINER_TYPE]?.getAt(CODE) as String
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

  method {
    coding {
      system = "urn:centraxx"
      version = context.source[laborFinding().laborMethod().version()]
      code = laborMethodCode == "ITEM_KLINISCHE_DATEN" ? "CIMD_KERNZUSATZDATEN" : "CIMD_ABWEICHUNGEN"
    }
  }

  context.source[laborFinding().laborFindingLaborValues()].each { final lflv ->

    final def laborValue = lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE]

    final String laborValueCode = laborValue?.getAt(CODE) as String
    if (isIziRelevantLaborValue(laborValueCode)) {
      component {
        code {
          coding {
            system = "urn:centraxx"
            code = mapLocalToCentralLabValueCode(laborValueCode)
          }
          laborValue?.getAt(LaborValue.IDCONTAINERS)?.each { final idContainer ->
            coding {
              system = idContainer[ID_CONTAINER_TYPE]?.getAt(CODE)
              code = idContainer[PSN] as String
            }
          }
        }

        if (isNumeric(laborValue)) {
          valueQuantity {
            value = sanitizeScale(lflv[LaborFindingLaborValue.NUMERIC_VALUE])
            unit = laborValue?.getAt(LaborValueNumeric.UNIT)?.getAt(CODE) as String
          }
        } else if (isBoolean(laborValue)) {
          valueBoolean(lflv[LaborFindingLaborValue.BOOLEAN_VALUE] as Boolean)
        } else if (isDate(laborValue)) {
          valueDateTime {
            if ("UNKNOWN" == lflv[LaborFindingLaborValue.DATE_VALUE]?.getAt(PrecisionDate.PRECISION)) {
              extension {
                url = FhirUrls.Extension.FhirDefaults.DATA_ABSENT_REASON
                valueCode = "unknown"
              }
            } else {
              date = lflv[LaborFindingLaborValue.DATE_VALUE]?.getAt(PrecisionDate.DATE)
            }
          }
        } else if (isTime(laborValue)) {
          valueTime(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
        } else if (isString(laborValue)) {
          valueString(lflv[LaborFindingLaborValue.STRING_VALUE] as String)
        } else if (isEnumeration(laborValue)) {
          valueCodeableConcept {
            lflv[LaborFindingLaborValue.MULTI_VALUE].each { final entry ->
              coding {
                system = "urn:centraxx:CodeSystem/UsageEntry"
                code = entry[CODE] as String
              }
            }
            lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
              coding {
                final String catalogCode = entry[CatalogEntry.CATALOG]?.getAt(CODE)
                final String catalogKind = catalogCode == "CIMD_ABWEICHUNGEN" ? "Catalog" : "ValueList"
                system = "urn:centraxx:CodeSystem/" + catalogKind + "#c." + catalogCode
                code = entry[CODE] as String
              }
            }
          }
        } else if (isOptionGroup(laborValue)) {
          valueCodeableConcept {
            lflv[LaborFindingLaborValue.MULTI_VALUE].each { final entry ->
              coding {
                system = "urn:centraxx:CodeSystem/UsageEntry"
                code = entry[CODE] as String
              }
            }
            lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
              coding {
                final String catalogCode = entry[CatalogEntry.CATALOG]?.getAt(CODE)
                final String catalogKind = catalogCode == "CIMD_ABWEICHUNGEN" ? "Catalog" : "ValueList"
                system = "urn:centraxx:CodeSystem/" + catalogKind + "#c." + catalogCode
                code = entry[CODE] as String
              }
            }
          }
        } else if (isCatalog(laborValue)) {
          valueCodeableConcept {
            lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
              coding {
                final String catalogCode = entry[CatalogEntry.CATALOG]?.getAt(CODE)
                final String catalogKind = catalogCode == "CIMD_ABWEICHUNGEN" ? "Catalog" : "ValueList"
                system = "urn:centraxx:CodeSystem/" + catalogKind + "#c." + catalogCode
                code = entry[CODE] as String
              }
            }
            lflv[LaborFindingLaborValue.ICD_ENTRY_VALUE].each { final entry ->
              coding {
                system = "urn:centraxx:CodeSystem/IcdCatalog" // uses always the last ICD 10 catalog version in the target system
                code = entry[CODE] as String
              }
            }
            // example for master data catalog entries of blood group
            lflv[LaborFindingLaborValue.MULTI_VALUE_REFERENCES].each { final entry ->
              final def bloodGroup = entry[ValueReference.BLOOD_GROUP_VALUE]
              if (bloodGroup != null) {
                coding {
                  system = FhirUrls.System.Patient.BloodGroup.BASE_URL
                  code = bloodGroup?.getAt(CODE) as String
                }
              }

              // example for master data catalog entries of attending doctor
              final def attendingDoctor = entry[ValueReference.ATTENDING_DOCTOR_VALUE]
              if (attendingDoctor != null) {
                coding {
                  system = FhirUrls.System.AttendingDoctor.BASE_URL
                  // HDRP uses the reference embedded in a coding to support multi selects
                  code = "Practitioner/" + attendingDoctor?.getAt(AbstractCatalog.ID) as String
                }
              }
            }
          }
        } else {
          final String msg = laborValue?.getAt(LaborValue.D_TYPE) + " not implemented yet."
          System.out.println(msg)
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

static boolean isIziRelevantLaborValue(final String laborValueCode) {
  return ["ITEM_VISITENDATUM",
          "ITEM_RAUCHERSTATUS",
          "ITEM_BMI",
          "ITEM_PACKYEARS",
          "ITEM_RAUCHER_SEIT",
          "ITEM_EINSCHLUSSDIAGNOSE",
          "ITEM_AUFGEHOERT_AB",
          "NUECHTERNSTATUS_HUB",
          "ITEM_DIAGNOSE_BEGLEITERKRANKUNG",
          "CIMD_AKTUELLE_MEDIKATION",
          "ITEM_AKTUELLE_MEDIKATION",
          "CIMD_AKTUELLE_MEDIKATION_WIRKSTOFFKLASSEN_ATC",
          "CIMD_MEDIKATION_FREITEXTFELD",
          "ITEM_POSITION_BEI_BLUTENTNAHME",
          "ITEM_STAUBINDE_UNMITTELBAR_NACH_BLUTEINFLUSS_GELOEST",
          "CIMD_ABWEICHUNGEN"].contains(laborValueCode)
}

static String mapLocalToCentralLabValueCode(final String localLaborValueCode) {
  if (localLaborValueCode == null) {
    return null
  }

  return localLaborValueCode.equals("ITEM_AKTUELLE_MEDIKATION") ? "CIMD_AKTUELLE_MEDIKATION" : localLaborValueCode
}

static Object sanitizeScale(final Object numeric) {
  return numeric == null ? null : new BigDecimal(numeric.toString()).stripTrailingZeros()
}
