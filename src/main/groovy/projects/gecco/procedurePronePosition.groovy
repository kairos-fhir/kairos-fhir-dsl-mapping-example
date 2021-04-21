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
 * @since CXX.v.3.18.1
 *
 * Maps the following profile:
 *  - Prone Position
 */


procedure {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "PRONE_POSITION_PROFILE_CODE") {
    return // no export
  }

  final def pronePosition = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "PRONE_POSITION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (!pronePosition){
    return // no export
  }

  extension {
    url = "https://simplifier.net/packages/de.medizininformatikinitiative.kerndatensatz.prozedur/1.0.6/files/351664"
    valueDateTime = context.source[laborMapping().laborFinding().creationDate()]
  }

  id = "PronePosition/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/prone-position"
  }

  final def eventStatus = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_EVENT_STATUS_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  eventStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
    status = entry[CatalogEntry.CODE] as String
  }

  category {
    coding {
      system = "http://snomed.info/sct"
      code = "225287004"
    }
  }

  code {
    coding {
      system = "http://snomed.info/sct"
      code = "431182000"
    }
  }

  subject{
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }
  final def episodeID = context.source[laborMapping().episode().id()]
  if (episodeID){
    encounter {
      reference = "Episode/" + episodeID
    }
  }

  final def performedDate = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "PERFORMED_DATE_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (performedDate){
    performedDateTime {
      performedDateTime = performedDate[LaborFindingLaborValue.DATE_VALUE]
    }
  }

//If the measurement profile contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
  final def pHAnnotation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "ANNOTATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (pHAnnotation){
    note{
      text = pHAnnotation[LaborFindingLaborValue.STRING_VALUE]
    }
  }























}
