package common.utils

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE

class LflvUtils {

  static Map<String, Object> getLflvMap(final List lflvs, final Map<String, String> types) {
    final Map<String, Object> lflvMap = [:]

    types.each { final String lvCode, final String lvType ->
      final def lflvForLv = lflvs.find { final def lflv ->
        lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == lvCode
      }

      if (lflvForLv && lflvForLv[lvType]) {
        lflvMap[(lvCode)] = lflvForLv[lvType]
      }
    }
    return lflvMap
  }

  static Map<String, Object> getLflvMapFromLaborMapping(final def mapping, final Map<String, String> types) {
    final Map<String, Object> lflvMap = [:]
    if (!mapping) {
      return lflvMap
    }

    types.each { final String lvCode, final String lvType ->
      final def lflvForLv = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
        lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == lvCode
      }

      if (lflvForLv && lflvForLv[lvType]) {
        lflvMap[(lvCode)] = lflvForLv[lvType]
      }
    }
    return lflvMap
  }

}
