package customexport.patientfinder.iqtrial

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborMethodCategory
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a HDRP LaborMapping
 * @author Mike Wähnert
 * @since v.1.52.0
 * @since HDRP v.2025.3.0
 * The first code of each component represents the LaborValue.Code in HDRP. Further codes could be representations in LOINC, SNOMED-CT etc.
 * LaborValueIdContainer in HDRP are just an export example, but not intended to be imported by HDRP FHIR API yet.
 */
observation {

  if (context.source[laborMapping().laborFinding().laborMethod().category()]  != LaborMethodCategory.VITALSIGN.toString()){
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

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

  if (context.source[laborMapping().mappingType()] as LaborMappingType == LaborMappingType.SAMPLELABORMAPPING){
    specimen {
      reference = "Specimen/" + context.source[laborMapping().relatedOid()]
    }
  }

  method {
    coding {
      system = FhirUrls.System.LaborMethod.BASE_URL
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
    }
  }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->

    final def laborValue = lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE] // from HDRP.v.2022.3.0

    final String laborValueCode = laborValue?.getAt(CODE) as String

    final String laborValueDisplay = laborValue?.getAt(LaborValue.MULTILINGUALS)?.find { final def ml ->
      ml[Multilingual.LANGUAGE] == "en" && ml[Multilingual.SHORT_NAME] != null
    }?.getAt(Multilingual.SHORT_NAME) as String

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

