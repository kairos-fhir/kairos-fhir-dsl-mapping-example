package customimport.ctcue.customexport

import de.kairos.fhir.centraxx.metamodel.PrecisionDate

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.CrfTemplateField.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.LaborFinding.LABOR_FINDING_LABOR_VALUES
import static de.kairos.fhir.centraxx.metamodel.LaborFinding.LABOR_METHOD
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.LaborMapping.LABOR_FINDING
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.STRING_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.medProcedure

procedure {

  id = "Procedure/" + context.source[medProcedure().id()]

  final def procedureDataMapping = context.source[medProcedure().laborMappings()].find {
    it[LABOR_FINDING][LABOR_METHOD][CODE] == "ADDITIONAL_PROCEDURE_DATA"
  }

  if (procedureDataMapping == null){
    return
  }

  final List bodySiteMappings = context.source[medProcedure().laborMappings()].findAll {
    it[LABOR_FINDING][LABOR_METHOD][CODE] == "ADDITIONAL_PROCEDURE_BODYSITE_DATA"
  }

  println(context.source)
  println(procedureDataMapping)
  println(bodySiteMappings)

  identifier {
    value = context.source[medProcedure().procedureId()]
  }

  final def parentLflv = findLabFinLabVal(procedureDataMapping, "Procedure.partOf")

  // atm cxx procedures are not linked to each other -> only way is to store identfier of parent in the finding
  if (parentLflv) {
    identifier {
      value = parentLflv?.getAt(STRING_VALUE)
    }
  }

  final def statusLflv = findLabFinLabVal(procedureDataMapping, "Procedure.status")

  status {
    value = statusLflv?.getAt(STRING_VALUE)
  }

  final def categoryCodeLflv = findLabFinLabVal(procedureDataMapping, "Procedure.category.coding.code")
  final def categorySystemLflv = findLabFinLabVal(procedureDataMapping, "Procedure.category.coding.system")
  final def categoryDisplayLflv = findLabFinLabVal(procedureDataMapping, "Procedure.category.coding.display")

  category {
    coding {
      system = categorySystemLflv?.getAt(STRING_VALUE) as String
      code = categoryCodeLflv?.getAt(STRING_VALUE) as String
      display = categoryDisplayLflv?.getAt(STRING_VALUE) as String
    }
  }

  final def codeCodeLflv = findLabFinLabVal(procedureDataMapping, "Procedure.code.coding.code")
  final def codeSystemLflv = findLabFinLabVal(procedureDataMapping, "Procedure.code.coding.system")
  final def codeDisplayLflv = findLabFinLabVal(procedureDataMapping, "Procedure.code.coding.display")

  code {
    coding {
      system = codeSystemLflv?.getAt(STRING_VALUE) as String
      code = codeCodeLflv?.getAt(STRING_VALUE) as String
      display = codeDisplayLflv?.getAt(STRING_VALUE) as String
    }
  }

  subject {
    reference = "Patient/" + context.source[medProcedure().patientContainer().id()]
  }

  performedDateTime = context.source[medProcedure().procedureDate()][PrecisionDate.DATE]

  bodySiteMappings.each { final def bodySiteMapping ->

    final def bodySiteCodeLflv = findLabFinLabVal(bodySiteMapping, "Procedure.bodySite.coding.code")
    final def bodySiteDisplayLflv = findLabFinLabVal(bodySiteMapping, "Procedure.bodySite.coding.code")

    bodySite {
      coding {
        code = bodySiteCodeLflv?.getAt(STRING_VALUE) as String
        display = bodySiteDisplayLflv?.getAt(STRING_VALUE) as String
      }
    }

  }
}


static def findLabFinLabVal(final def mapping, final String code) {
  return mapping[LABOR_FINDING][LABOR_FINDING_LABOR_VALUES].find {
    it[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == code
  }
}

