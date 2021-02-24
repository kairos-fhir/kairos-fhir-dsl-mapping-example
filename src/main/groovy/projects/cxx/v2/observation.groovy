package projects.cxx.v2

import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.3.17.2
 */
observation {
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
    "COVID-19-PATIENTID" == it[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }
  if (patIdContainer) {
    subject {
      identifier {
        value = patIdContainer[PSN]
        type {
          coding {
            system = "urn:centraxx"
            code = patIdContainer[ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE) as String
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
          code = lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE) as String
        }
      }

      if (isNumeric(lflv)) {
        valueQuantity {
          value = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
        }
      }
      if (isBoolean(lflv)) {
        valueBoolean(lflv[LaborFindingLaborValue.BOOLEAN_VALUE] as Boolean)
      }

      if (isDate(lflv)) {
        valueDateTime {
          date = lflv[LaborFindingLaborValue.DATE_VALUE]
        }
      }

      if (isTime(lflv)) {
        valueTime(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
      }

      if (isString(lflv)) {
        valueString(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
      }

      if (isString(lflv)) {
        valueString(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
      }

      if (isString(lflv)) {
        valueString(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
      }

    }
  }
}

static boolean isBoolean(final Object lflv) {
  return LaborValueDType.BOOLEAN == lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE)
}

static boolean isNumeric(final Object lflv) {
  return ["INTEGER", "DECIMAL", "SLIDER"].contains(lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as String)
}

static boolean isDate(final Object lflv) {
  return ["DATE", "LONGDATE"].contains(lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as String)
}

static boolean isTime(final Object lflv) {
  return ["TIME"].contains(lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as String)
}

static boolean isEnumeration(final Object lflv) {
  return ["ENUMERATION"].contains(lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as String)
}

static boolean isString(final Object lflv) {
  return ["STRING", "LONGSTRING"].contains(lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as String)
}

static boolean isCatalog(final Object lflv) {
  return ["CATALOG"].contains(lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as String)
}

static boolean isOptionGroup(final Object lflv) {
  return ["OPTIONGROUP"].contains(lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as String)
}