package projects.patientfinder

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate

import static de.kairos.fhir.centraxx.metamodel.RecordedValue.DATE_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.STRING_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
/**
 * Represented by a CXX LaborMapping
 * @author Jonas Küttner
 * @since CXX.v.2024.4.3, CXX.v.2024.5.0, kairos-fhir-dsl-1.42.0
 */

final String ALLERGEN = "Allergen.Allergen"
final String CATEGORY = "Allergen.Category"
final String COMMENTS = "Allergen.Comments"
final String REACTION = "Allergen.Reaction"
final String SEVERITY = "Allergen.Severity"
final String STATUS = "Allergen.Status"
final String STATUS_REASON = "Allergy.StatusReason"
final String FINISH = "Finish"
final String RECORD_DATE = "Record date"
final String START = "Start"


final Map PROFILE_TYPES = [
    (ALLERGEN)     : STRING_VALUE,
    (CATEGORY)     : STRING_VALUE,
    (COMMENTS)     : STRING_VALUE,
    (REACTION)     : STRING_VALUE,
    (SEVERITY)     : STRING_VALUE,
    (STATUS)       : STRING_VALUE,
    (STATUS_REASON): STRING_VALUE,
    (FINISH)       : DATE_VALUE,
    (RECORD_DATE)  : DATE_VALUE,
    (START)        : DATE_VALUE
]


allergyIntolerance {

  if ((context.source[laborMapping().laborFinding().laborMethod().code()] as String) != "Allergen") {
    return
  }

  id = "AllergyIntolerance/" + context.source[laborMapping().laborFinding().id()]

  final Map<String, Object> lflvMap = getLflvMap(context.source, PROFILE_TYPES)

  if (lflvMap.containsKey(STATUS)) {
    clinicalStatus {
      coding {
        code = lflvMap.get(STATUS) as String
      }
    }
  }


  if (lflvMap.containsKey(SEVERITY)) {
    criticality {
      extension {
        url = "https://fhir.iqvia.com/patientfinder/extension/code-specification-extension"
        valueCodeableConcept {
          coding {
            code = lflvMap.get(SEVERITY) as String
          }
        }
      }
    }
  }

  if (lflvMap.containsKey(CATEGORY)) {
    category {
      extension {
        url = "https://fhir.iqvia.com/patientfinder/extension/code-specification-extension"
        valueCodeableConcept {
          coding {
            code = lflvMap.get(CATEGORY) as String
          }
        }
      }
    }
  }

  patient {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  onsetPeriod {
    if (lflvMap.containsKey(START) && lflvMap.get(START)) {
      start {
        date = lflvMap.get(START)[PrecisionDate.DATE]
      }
    }
    if (lflvMap.containsKey(FINISH) && lflvMap.get(FINISH)) {
      end {
        date = lflvMap.get(FINISH)[PrecisionDate.DATE]
      }
    }
  }

  if (lflvMap.containsKey(RECORD_DATE) && lflvMap.get(RECORD_DATE)) {
    recordedDate {
      date = lflvMap.get(RECORD_DATE)[PrecisionDate.DATE]
    }
  }

  reaction {
    if (lflvMap.containsKey(ALLERGEN)) {
      substance {
        coding {
          code = lflvMap.get(ALLERGEN) as String
        }
      }
    }

    if (lflvMap.containsKey(REACTION)) {
      if (lflvMap.get(REACTION).class == String) {
        manifestation {
          coding {
            code = lflvMap.get(REACTION) as String
          }
        }
      } else {
        lflvMap.get(REACTION).each { final def reactionCode ->
          manifestation {
            coding {
              code = reactionCode as String
            }
          }
        }
      }
    }
  }
}

static Map<String, Object> getLflvMap(final def mapping, final Map<String, String> types) {
  final Map<String, Object> lflvMap = [:]
  if (!mapping) {
    return lflvMap
  }

  types.each { final String lvCode, final String lvType ->
    final List<Map<String, Object>> lflvForLv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].findAll { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == lvCode
    }

    if (lflvForLv.size() == 1) {
      lflvMap[(lvCode)] = lflvForLv[0][lvType]
    }

    if (lflvForLv && lflvForLv.size() > 1) {
      lflvMap[(lvCode)] = lflvForLv.collect { it[lvType] }
    }
  }

  return lflvMap
}
