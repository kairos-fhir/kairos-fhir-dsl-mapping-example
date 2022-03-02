package projects.uscore


import org.hl7.fhir.r4.model.Immunization

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.PrecisionDate.DATE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.CATALOG_ENTRY_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.DATE_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
/**
 * Represents a CXX LaborMapping for the US Core Resource Profile: US Core Immunization Profile.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-immunization.html
 *
 * The mapping works with the master data specification that is provided in xml/immunization.xml
 * The xml file can be imported over CXX xml import interface
 * The corresponding code systems are provided rudimentary and are to be completed.
 *
 *
 * @author Mike Wähnert, Jonas Küttner
 * @since v.1.14.0, CXX.v.2022.1.0
 */

immunization {
  if ("US_CORE_IMMUNIZATION" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Immunization/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-immunization")
  }

  final def lblvClinicalStatus = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> "US_CORE_IMMUNIZATION_STATUS" == lblv[LABOR_VALUE][CODE]
  }

  clinicalStatus = Immunization.ImmunizationStatus.fromCode((lblvClinicalStatus[CATALOG_ENTRY_VALUE] as List)[0][CODE] as String)

  final def lblvStatusReason = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_IMMUNIZATION_STATUS_REASON"
  }

  if (lblvStatusReason) {
    statusReason {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v3-ActReason"
        code = (lblvStatusReason[CATALOG_ENTRY_VALUE] as List)[0][CODE] as String
      }
    }
  }

  final def lblvVaccineCode = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_VACCINE_CODE"
  }


  vaccineCode {
    coding {
      system = "https://vsac.nlm.nih.gov/valueset/2.16.840.1.113762.1.4.1010.6/expansion"
      code = (lblvVaccineCode[CATALOG_ENTRY_VALUE] as List)[0][CODE] as String
    }
  }

  patient {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  final def lblvDate = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_IMMUNIZATION_DATE"
  }

  if (lblvDate) {
    occurrenceDateTime {
      date = lblvDate[DATE_VALUE][DATE] as String
    }
  }

  final def lblvPrimarySource = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_IMMUNIZATION_PRIMARY_SOURCE"
  }

  primarySource = lblvPrimarySource[CATALOG_ENTRY_VALUE][CODE] as String
}