package projects.gemelli

import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.CrfTemplateField.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a CXX LaborMapping.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-procedure.html
 *
 * @author Mike Wähnert
 * @since v.1.13.0, CXX.v.2023.1.0
 *
 */
procedure {

  if ("Gemelli-Procedure" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Procedure/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-procedure")
  }

  status = Procedure.ProcedureStatus.UNKNOWN

  final def procedureCodeValue = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == "Procedure code" }

  final def procedureDescriptionValue = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == "Procedure description" }

  code {
    coding {
      system = "http://hl7.org/fhir/sid/icd-9-cm"
      code = procedureCodeValue?.getAt(LaborFindingLaborValue.STRING_VALUE) as String
      display = procedureDescriptionValue?.getAt(LaborFindingLaborValue.STRING_VALUE)
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  performedDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }
}
