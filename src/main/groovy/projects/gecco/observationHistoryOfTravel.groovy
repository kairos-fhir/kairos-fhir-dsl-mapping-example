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
import org.eclipse.birt.chart.script.api.data.IDateTimeDataElement
import org.eclipse.birt.chart.util.CDateTime
import org.hl7.fhir.r4.model.CanonicalType
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.IntegerType
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.SimpleQuantity
import org.hl7.fhir.r4.model.StringType

import javax.persistence.criteria.Root

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since v.1.7.0, CXX.v.3.18.0
 */
observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "HISTORY_OF_TRAVEL_PROFILE_CODE") {
    return // no export
  }

  id = "HistoryOfTravel/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/history-of-travel"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category{
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "social-history"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "8691-8"
    }
  }

  subject{
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }
  encounter {
    reference = "Episode/" + context.source[laborMapping().episode().id()]
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  final def hotLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
     "YES_NO_UNKNOWN_OTHER_NOTAPPLICABLE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (hotLfLv){
    valueCodeableConcept{
      hotLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
        coding {
          system = "http://snomed.info/sct"
          code = entry[CatalogEntry.CODE] as String
        }
      }
    }
  }


  //TravelStartDate
  final def tsdHotLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "TRAVEL_START_DATE_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (tsdHotLfLv){
    component{
      code{
        coding{
          system = "http://loinc.org"
          code = "82752-7"
          display = "Date travel started"
        }
      }
      valueDateTime {
        date = tsdHotLfLv[LaborFindingLaborValue.DATE_VALUE]
      }
    }
  }

  //Country
  final def countryHotLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COUNTRY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (countryHotLfLv){
    component{
      code{
        coding{
          system = "http://loinc.org"
          code = "94651-7"
          display = "Country of travel"
        }
      }
      valueCodeableConcept {
        countryHotLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          coding {
            system = "Iso3166-1-2"
            code = entry[CatalogEntry.CODE] as String
          }
        }
      }
    }
  }

  //State
  final def stateHotLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "STATE_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (stateHotLfLv){
    component{
      code{
        coding{
          system = "http://loinc.org"
          code = "82754-3"
          display = "State of travel"
        }
      }
      valueCodeableConcept {
        stateHotLfLv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
          coding {
            system = "ISO31662DE"
            code = entry[CatalogEntry.CODE] as String
          }
        }
      }
    }
  }

  //City
  final def cityHotLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "CITY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (cityHotLfLv){
    component{
      code{
        coding{
          system = "http://loinc.org"
          code = "94653-3"
          display = "City of travel"
        }
      }
      valueString(cityHotLfLv[LaborFindingLaborValue.STRING_VALUE] as String)
    }
  }

/*
  //TravelEndDate
  final def tedHotLfLv = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "TRAVEL_END_DATE_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (tedHotLfLv){
    component{
      code{
        coding{
          system = "http://loinc.org"
          code = "91560-3"
          display = "Date of departure from travel destination"
        }
      }
      valueDateTime {
        date = tedHotLfLv[LaborFindingLaborValue.TIME_VALUE]
      }
    }
  }
*/
}
