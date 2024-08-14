package projects.uscore


import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.OrganisationUnit
import de.kairos.fhir.centraxx.metamodel.ValueReference

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a CXX LaborMapping for the US Core Resource Profile: US Core Provenance profile.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-provenance.html
 *
 * @author Mike Wähnert, Niklas Biedka
 * @since v.1.14.0, CXX.v.2022.1.0
 */
provenance {

  if ("US_CORE_PROVENANCE" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Provenance/" + context.source[laborMapping().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-provenance")
  }

  target {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  final def lblvRecorded = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find() {
    final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_RECORDED"
  }

  recorded = lblvRecorded

  final def lblvOrganizations = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find() {
    final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_ORGANIZATION"
  }

  final def combinedList = []
  lblvOrganizations[LaborFindingLaborValue.MULTI_VALUE_REFERENCES].each {
    final entry -> combinedList.add(entry)
  }
  lblvOrganizations[LaborFindingLaborValue.MASTER_DATA_CATALOG_ENTRY_VALUE].each {
    final entry -> combinedList.add(entry)
  }

  agent {
    type {
      coding {
        system = "http://hl7.org/fhir/us/core/CodeSystem/us-core-provenance-participant-type"

      }
    }
    combinedList.each { final mdce ->
      who {
        reference = "Organization/" + mdce[ValueReference.ORGANIZATION_VALUE][OrganisationUnit.ID]
      }
    }
  }

  agent {
    type {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/provenance-participant-type"
        code = "author"
      }
    }
  }

  agent {
    type {
      coding {
        system = "http://hl7.org/fhir/us/core/CodeSystem/us-core-provenance-participant-type"
        code = "transmitter"
      }
    }
  }
}
