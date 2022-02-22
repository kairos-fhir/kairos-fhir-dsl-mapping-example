package projects.uscore


import org.hl7.fhir.r4.model.Immunization

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.CATALOG_ENTRY_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

immunization {
  if ("US_CORE_IMMUNIZATION" != context.source[laborMapping().laborFinding().laborMethod().code()]){
    return
  }

  final def lblvClinicalStatus = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
     final lblv -> "US_CORE_IMMUNIZATION_STATUS" == lblv[LABOR_VALUE][CODE]
  }

  clinicalStatus(Immunization.ImmunizationStatus.fromCode(
      lblvClinicalStatus[CATALOG_ENTRY_VALUE][CODE] as String
  ))

  final def lblvStatusReason = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> "US_CORE_IMMUNIZATION_STATUS_REASON" == lblv[LABOR_VALUE][CODE]
  }

  if (lblvStatusReason) {
    statusReason {
      coding {
        system = "http://hl7.org/fhir/ValueSet/immunization-status-reason"
        code = lblvStatusReason[CATALOG_ENTRY_VALUE][CODE] as String
      }
    }
  }

  final def lblvVaccineCode = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> "US_CORE_VACCINE_CODE" == lblv[LABOR_VALUE][CODE]
  }


  vaccineCode {
    coding {
      system = "https://vsac.nlm.nih.gov/valueset/2.16.840.1.113762.1.4.1010.6/expansion"
      code = lblvVaccineCode[CATALOG_ENTRY_VALUE][CODE] as String
    }
  }

  patient {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  final def lblvDate = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> "US_CORE_IMMUNIZATION_DATE" == lblv[LABOR_VALUE][CODE]
  }

  if (lblvDate) {
    occurrenceDateTime {
      date = lblvDate[CATALOG_ENTRY_VALUE][CODE] as String
    }
  }

  final def lblvPrimarySource = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> "US_CORE_IMMUNIZATION_PRIMARY_SOURCE" == lblv[LABOR_VALUE][CODE]
  }

  primarySource = lblvPrimarySource[CATALOG_ENTRY_VALUE][CODE] as String
}