package projects.databox

import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFindingLaborValue
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by CXX LaborMapping
 * @author Marvin Schmidtke
 * TODO: extend example for Enumerations and RadioOptionGroups
 * The first code of each component represents the LaborValue.Code in CXX. Further codes could be representations in LOINC, SNOMED-CT etc.
 * LaborValueIdContainer in CXX are just an export example, but not intended to be imported by CXX FHIR API yet.
 */
observation {
  final def whitelistFindingCodes = ["RADIOLOGYFINDING", "MOLPATH_MUTATIONDIAGNOSIS", "MICROBIOLOGYFINDING", "MOLPATH_LYMPHOMDIAGNOSIS", "LEGACY_MP", "MOLPATH_FISH", "MOLPATH_PATHOGEN", "MOLPATH_ENDOPREDICT"]
  if(!whitelistFindingCodes.contains(context.source[laborMapping().laborFinding().laborMethod().code()] as String)){
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


  final def episode = context.source[laborFindingLaborValue().laborFinding().laborMappings()].find { it[LaborMapping.EPISODE] != null }
  if (episode) {
    encounter {
      reference = "Encounter/" + episode[Episode.ID]
    }
  }

  final def patIdContainer = context.source[laborMapping().relatedPatient().idContainer()]?.find {
    "MPI" == it[ID_CONTAINER_TYPE]?.getAt(CODE)
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
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
    }
  }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->
    component {
      code {
        coding {
          system = "urn:centraxx"
          code = lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(CODE) as String
        }
        lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.IDCONTAINERS)?.each { final idContainer ->
          coding {
            system = idContainer[ID_CONTAINER_TYPE]?.getAt(CODE)
            code = idContainer[PSN] as String
          }
        }
      }

      if (isNumeric(lflv)) {
        valueQuantity {
          value = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
          unit = lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValueNumeric.UNIT)?.getAt(CODE) as String
        }
      }
      if (isDecimal(lflv)) {
        valueQuantity {
          if(0E-8 == lflv[LaborFindingLaborValue.NUMERIC_VALUE]){
            value = "0"
            unit = lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValueNumeric.UNIT)?.getAt(CODE) as String
          } else {
            value = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
            unit = lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValueNumeric.UNIT)?.getAt(CODE) as String
          }
        }
      }
      if (isBoolean(lflv)) {
        valueBoolean(lflv[LaborFindingLaborValue.BOOLEAN_VALUE] as Boolean)
      }

      if (isDate(lflv)) {
        valueDateTime {
          date = lflv[LaborFindingLaborValue.DATE_VALUE]?.getAt(PrecisionDate.DATE)
        }
      }

      if (isTime(lflv)) {
        valueTime(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
      }

      if (isString(lflv)) {
        valueString(lflv[LaborFindingLaborValue.STRING_VALUE] as String)
      }

      if (isCatalog(lflv)) {
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
        }
      }
    }
  }
}

private static boolean isDTypeOf(final Object lflv, final List<LaborValueDType> types) {
  return types.contains(lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as LaborValueDType)
}

static boolean isBoolean(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.BOOLEAN])
}

static boolean isNumeric(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.INTEGER, LaborValueDType.SLIDER])
}

static boolean isDecimal(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.DECIMAL])
}

static boolean isDate(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.DATE, LaborValueDType.LONGDATE])
}

static boolean isTime(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.TIME])
}

static boolean isEnumeration(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.ENUMERATION])
}

static boolean isString(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.STRING, LaborValueDType.LONGSTRING])
}

static boolean isCatalog(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.CATALOG])
}

static boolean isOptionGroup(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.OPTIONGROUP])
}
