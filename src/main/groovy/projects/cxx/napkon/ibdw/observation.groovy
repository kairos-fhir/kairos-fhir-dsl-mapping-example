package projects.cxx.napkon.ibdw

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.SampleIdContainer
import de.kairos.fhir.centraxx.metamodel.Unity
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.SampleCategory
import org.hl7.fhir.r4.model.Observation

import java.text.SimpleDateFormat

import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.BOOLEAN_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.CATALOG_ENTRY_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.DATE_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.NUMERIC_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.STRING_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.TIME_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
/**
 * Represented by a CXX LaborMapping
 * @author Jonas Küttner, Mike Wähnert
 * @since v.1.8.0, CXX.v.3.18.1.1
 *
 * Script to extract measurement results that contain only simple data types and single / multiple selections from value lists.
 * Based on the assumption that the measurement profiles (LaborMethods), measurement parameters (LaborValues) and the associated value lists are
 * defined with the same codes in both CXX instances. In this case, only one mapping to the oid of the value list in the target system is required
 * for the import.
 */
observation {
  // 0. Filter patients
//  final def patientList = ["lims_768700553"] //, "lims_745748710"]
//  final def currentPatientId = context.source[laborMapping().relatedPatient().idContainer()]?.find {
//    "NAPKON" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
//  }
//  final boolean belongsToExcludedPatient = patientList.contains(currentPatientId[IdContainer.PSN])
//
//  if (!belongsToExcludedPatient) {
//    return
//  }

  // Filter sample category
  final SampleCategory category = context.source[laborMapping().sample().sampleCategory()] as SampleCategory

  final boolean isSampleMapping = LaborMappingType.SAMPLELABORMAPPING == context.source[laborMapping().mappingType()] as LaborMappingType
  final boolean isNumMethod = ["DZHKFLAB", 'NUM_PBMC_ISOLIERUNG'].contains(context.source[laborMapping().laborFinding().laborMethod().code()])
  if (!(isSampleMapping && isNumMethod)) {
    return
  }
  
//  final SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
//  final SimpleDateFormat numDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss")

  // 2. Filter NAPKON-ID Mapping exists
  String napkonMappingExists = ""
  if (category == SampleCategory.MASTER) {
    napkonMappingExists = context.source[laborMapping().sample().idContainer()]?.find { final def entry ->
      "NAPKONSMP" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
    }
  }
    final def extSampleId = context.source[laborMapping().sample().idContainer()]?.find { final def entry ->
    "NAPKONSMP" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }
  
  if (!napkonMappingExists) {
    return
  }

  // Filter samples older than specified date
//  String receptionDate = ""
//  if (category == SampleCategory.MASTER) {
//    receptionDate = context.source[laborMapping().sample().receiptDate().date()]
//  } else if (category == SampleCategory.ALIQUOTGROUP) {
//    receptionDate = context.source[laborMapping().sample().parent().receiptDate().date()]
//  } else if (category == SampleCategory.DERIVED) {
//    receptionDate = context.source[laborMapping().sample().parent().parent().receiptDate().date()]
//  }
//  if (receptionDate == null || receptionDate < "2023-01-01T00:00:00.000+01:00") {
//    return
//  }

//  println("observationDate: " + context.source[laborMapping().laborFinding().findingDate().date()])
//  Date observationDate =  isoDateFormat.parse(context.source[laborMapping().laborFinding().findingDate().date()] as String)
//  String observationCode = ""
//  if (observationDate) {
//  	observationCode = "Biomaterial-Zentrifugation_" + extSampleId[SampleIdContainer.PSN] + "_" + numDateFormat.format(observationDate)
//  }
//  else {
//	// Do not create observation if observationDate is NULL
//	return
//  }

  // Update resource - ignore missing elements
//  extension {
//    url = FhirUrls.Extension.UPDATE_WITH_OVERWRITE
//    valueBoolean = false
//  }

  id = "Observation/" + context.source[laborMapping().id()]

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
//	  code = observationCode
	  code = context.source[laborMapping().laborFinding().shortName()] as String
      system = "urn:centraxx"
    }
  }
  
  final def patIdContainer = context.source[laborMapping().relatedPatient().idContainer()]?.find {
    "NAPKON" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  if (patIdContainer) {
    subject {
      identifier {
        value = patIdContainer[IdContainer.PSN]
        type {
          coding {
            system = "urn:centraxx"
            code = "LIMSPSN"
          }
        }
      }
    }
  }

  if (context.source[laborMapping().sample()] != null) {
    // Reference by identifier SampleId, because parent MasterSample might already exists in the target system
    // The napkon sample id of BB is provided as sample id to DZHK.
    if (extSampleId) {
      specimen {
        identifier {
          type {
            coding {
              code = "SAMPLEID"
            }
          }
          value = extSampleId[SampleIdContainer.PSN]
        }
      }
    }
  }

//  effectiveDateTime = observationDate
  effectiveDateTime = context.source[laborMapping().laborFinding().findingDate().date()]


  method {
    coding {
      system = "urn:centraxx"
      version = context.source[laborMapping().laborFinding().laborMethod().version()]
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
    }
  }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final def labFinLabVal ->
    component {
      code {
        coding {
          system = "urn:centraxx"
          code = labFinLabVal[LABOR_VALUE][LaborValue.CODE] as String
        }
      }
      if (labFinLabVal[NUMERIC_VALUE]) {
        valueQuantity {
          value = labFinLabVal[NUMERIC_VALUE]
          unit = labFinLabVal[LABOR_VALUE]?.getAt(LaborValueNumeric.UNIT)?.getAt(Unity.CODE) as String
        }
      }

      if (labFinLabVal[STRING_VALUE]) {
        valueString(labFinLabVal[STRING_VALUE] as String)
      }

      if (labFinLabVal[STRING_VALUE]) {
        valueQuantity {
          value = labFinLabVal[STRING_VALUE]
        }
      }

      if (labFinLabVal[DATE_VALUE]) {
        valueDateTime {
          date = labFinLabVal[DATE_VALUE]?.getAt(PrecisionDate.DATE)
        }
      }

      if (labFinLabVal[TIME_VALUE]) {
        valueTime(labFinLabVal[TIME_VALUE] as String)
      }

      if (labFinLabVal[BOOLEAN_VALUE]) {
        valueBoolean(labFinLabVal[BOOLEAN_VALUE] as Boolean)
      }

      if (labFinLabVal[CATALOG_ENTRY_VALUE]) {
        valueCodeableConcept {
          labFinLabVal[CATALOG_ENTRY_VALUE].each { final def entry ->
            coding {
              system = "urn:centraxx:CodeSystem/UsageEntry-" + entry[CatalogEntry.ID]
              code = entry[CatalogEntry.CODE] as String
            }
          }
        }
      }
      if (labFinLabVal[LaborFindingLaborValue.MULTI_VALUE]) {
        valueCodeableConcept {
          labFinLabVal[LaborFindingLaborValue.MULTI_VALUE].each { final def entry ->
            coding {
              system = "urn:centraxx:CodeSystem/UsageEntry-" + entry[UsageEntry.ID]
              code = entry[UsageEntry.CODE] as String
            }
          }
        }
      }
    }
  }
}