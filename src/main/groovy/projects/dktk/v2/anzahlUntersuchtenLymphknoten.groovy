package projects.dktk.v2


import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * Specified by https://simplifier.net/oncology/anzahluntersuchtenlymphknoten
 * @author Mike Wähnert
 * @since kairos-fhir-dsl.v.1.12.0, CXX.v.3.18.1.19, CXX.v.3.18.2
 * Based on the measurement profile, which has been specified by the CCP-IT group in ../xml/masterdata_lymphknoten.xml
 */
observation {

  if ("DKTK-Lymphknoten" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  def lflv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find { final lflv ->
    final def laborValue = lflv[LaborFindingLaborValue.LABOR_VALUE] != null
        ? lflv[LaborFindingLaborValue.LABOR_VALUE] // before CXX.v.2022.3.0
        : lflv["crfTemplateField"][CrfTemplateField.LABOR_VALUE] // from CXX.v.2022.3.0
    final String laborValueCode = laborValue?.getAt(CODE) as String
    return laborValueCode.equalsIgnoreCase("LK_untersucht")
  }
  if (lflv == null) {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Observation-AnzahlUntersuchtenLymphknoten"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://hl7.org/fhir/observation-category"
      code = "laboratory"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "21894-1"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  final String focusReference = getSpecimenReference(
      context.source[laborMapping().mappingType()] as LaborMappingType,
      context.source[laborMapping().relatedOid()] as String)

  if (focusReference != null) {
    specimen {
      reference = focusReference
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source[laborMapping().laborFinding().findingDate().date()] as String)
  }

  valueQuantity {
    value = lflv[LaborFindingLaborValue.NUMERIC_VALUE] as String
  }

}

static String getSpecimenReference(final LaborMappingType mappingType, final String relatedOid) {
  return LaborMappingType.SAMPLELABORMAPPING == mappingType ? "Specimen/" + relatedOid : null
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}
