package projects.gecco


import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.DateTimeType

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since CXX.v.3.18.1*
 * Maps the following profile:
 *  - Pharmacological Therapy Immunoglobulins
 */


medicationStatement {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "COVID_IMMUNOGLOBULINS_PROFILE_CODE") {
    return // no export
  }

  id = "Immunoglobulins/" + context.source[laborMapping().laborFinding().id()]
  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy-immunoglobulins"
  }

  final def medicationStatus = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_MEDICATION_STATUS_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  medicationStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
    status = entry[CatalogEntry.CODE] as String
  }

  final def medicationCode = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_IMMUNOGLOBULINS_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  medicationCodeableConcept {
    medicationCode[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
      coding {
        system = "http://fhir.de/CodeSystem/dimdi/atc"
        code = medicationCode[CatalogEntry.CODE] as String
      }
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }
  final def episodeID = context.source[laborMapping().episode().id()]
  if (episodeID) {
    context_ {
      reference = "Episode/" + episodeID
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }


  final def dosageText = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_DOSAGE_TEXT_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  final def timingCode = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_MEDICATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  final def timingEvent = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_MEDICATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  dosage {
    text = dosageText[LaborFindingLaborValue.STRING_VALUE] as String

    timing {
      timingCode[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
        code = entry[LaborFindingLaborValue.CATALOG_ENTRY_VALUE] as CodeableConcept
      }
      event = timingEvent[LaborFindingLaborValue.DATE_VALUE] as List<DateTimeType>
    }

  }

}
