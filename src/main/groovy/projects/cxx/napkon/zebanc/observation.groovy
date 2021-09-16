package projects.cxx.napkon.zebanc

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.RootEntities
import de.kairos.fhir.centraxx.metamodel.SampleIdContainer
import de.kairos.fhir.centraxx.metamodel.Unity
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.SampleCategory
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.BOOLEAN_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.DATE_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.NUMERIC_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.STRING_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.TIME_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.CATALOG_ENTRY_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX LaborMapping
 * @author Jonas Küttner, Mike Wähnert
 * @since v.1.8.0, CXX.v.3.8.1.1
 *
 * Script to extract measurement results that contain only simple data types and single / multiple selections from value lists.
 * Based on the assumption that the measurement profiles (LaborMethods), measurement parameters (LaborValues) and the associated value lists are
 * defined with the same codes in both CXX instances. In this case, only one mapping to the oid of the value list in the target system is required
 * for the import.
 */
observation {

  final boolean isSampleMapping = LaborMappingType.SAMPLELABORMAPPING == context.source[laborMapping().mappingType()] as LaborMappingType
  final boolean isDzhkMethod = ["DZHKFLAB"].contains(context.source[laborMapping().laborFinding().laborMethod().code()])
  if (!(isSampleMapping && isDzhkMethod)) {
    return
  }

  final def extSampleId = context.source[laborMapping().sample().idContainer()]?.find { final def entry ->
    "NAPKONProbenID" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  // Filter master samples of HEPFIX and its family - in test was _BB
  final SampleCategory category = context.source[sample().sampleCategory()] as SampleCategory
  if ((category == SampleCategory.MASTER && !extSampleId) || (extSampleId && (extSampleId?.getAt(SampleIdContainer.PSN) as String).contains("_BB"))) {
    return
  }

  String napkonId = extSampleId?.getAt(SampleIdContainer.PSN) as String
  if (napkonId && napkonId.length() == 20) {
    napkonId = napkonId.substring(0, 10)
  }

  // Filter HEPFIX master sample children
  if (context.source[sample().sampleType().code()] == "HEPARINFIXATED") {
    return
  }

  id = "Observation/" + context.source[laborMapping().id()]
  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      system = "urn:centraxx"
      code = context.source[laborMapping().laborFinding().shortName()] as String
    }
  }

  final def patIdContainer = context.source[laborMapping().relatedPatient().idContainer()]?.find {
    "NAPKONPATID" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
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
          value = napkonId
        }
      }
    }
  }

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
