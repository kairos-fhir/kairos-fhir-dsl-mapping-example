package projects.uscore

import de.kairos.fhir.centraxx.metamodel.AttendingDoctor
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.MasterDataCatalogEntry
import de.kairos.fhir.centraxx.metamodel.ValueReference
import org.hl7.fhir.r4.model.CareTeam

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a CXX LaborMapping for the US Core Resource Profile: US Core CarePlan Profile.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-careteam.html
 *
 * The Script works with a CentraXX measurement profile with the code US_CORE_CARE_TEAM
 * that has a measurement parameter, which is a single choice from a master data catalog of CentraXX doctors.
 * The master data definition for this profile can be found in xml/careTeam.xml and can be imported over
 * the CentraXX XML import interface
 *
 * TODO: Export von MasterDataEntries
 *
 * @author Mike Wähnert, Jonas Küttner
 * @since v.1.14.0, CXX.v.2022.1.0
 */
careTeam {

  if ("US_CORE_CARE_TEAM" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "CareTeam/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-careteam")
  }

  final def lblvCareTeamStatus = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "FHIR_CARE_TEAM_STATUS" }

  status = CareTeam.CareTeamStatus.fromCode((lblvCareTeamStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE] as List)[0][CODE] as String)

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  final def lblvMembers = context.source[laborMapping().laborFinding().laborFindingLaborValues()]
      .find { final lblv -> lblv[LABOR_VALUE][CODE] == "US_CORE_CARE_TEAM" }


  final def combinedList = []
  lblvMembers[LaborFindingLaborValue.MULTI_VALUE_REFERENCES].each { final entry ->
    combinedList.add(entry)
  }
  lblvMembers[LaborFindingLaborValue.MASTER_DATA_CATALOG_ENTRY_VALUE].each { final entry ->
    combinedList.add(entry[MasterDataCatalogEntry.VALUE_REFERENCE])
  }

  combinedList.each { final mdce ->
    participant {
      role {
        coding {
          system = "http://hl7.org/fhir/StructureDefinition/data-absent-reason"
          code = "unknown"
          display = "unknown"
        }
      }
      member {
        reference = "Practitioner/" + mdce[ValueReference.ATTENDING_DOCTOR_VALUE][AttendingDoctor.ID]
      }
    }
  }
}
