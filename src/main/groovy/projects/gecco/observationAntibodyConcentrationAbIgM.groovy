package projects.gecco

import de.kairos.centraxx.rest.kmu.virtualslide.xml.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueInteger
import de.kairos.fhir.centraxx.metamodel.RootEntities
import de.kairos.fhir.centraxx.metamodel.Unity
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
 * @since CXX.v.3.18.0
 *
 * Maps the following profile:
 *  - SARS-CoV-2 (COVID-19) IgM Ab [Units/volume] in Serum or Plasma by Immunoassay
 */


observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "COVID_IMMUNOASSAY_PROFILE_CODE") {
    return // no export
  }
  
  final def parameterCode = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "COVID_IGM_AB_CONCENTRATION_IMMUNOASSAY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (parameterCode){
    id = "AbIgMConcentration/" + context.source[laborMapping().laborFinding().id()]
    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sars-cov-2-igm-ser-pl-ia-acnc"
    }

    status = Observation.ObservationStatus.UNKNOWN

    category{
      coding {
        system = "http://loinc.org"
        code = "26436-6"
      }
      coding {
        system = "http://terminology.hl7.org/CodeSystem/observation-category"
        code = "laboratory"
      }
    }
    code {
      coding {
        system = "http://loinc.org"
        code = "94506-3"
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

    effectiveDateTime {
      date = context.source[laborMapping().laborFinding().findingDate().date()]
    }

    valueQuantity{
      value = parameterCode[LaborFindingLaborValue.NUMERIC_VALUE]
      unit = parameterCode[LaborFindingLaborValue.LABOR_VALUE][LaborValueInteger.UNIT][Unity.CODE] as String
      system = "http://unitsofmeasure.org"
      code = "[IU]/mL"
    }

    //If the measurement profile contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
    final def antibodyAnnotation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
      "ANNOTATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
    }
    if (antibodyAnnotation){
      note{
        text = antibodyAnnotation[LaborFindingLaborValue.STRING_VALUE]
      }
    }

    if (context.source[laborMapping().mappingType()] == LaborMappingType.SAMPLELABORMAPPING as String){
      specimen {
        reference = "Specimen/" + context.source[laborMapping().relatedOid()]
      }
    }

    referenceRange {
      low {
        value = parameterCode[LaborFindingLaborValue.LABOR_VALUE][LaborValueInteger.LOWER_VALUE]
      }
      high {
        value = parameterCode[LaborFindingLaborValue.LABOR_VALUE][LaborValueInteger.UPPER_VALUE]
      }
    }
  }
}
