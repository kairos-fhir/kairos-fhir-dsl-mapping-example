package customimport.ctcue.customexport.simple

import ca.uhn.fhir.context.FhirContext
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.AllergyIntolerance
import org.hl7.fhir.r4.model.Resource

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

resource {
  resource = new AllergyIntolerance()

  if (context.source[laborMapping().laborFinding().laborMethod()] == "FHIR_RESOURCE") {
    return
  }

  final def res = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lflv -> lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == "FHIR_RESOURCE"
  }

  if (res == null || res[LaborFindingLaborValue.STRING_VALUE] == null) {
    return
  }

  final FhirContext fhirContext = FhirContext.forR4()
  resource = fhirContext.newJsonParser().parseResource(res[LaborFindingLaborValue.STRING_VALUE] as String) as Resource
}