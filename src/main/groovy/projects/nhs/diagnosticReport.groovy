package projects.nhs

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * represented by CXX LaborMapping
 * @author Mike Wähnert
 * @since v.1.8.0, CXX.v.3.18.1
 */
diagnosticReport {

  final String laborMethodCode = context.source[laborMapping().laborFinding().laborMethod().code()]
  final boolean isExportRelevant = "DIAGNOSTIC_REPORT".equalsIgnoreCase(laborMethodCode)
  if (!isExportRelevant) {
    return
  }
  
  id = "DiagnosticReport/" + context.source[laborMapping().laborFinding().id()]

  identifier {
    system = "urn:centraxx"
    value = context.source[laborMapping().laborFinding().laborFindingId()]
  }

  status = DiagnosticReport.DiagnosticReportStatus.UNKNOWN

  code {
    coding {
      system = "urn:centraxx"
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
      display = context.source[laborMapping().laborFinding().laborMethod().nameMultilingualEntries()].find { final def entry ->
        "en" == entry[MultilingualEntry.LANG]
      }?.getAt(MultilingualEntry.VALUE)
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  if (context.source[laborMapping().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[laborMapping().episode().id()]
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  issued {
    date = context.source[laborMapping().laborFinding().creationDate()]
  }

  result {
    reference = "Observation/" + context.source[laborMapping().laborFinding().id()]
  }


  // there is no field for an interpretation/conclusion of results in CXX. To export such, a measure parameter must
  // be introduced in CXX.
  final def interpretation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "INTERPRETATION" == it[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE]
  }
  conclusion = interpretation ? interpretation[LaborFindingLaborValue.STRING_VALUE] : null
}
