package projects.izi.hannover

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.ValueReference
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Mike Wähnert
 * @since kairos-fhir-dsl.v.1.12.0, CXX.v.3.18.1.19, CXX.v.3.18.2
 * The first code of each component represents the LaborValue.Code in CXX. Further codes could be representations in LOINC, SNOMED-CT etc.
 * LaborValueIdContainer in CXX are just an export example, but not intended to be imported by CXX FHIR API yet.
 */
observation {

  final def isIziRelevant = "ITEM_KLINISCHE_DATEN" == context.source[laborMapping().laborFinding().laborMethod().code()]
  if (!(isIziRelevant)) {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      system = "urn:centraxx"
      code = context.source[laborMapping().laborFinding().shortName()] as String
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  final def patIdContainer = context.source[laborMapping().relatedPatient().idContainer()]?.find {
    "SID" == it[ID_CONTAINER_TYPE]?.getAt(CODE)
  }

  if (patIdContainer) {
    subject {
      identifier {
        value = patIdContainer[PSN]
        type {
          coding {
            system = "urn:centraxx"
            code = patIdContainer[ID_CONTAINER_TYPE]?.getAt(CODE) as String
          }
        }
      }
    }
  }

  method {
    coding {
      system = "urn:centraxx"
      version = context.source[laborMapping().laborFinding().laborMethod().version()]
      code = "CIMD_KERNZUSATZDATEN"
    }
  }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->

    final def laborValue = lflv[LaborFindingLaborValue.LABOR_VALUE] != null
        ? lflv[LaborFindingLaborValue.LABOR_VALUE] // before CXX.v.2022.3.0
        : lflv["crfTemplateField"][CrfTemplateField.LABOR_VALUE] // from CXX.v.2022.3.0

    final String laborValueCode = laborValue?.getAt(CODE) as String
    if (isCimdKenzusatzdaten(laborValueCode)) {
      component {
        code {
          coding {
            system = "urn:centraxx"
            code = laborValueCode
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
                system = "urn:centraxx:CodeSystem/UsageEntry"
                code = entry[CODE] as String
              }
            }
            lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
              coding {
                system = "urn:centraxx:CodeSystem/ValueList-" + entry[CatalogEntry.CATALOG]?.getAt(AbstractCatalog.ID)
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
                system = "urn:centraxx:CodeSystem/ValueList-" + entry[CatalogEntry.CATALOG]?.getAt(AbstractCatalog.ID)
                code = entry[CODE] as String
              }
            }
          }
        } else if (isCatalog(laborValue)) {
          valueCodeableConcept {
            lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
              coding {
                system = "urn:centraxx:CodeSystem/ValueList-" + entry[CatalogEntry.CATALOG]?.getAt(AbstractCatalog.ID)
                code = entry[CODE] as String
              }
            }
            lflv[LaborFindingLaborValue.ICD_ENTRY_VALUE].each { final entry ->
              coding {
                system = "urn:centraxx:CodeSystem/IcdCatalog-" + entry[IcdEntry.CATALOGUE]?.getAt(AbstractCatalog.ID)
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
                  // CXX uses the reference embedded in a coding to support multi selects
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

private static boolean isDTypeOf(final Object laborValue, final List<LaborValueDType> types) {
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

static boolean isCatalog(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.CATALOG])
}

static boolean isOptionGroup(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.OPTIONGROUP])
}

static boolean isCimdKenzusatzdaten(final String laborValueCode) {
  return ["ITEM_VISITENDATUM",
          "ITEM_RAUCHERSTATUS",
          "ITEM_BMI",
          "ITEM_PACKYEARS",
          "ITEM_RAUCHER_SEIT",
          "CIMD_EINSCHLUSSDIAGNOSE_LISTE",
          "ITEM_AUFGEHOERT_AB",
          "NUECHTERNSTATUS_HUB",
          "ITEM_DIAGNOSE_BEGLEITERKRANKUNG",
          "CIMD_AKTUELLE_MEDIKATION",
          "CIMD_AKTUELLE_MEDIKATION_WIRKSTOFFKLASSEN_ATC",
          "CIMD_MEDIKATION_FREITEXTFELD",
          "ITEM_POSITION_BEI_BLUTENTNAHME",
          "ITEM_STAUBINDE_UNMITTELBAR_NACH_BLUTEINFLUSS_GELOEST"].contains(laborValueCode)
}
