package projects.gecco

import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueInteger
import de.kairos.fhir.centraxx.metamodel.RootEntities
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.CanonicalType
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.IntegerType
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.SimpleQuantity

import javax.persistence.criteria.Root

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since CXX.v.3.18.0
 */

observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "HISTORY_OF_VACCINATION_PROFILE_CODE") {
    return // no export
  }

  id = "HistoryOfVaccination/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/immunization"
  }

  final def vaccCodeLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "HISTORY_OF_VACCINATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (vaccCodeLfLv){
    valueCodeableConcept {
      vaccCodeLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
        coding {
          code = entry[CatalogEntry.CODE] as String
        }
      }
    }
  }

  patient{
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }
  encounter {
    reference = "Episode/" + context.source[laborMapping().episode().id()]
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  //If the measurement profile contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
  final def histOfVaccAnnotation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "ANNOTATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (histOfVaccAnnotation){
    note{
      text = histOfVaccAnnotation[LaborFindingLaborValue.STRING_VALUE]
    }
  }

  final def targetDisLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "VACCINE_TARGET_DISEASE_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (targetDisLfLv){
    code{
      targetDisLfLv[LaborFindingLaborValue.ICD_ENTRY_VALUE].each { final entry ->
        coding {
          system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
          code = entry[IcdEntry.CODE] as String
        }
      }
    }
  }

}
