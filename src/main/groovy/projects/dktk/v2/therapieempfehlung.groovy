package projects.dktk.v2

import de.kairos.fhir.centraxx.metamodel.AbstractCode
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import org.hl7.fhir.r4.model.CarePlan

import java.util.function.Function

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Transforms CXX LaborFinding of the LaborMethod "DKTK-Therapieempfehlung der Tumorkonferenz" to a FHIR CarePlan
 * Import xml/masterdata_therapieempfehlung.xml before
 * @author Mike Wähnert
 * @since CXX.3.18.3.14, CXX.3.18.4, CXX.2022.1.0
 */
carePlan {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "DKTK-Therapieempfehlung der Tumorkonferenz") {
    return
  }

  id = "CarePlan/" + context.source[laborMapping().laborFinding().id()]

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  def lflvs = context.source[laborMapping().laborFinding().laborFindingLaborValues()]

  lflvs.each { final lflv ->
    final def laborValue = lflv[LaborFindingLaborValue.LABOR_VALUE] != null
        ? lflv[LaborFindingLaborValue.LABOR_VALUE] // before CXX.v.2022.3.0
        : lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE] // from CXX.v.2022.3.0
    final String laborValueCode = laborValue?.getAt(CODE) as String

    // parameter Therapieempfehlung
    if (laborValueCode.equalsIgnoreCase("Tumorkonferenz ID")) {
      identifier {
        value = lflv[LaborFindingLaborValue.STRING_VALUE]
      }
    }
  }

  created {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  Map<Integer, List<Map<String, Object>>> lflvsOrderedByValueIndex = orderByValueIndex(lflvs)
  lflvsOrderedByValueIndex.values().each { lflvRow ->
    activity {
      detail {
        boolean hasStatus = false

        lflvRow.each { final lflv ->
          final def laborValue = lflv[LaborFindingLaborValue.LABOR_VALUE] != null
              ? lflv[LaborFindingLaborValue.LABOR_VALUE] // before CXX.v.2022.3.0
              : lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE] // from CXX.v.2022.3.0
          final String laborValueCode = laborValue?.getAt(CODE) as String

          // parameter Therapieempfehlung
          if (laborValueCode.equalsIgnoreCase("Therapieempfehlung")) {
            code {
              lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
                coding {
                  system = "http://dktk.dkfz.de/fhir/onco/core/ValueSet/TherapieempfehlungCS"
                  code = entry[CODE] as String
                }
              }
            }
          }

          //status = CarePlan.CarePlanActivityStatus.UNKNOWN // overridden by a more concrete value below, if exists
          if (laborValueCode.equalsIgnoreCase("Status der Therapieempfehlung")) {
            lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
              status = CarePlan.CarePlanActivityStatus.fromCode(entry[CODE] as String)
              hasStatus = true
            }
          }

          // parameter Therapieabweichung
          if (laborValueCode.equalsIgnoreCase("Therapieabweichung")) {
            statusReason {
              lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
                coding {
                  system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/JNUCS"
                  code = entry[CODE] as String
                }
              }
            }
          }
        }

        if (!hasStatus) {
          status = CarePlan.CarePlanActivityStatus.UNKNOWN
        }
      }
    }
  }
}

static Map<Integer, List<Map<String, Object>>> orderByValueIndex(def lflvs) {
  Map<Integer, List<Map<String, Object>>> result = new HashMap<>()
  for (Map<String, Object> lflv : lflvs) {
    Integer valueIndex = lflv.get(LaborFindingLaborValue.VALUE_INDEX) as Integer
    List<Map<String, Object>> lflvRow = result.computeIfAbsent(valueIndex, new Function<Integer, List<Map<String, Object>>>() {
      @Override
      List<Map<String, Object>> apply(final Integer integer) {
        return new ArrayList<Map<String, Object>>()
      }
    })
    lflvRow.add(lflv)
  }

  return result.sort();
}