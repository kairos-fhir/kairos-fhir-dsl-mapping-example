package projects.patientfinder


import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueGroup
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ENTITY_SOURCE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.CrfTemplateField.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
/**
 * represented by CXX LaborMapping
 * @author Mike Wähnert
 * @since v.1.8.0, CXX.v.3.18.1
 *
 * This writes free text fields into a diagnostic report. if a LaborMethod code contains  '_free_text', all LabFinLabVals are
 * concatenated in to a string. For other profiles, only LabFinLabVals with a LaborValue code that contains _MEMO are written
 * in a concatenated string
 */
diagnosticReport {
  final def laborMethod = context.source[laborMapping().laborFinding().laborMethod()]
  final String laborMethodCode = laborMethod[CODE]

  if ("Allergen".equalsIgnoreCase(laborMethodCode)){
    return
  }

  final boolean isFreeText = laborMethodCode.contains("_free_text") || "Histology".equalsIgnoreCase(laborMethodCode) || "histological and cytological findings".equalsIgnoreCase(laborMethodCode)

  def labFinLabVals

  if (isFreeText) {
    labFinLabVals = (Collection) context.source[laborMapping().laborFinding().laborFindingLaborValues()]
  } else {
    labFinLabVals = context.source[laborMapping().laborFinding().laborFindingLaborValues()].findAll {
      final lflv ->
        ((String) lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE])
            .toLowerCase()
            .contains("_memo")
    }
  }

  if (labFinLabVals.isEmpty()) {
    return
  }

  id = "DiagnosticReport/" + context.source[laborMapping().laborFinding().id()]

  if (context.source[laborMapping().mappingType()].toString().equalsIgnoreCase(LaborMappingType.SAMPLELABORMAPPING.toString())) {
    specimen {
      reference = "Specimen/" + context.source[laborMapping().relatedOid()]
    }
  }

  identifier {
    system = "urn:centraxx"
    value = context.source[laborMapping().laborFinding().laborFindingId()]
  }

  status = DiagnosticReport.DiagnosticReportStatus.UNKNOWN

  code {
    coding {
      system = "urn:centraxx"
      code = laborMethodCode
      display = laborMethod[LaborMethod.NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE)
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  if (!isFakeEpisode(context.source[laborMapping().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[laborMapping().episode().id()]
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source[laborMapping().laborFinding().findingDate().date()] as String)
  }

  issued {
    date = context.source[laborMapping().laborFinding().creationDate()]
  }

  result {
    reference = "Observation/" + context.source[laborMapping().laborFinding().id()]
  }

  final def concatString = labFinLabVals.findAll{final lflv -> lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] != "Specialism"} // filter specialism
      .collect {
    final lflv ->
      final def laborValue = lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE]
      if (isString(laborValue)) {
        return "${laborValue[CODE]}:" + (lflv[LaborFindingLaborValue.STRING_VALUE] ? " ${lflv[LaborFindingLaborValue.STRING_VALUE]}" : "")
      } else if (isBoolean(laborValue)) {
        return "${laborValue[CODE]}:" + " ${lflv[LaborFindingLaborValue.BOOLEAN_VALUE]}"
      } else if (isDate(laborValue)) {
        return "${laborValue[CODE]}:" + (lflv[LaborFindingLaborValue.DATE_VALUE]?.getAt(PrecisionDate.DATE) ?
            " ${lflv[LaborFindingLaborValue.DATE_VALUE]?.getAt(PrecisionDate.DATE)}" : "")
      } else if (isNumeric(laborValue)) {
        return "${laborValue[CODE]}:" + (lflv[LaborFindingLaborValue.NUMERIC_VALUE] ? " ${lflv[LaborFindingLaborValue.NUMERIC_VALUE]}" : "")
      } else {
        return "${laborValue[CODE]}:" // assumes that catalog values are never used in these labor findings
      }
  }.join("\n\n")

  conclusion = concatString

  final def specialism = labFinLabVals.find{final lflv -> lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == "Specialism"} // filter specialism

  if (specialism != null){
    performer {
      display = specialism[LaborFindingLaborValue.STRING_VALUE]
    }
  }

  final def uniqueGroups = labFinLabVals.collect { final def lflv ->
    return lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][LaborValue.GROUP]
  }.findAll().unique()

  uniqueGroups.sort { it[CODE] }.each { final def group ->
    category {
      coding {
        code = group[CODE] as String
        display = group[LaborValueGroup.NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE)
      }
    }
  }
}

static boolean isFakeEpisode(final def episode) {
  if (episode == null) {
    return true
  }

  if (["SACT", "COSD"].contains(episode[ENTITY_SOURCE])) {
    return true
  }

  final def fakeId = episode[Episode.ID_CONTAINER]?.find { (it[PSN] as String).toUpperCase().startsWith("FAKE") }
  return fakeId != null
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

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}