package projects.bbmri

import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * Specified by https://simplifier.net/bbmri.de/bodyweight
 *
 * @author Mike Wähnert
 * @since v.1.7.0. CXX.v.3.17.2
 */
observation {

  if ("BodyWeight" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return // no export
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("https://fhir.bbmri.de/StructureDefinition/BodyWeight")
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "vital-signs"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "29463-7"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  effectiveDateTime {
    date = normalizeDate(context.source[laborMapping().laborFinding().findingDate().date()] as String)
  }

  final def bodyHeightLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "BodyWeightValue" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }

  if (bodyHeightLfLv) {
    valueQuantity {
      value = bodyHeightLfLv[LaborFindingLaborValue.NUMERIC_VALUE]
      unit = "kg"
      system = "http://unitsofmeasure.org"
      code = "kg"
    }
  }

}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}