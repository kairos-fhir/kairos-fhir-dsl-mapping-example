package projects.bbmri


import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.RootEntities
import org.hl7.fhir.r4.model.Observation
/**
 * Represented by a CXX LaborMapping
 * Specified by https://simplifier.net/bbmri.de/bodyheight
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.2
 */
observation {

  if ("BodyHeight" != context.source[RootEntities.laborMapping().laborFinding().laborMethod().code()]) {
    return // no export
  }

  id = "Observation/" + context.source[RootEntities.laborMapping().laborFinding().id()]

  meta {
    profile("https://fhir.bbmri.de/StructureDefinition/BodyHeight")
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
      code = "8302-2"
    }
  }

  subject {
    reference = "Patient/" + context.source[RootEntities.laborMapping().relatedPatient().id()]
  }

  effectiveDateTime {
    date = normalizeDate(context.source[RootEntities.laborMapping().laborFinding().findingDate().date()] as String)
  }

  final def bodyHeightLfLv = context.source[RootEntities.laborMapping().laborFinding().laborFindingLaborValues()].find {
    "BodyHeightValue" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }

  if (bodyHeightLfLv) {
    valueQuantity {
      value = bodyHeightLfLv[LaborFindingLaborValue.NUMERIC_VALUE]
      unit = "cm"
      system = "http://unitsofmeasure.org"
      code = "cm"
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