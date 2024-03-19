package projects.cxx.v2

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Coverage

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientInsurance

/**
 * Represented by a CXX PatientInsurance
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.30.0, CXX.v.2024.2.0
 */
coverage {

  id = "Coverage/" + context.source[patientInsurance().id()]

  status = Coverage.CoverageStatus.ACTIVE

  beneficiary {
    reference = "Patient" + context.source[patientInsurance().patientContainer().id()]
  }

  relationship {
    coding {
      system = FhirUrls.System.Patient.PatientInsurance.RELATIONSHIP
      code = context.source[patientInsurance().insuredRelationship()]
    }
  }

  period {
    start {
      date = context.source[patientInsurance().validFrom()]
    }
    end {
      date = context.source[patientInsurance().validUntil()]
    }
  }

  payor {
    reference = "Organization/InsuranceCompany-" + context.source[patientInsurance().insuranceCompany().id()]
  }

  class_ {
    type {
      coding {
        system = FhirUrls.System.Patient.PatientInsurance.COVERAGE_TYPE
      }
    }
    value = context.source[patientInsurance().coverageType()]
  }

  extension {
    url = FhirUrls.Extension.PatientInsurance.POLICE_NUMBER
    valueString = context.source[patientInsurance().policeNumber()]
  }

  extension {
    url = FhirUrls.Extension.PatientInsurance.RANK
    valueInteger = context.source[patientInsurance().rank()] as Integer
  }

}
