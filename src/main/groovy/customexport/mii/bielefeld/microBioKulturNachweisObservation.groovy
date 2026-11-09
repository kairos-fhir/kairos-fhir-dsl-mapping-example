package customexport.mii.bielefeld


import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import org.hl7.fhir.r4.model.DiagnosticReport

import java.util.Map.Entry

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding

//final String OBS_CODE = "Observation.code"
final String OBS_STATUS = "Observation.status"
final String OBS_KL_BEZUGS_DATUM = "Observation.effective[x].extension:QuelleKlinischesBezugsdatum.value[x].code"
final String OBS_EFFECTIVE = "Observation.effective[x]"
final String OBS_VAL_CODEABLE_CONCEPT = "Observation.value[x]:valueCodeableConcept"
final String EMP_CODE = "Observation.code_Empfindlichkeit"
final String EMP_INTERPRETATION = "Observation.interpretation:EUCAST"


final Map PROFILE_TYPES = [
    (OBS_STATUS)              : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (OBS_KL_BEZUGS_DATUM)     : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (OBS_EFFECTIVE)           : LaborFindingLaborValue.DATE_VALUE,
    (OBS_VAL_CODEABLE_CONCEPT): LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (EMP_CODE)                : LaborFindingLaborValue.STRING_VALUE, // TODO: should be catalog
    (EMP_INTERPRETATION)      : LaborFindingLaborValue.CATALOG_ENTRY_VALUE
]

