package projects.patientfinder

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import de.kairos.fhir.centraxx.metamodel.enums.LaborMethodCategory
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * represented by CXX LaborMapping
 * @author Mike Wähnert
 * @since v.1.44.0, CXX.v.2025.1.0
 */
diagnosticReport {

  if (context.source[laborMapping().laborFinding().laborMethod().category()] as LaborMethodCategory != LaborMethodCategory.OTHER){
    return
  }

  id = "DiagnosticReport/" + context.source[laborMapping().laborFinding().id()]

  identifier {
    value = context.source[laborMapping().laborFinding().laborFindingId()]
  }

  status = DiagnosticReport.DiagnosticReportStatus.UNKNOWN

  category {
    coding {
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
      display = context.source[laborMapping().laborFinding().laborMethod().nameMultilingualEntries()]
          .find { final def me -> me[MultilingualEntry.LANG] == "en" }?.getAt(MultilingualEntry.VALUE)
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  final def lflvString = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find { final def lflv ->
    (lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.D_TYPE] as LaborValueDType) in [LaborValueDType.LONGSTRING, LaborValueDType.STRING]
  }

  conclusion = lflvString ? lflvString[LaborFindingLaborValue.STRING_VALUE] : null
}
