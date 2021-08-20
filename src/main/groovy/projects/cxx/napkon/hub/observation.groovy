package projects.cxx.napkon.hub


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
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.BOOLEAN_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.DATE_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.NUMERIC_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.STRING_VALUE
import static de.kairos.fhir.centraxx.metamodel.RecordedValue.TIME_VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
/**
 * Represented by a CXX LaborMapping
 * @author Jonas Küttner, Mike Wähnert
 * @since v.1.10.0, CXX.v.3.18.2, CXX.v.3.18.1.8
 *
 * The mapping transforms specimen from the HUB Hannover system to the DZHK Greifswald system.
 * Script to extract measurement results that contain only simple data types and single / multiple selections from controlled vocabulary.
 * Based on the assumption that the measurement profiles (LaborMethods), measurement parameters (LaborValues) and the associated controlled vocabulary are
 * defined with the same codes in both CXX instances. In this case, only one mapping to the oid of the value list in the target system is required
 * for the import.
 */
observation {
  def isSampleMapping = LaborMappingType.SAMPLELABORMAPPING == context.source[laborMapping().mappingType()] as LaborMappingType
  def isDzhkRelevant = ["DZHKFLAB", "NUM_PBMC_ISOLIERUNG", "NUM_BAL", "SPECIAL_ONE"].contains(context.source[laborMapping().laborFinding().laborMethod().code()])
  if (!(isSampleMapping && isDzhkRelevant)) {
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
    "LIMSPSN" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  if (patIdContainer) {
    subject {
      identifier {
        value = patIdContainer[IdContainer.PSN]
        type {
          coding {
            system = "urn:centraxx"
            code = patIdContainer[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE) as String
          }
        }
      }
    }
  }

  if (context.source[laborMapping().sample()] != null) {
    // Reference by identifier SampleId, because parent MasterSample might already exists in the target system
    // The external sample id of HUB is provided as sample id to DZHK.
    final def extSampleId = context.source[laborMapping().sample().idContainer()]?.find { final def entry ->
      "EXTSAMPLEID" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
    }

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


      if (labFinLabVal[LaborFindingLaborValue.MULTI_VALUE]){
        valueCodeableConcept {
          labFinLabVal[LaborFindingLaborValue.MULTI_VALUE].each {final def entry ->
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

