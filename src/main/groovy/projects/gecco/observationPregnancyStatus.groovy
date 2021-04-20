package projects.gecco


import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since v.1.7.0, CXX.v.3.17.2
 */
observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "PREGNANCY_CODE") {
    return // no export
  }

  id = "PregnancyStatus/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pregnancy-status"
  }

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      system = "http://loinc.org"
      code = "82810-3"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }
  encounter {
    reference = "Episode/" + context.source[laborMapping().episode().id()]
  }


  effectiveDateTime {
    date = normalizeDate(context.source[laborMapping().laborFinding().findingDate().date()] as String)
  }

  final def pregStatLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "PREGNANCYSTATUS_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (pregStatLfLv) {
    final Map<String, Object> multiValue = pregStatLfLv[LaborFindingLaborValue.MULTI_VALUE] as Map<String, Object>
    final def singleValue = multiValue.iterator().next()[UsageEntry.CODE] as String
    if (singleValue == "YES_CODE") {
      valueCodeableConcept {
        coding {
          system = "http://loinc.org"
          code = "LA15173-0"
        }
        coding {
          system = "http://snomed.info/sct"
          code = "77386006"
        }
      }
    } else if (singleValue == "NO_CODE") {
      valueCodeableConcept {
        coding {
          system = "http://loinc.org"
          code = "LA26683-5"
        }
      }
    }
  } else {
    valueCodeableConcept {
      coding {
        system = "http://loinc.org"
        code = "LA4489-6" //LOINC answer ID for unknown pregnancy
      }
    }
  }

  //If the measurement profile contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
  final def pregStatAnnotation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "ANNOTATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (pregStatAnnotation) {
    note {
      text = pregStatAnnotation[LaborFindingLaborValue.STRING_VALUE] as String
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}