bundle {

  if (context.source[laborFinding().laborMethod().code()] != "MP_Mikrobio_KulturNachweis") {
    return
  }


  final def lm = context.source[laborFinding().laborMappings()].find {
    it[LaborMapping.RELATED_PATIENT] != null
  }

  final lflvMap = getLflvMap(context.source, PROFILE_TYPES)

  final Map<String, Object> obsLvlvMap = lflvMap.get(0)

  final def obsStatus = obsLvlvMap.get(OBS_STATUS)?.find()
  final def obsEffectivePD = obsLvlvMap.get(OBS_EFFECTIVE)
  final def obsKlinischesBD = obsLvlvMap.get(OBS_KL_BEZUGS_DATUM)?.find()

  /**
   * Export the copyable group as Empfindlichkeit Observations first as they are references by the Kulturnachweis Observation
   */
  for (final Entry<Integer, Map<String, Object>> e : lflvMap.entrySet()) {
    final Integer valueIndex = e.getKey()
    final Map<String, Object> empLflvMap = e.getValue()

    entry {
      resource {
        observation {
          id = "Observation/" + context.source[laborFinding().id()] + "-" + valueIndex

          identifier {
            type {
              coding {
                system = "http://terminology.hl7.org/CodeSystem/v2-0203"
                code = "OBI"
              }
            }
            system = "https://ukowl.de/sid/mii-exa-mikrobio-empfindlichkeit"
            value = context.source[laborFinding().laborFindingId()]

            assigner {
              identifier {
                system = "https://www.medizininformatik-initiative.de/fhir/core/CodeSystem/core-location-identifier"
                value = "ukowl.de"
              }
            }
          }


          if (obsStatus) {
            status = obsStatus[CODE]
          } else {
            status = DiagnosticReport.DiagnosticReportStatus.UNKNOWN
          }

          category {
            coding {
              system = "http://loinc.org"
              code = "26436-6"
            }
            coding {
              system = "http://terminology.hl7.org/CodeSystem/observation-category"
              code = "laboratory"
            }
            coding {
              system = "http://loinc.org"
              code = "18725-2"
              display = "Microbiology studies"
            }
          }


          if (empLflvMap.get(EMP_CODE))
            code {
              coding {
                system = "http://loinc.org"
                code = empLflvMap.get(EMP_CODE) as String
              }
            }


          if (lm) {
            subject {
              reference = "Patient/" + lm[LaborMapping.RELATED_PATIENT][PatientContainer.ID]
            }
          }


          if (obsEffectivePD != null && obsEffectivePD[PrecisionDate.DATE] != null) {
            effectiveDateTime {
              date = obsEffectivePD[PrecisionDate.DATE]

              if (obsKlinischesBD) {
                extension {
                  url = "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum"
                  valueCoding {
                    system = "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/ValueSet/QuelleKlinischesBezugsdatum"
                    code = obsKlinischesBD[CODE] as String
                  }
                }
              }
            }
          }

          final def empInterpretation = empLflvMap.get(EMP_INTERPRETATION)?.find()

          if (empInterpretation) {
            interpretation {
              coding {
                system = "http://snomed.info/sct"
                code = empInterpretation[CODE] as String
              }
            }
          }
        }
      }
    }
  }

  // map for valueIndex 1 (default).. contains all  the general Kulturnachweis parameters
  entry {
    resource {
      observation {
        id = "Observation/" + context.source[laborFinding().id()]

        identifier {
          type {
            coding {
              system = "http://terminology.hl7.org/CodeSystem/v2-0203"
              code = "OBI"
            }
          }
          system = "https://ukowl.de/sid/mii-exa-mikrobio-kulturnachweis"
          value = context.source[laborFinding().laborFindingId()]

          assigner {
            identifier {
              system = "https://www.medizininformatik-initiative.de/fhir/core/CodeSystem/core-location-identifier"
              value = "ukowl.de"
            }
          }
        }

        if (obsStatus) {
          status = obsStatus[CODE]
        } else {
          status = DiagnosticReport.DiagnosticReportStatus.UNKNOWN
        }

        category {
          coding {
            system = "http://loinc.org"
            code = "26436-6"
          }
          coding {
            system = "http://terminology.hl7.org/CodeSystem/observation-category"
            code = "laboratory"
          }
          coding {
            system = "http://loinc.org"
            code = "18725-2"
            display = "Microbiology studies"
          }
        }

        code {
          coding {
            system = "http://loinc.org"
            code = "11475-1"
          }
        }


        if (lm) {
          subject {
            reference = "Patient/" + lm[LaborMapping.RELATED_PATIENT][PatientContainer.ID]
          }
        }

        if (obsEffectivePD != null && obsEffectivePD[PrecisionDate.DATE] != null) {
          effectiveDateTime {
            date = obsEffectivePD[PrecisionDate.DATE]

            if (obsKlinischesBD) {
              extension {
                url = "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum"
                valueCoding {
                  system = "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/ValueSet/QuelleKlinischesBezugsdatum"
                  code = obsKlinischesBD[CODE] as String
                }
              }
            }
          }
        }

        final def obsValue = obsLvlvMap.get(OBS_VAL_CODEABLE_CONCEPT)?.find()

        if (obsValue) {
          valueCodeableConcept {
            coding {
              system = "http://snomed.info/sct"
              code = obsValue[CODE] as String
            }
          }
        }

        lflvMap.keySet().forEach { final def valueIndex ->
          hasMember {
            reference = "Observation/" + context.source[laborFinding().id()] + "-" + valueIndex
          }
        }
      }
    }
  }
}

static Map<Integer, Map<String, Object>> getLflvMap(final def finding, final Map<String, String> types) {
  final Map<Integer, Map<String, Object>> lflvMap = [:]
  if (!finding) {
    return lflvMap
  }


  types.each { final String lvCode, final String lvType ->
    final def lflvsForLv = finding[LaborFinding.LABOR_FINDING_LABOR_VALUES].findAll { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == lvCode
    }

    lflvsForLv.each {
      if (it[lvType]) {
        final Integer valueIndex = it[LaborFindingLaborValue.VALUE_INDEX] as Integer
        lflvMap.computeIfAbsent(valueIndex, k -> new HashMap<>()).put(lvCode, it[lvType])
      }
    }
  }
  return lflvMap
}
