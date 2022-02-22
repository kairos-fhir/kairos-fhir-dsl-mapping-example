package projects.uscore


import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.CATALOG_ENTRY_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
/**
 * Represents a CXX LaborMapping for the US Core Resource Profile: US Core Implantable Device Profile.
 * Specified by http://hl7.org/fhir/us/core/StructureDefinition/us-core-allergyintolerance
 *
 * TODO: Work in progress!
 *
 * @author Mike Wähnert
 * @since v.1.14.0, CXX.v.2022.1.0
 */

allergyIntolerance {

  if ("US_CORE_ALLERGY_INTOLERANCE" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "AllergyIntolerance/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-allergyintolerance")
  }

  final def lblvClinicalStatus = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_ALLERGY_INTOLERANCE_CLINICAL_STATUS" }


  clinicalStatus {
    coding {
      system = "http://hl7.org/fhir/ValueSet/allergyintolerance-clinical"
      code = (lblvClinicalStatus[CATALOG_ENTRY_VALUE] as List)[0][CODE] as String
    }
  }

  final def lblvStatusReason = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_ALLERGY_INTOLERANCE_VERIFICATION_STATUS" }

  verificationStatus {
    coding {
      system = "http://hl7.org/fhir/R4/valueset-allergyintolerance-verification.html"
      code = (lblvStatusReason[CATALOG_ENTRY_VALUE] as List)[0][CODE] as String
    }
  }

  final def lblvCode = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_ALLERGY_INTOLERANCE_CODE" }

  code {
    coding {
      system = "https://vsac.nlm.nih.gov/valueset/2.16.840.1.113762.1.4.1186.8/expansion"
      code = (lblvCode[CATALOG_ENTRY_VALUE] as List)[0][CODE] as String
    }
  }

  patient {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  final def lblvManifestation = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_ALLERGY_INTOLERANCE_MANIFESTATION" }

  reaction {
    lblvManifestation?.each {final def ce ->
      manifestation {
        coding {
          system = "http://hl7.org/fhir/ValueSet/clinical-findings"
          code = ce[CODE] as String
        }
      }
    }
  }

}


