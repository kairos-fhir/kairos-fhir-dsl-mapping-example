package customexport.gemelli

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.Multilingual
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.ValueReference
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a HDRP LaborMapping
 * @author Mike Wähnert
 * @since kairos-fhir-dsl.v.1.52.0, HDRP.v.2025.2.0
 * The first code of each component represents the LaborValue.Code in HDRP. Further codes could be representations in LOINC, SNOMED-CT etc.
 * LaborValueIdContainer in HDRP are just an export example, but not intended to be imported by HDRP FHIR API yet.
 */
observation {

  if ("Gemelli-Procedure" == context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return // exported by procedureLaborMapping.groovy
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      system = FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME
      code = context.source[laborMapping().laborFinding().shortName()] as String
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
    precision = TemporalPrecisionEnum.DAY.name()
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  method {
    coding {
      system = FhirUrls.System.LaborMethod.BASE_URL
      version = context.source[laborMapping().laborFinding().laborMethod().version()]
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
    }
  }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->

    final def laborValue = lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE] // from HDRP.v.2022.3.0

    final String laborValueCode = laborValue?.getAt(CODE) as String
    final String laborValueDisplay = laborValue?.getAt(LaborValue.MULTILINGUALS)?.find { final def ml ->
      ml[Multilingual.LANGUAGE] == "en" && ml[Multilingual.SHORT_NAME] != null
    }?.getAt(Multilingual.SHORT_NAME) as String

    component {
      code {
        coding {
          system = FhirUrls.System.LaborValue.BASE_URL
          code = laborValueCode
          display = laborValueDisplay
        }
        laborValue?.getAt(LaborValue.IDCONTAINERS)?.each { final idContainer ->
          coding {
            system = idContainer[ID_CONTAINER_TYPE]?.getAt(CODE)
            code = idContainer[PSN] as String
          }
        }
      }

      if (isNumeric(laborValue)) {
        valueQuantity {
          value = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
          unit = laborValue?.getAt(LaborValueNumeric.UNIT)?.getAt(CODE) as String
        }
      } else if (isBoolean(laborValue)) {
        valueBoolean(lflv[LaborFindingLaborValue.BOOLEAN_VALUE] as Boolean)
      } else if (isDate(laborValue)) {
        valueDateTime {
          date = lflv[LaborFindingLaborValue.DATE_VALUE]?.getAt(PrecisionDate.DATE)
        }
      } else if (isTime(laborValue)) {
        valueTime(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
      } else if (isString(laborValue)) {
        valueString(lflv[LaborFindingLaborValue.STRING_VALUE] as String)
      } else if (isEnumeration(laborValue)) {
        valueCodeableConcept {
          lflv[LaborFindingLaborValue.MULTI_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/UsageEntry"
              code = entry[CODE] as String
            }
          }
          lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/ValueList-" + entry[CatalogEntry.CATALOG]?.getAt(AbstractCatalog.ID)
              code = entry[CODE] as String
            }
          }
        }
      } else if (isOptionGroup(laborValue)) {
        valueCodeableConcept {
          lflv[LaborFindingLaborValue.MULTI_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/UsageEntry"
              code = entry[CODE] as String
            }
          }
          lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/ValueList-" + entry[CatalogEntry.CATALOG]?.getAt(AbstractCatalog.ID)
              code = entry[CODE] as String
            }
          }
        }
      } else if (isCatalog(laborValue)) {
        valueCodeableConcept {
          lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/ValueList-" + entry[CatalogEntry.CATALOG]?.getAt(AbstractCatalog.ID)
              code = entry[CODE] as String
            }
          }
          lflv[LaborFindingLaborValue.ICD_ENTRY_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/IcdCatalog-" + entry[IcdEntry.CATALOGUE]?.getAt(AbstractCatalog.ID)
              code = entry[CODE] as String
            }
          }
          // example for master data catalog entries of blood group
          lflv[LaborFindingLaborValue.MULTI_VALUE_REFERENCES].each { final entry ->
            final def bloodGroup = entry[ValueReference.BLOOD_GROUP_VALUE]
            if (bloodGroup != null) {
              coding {
                system = FhirUrls.System.Patient.BloodGroup.BASE_URL
                code = bloodGroup?.getAt(CODE) as String
              }
            }

            // example for master data catalog entries of attending doctor
            final def attendingDoctor = entry[ValueReference.ATTENDING_DOCTOR_VALUE]
            if (attendingDoctor != null) {
              coding {
                system = FhirUrls.System.AttendingDoctor.BASE_URL
                // HDRP uses the reference embedded in a coding to support multi selects
                code = "Practitioner/" + attendingDoctor?.getAt(AbstractCatalog.ID) as String
              }
            }
          }
        }
      } else {
        final String msg = laborValue?.getAt(LaborValue.D_TYPE) + " not implemented yet."
        System.out.println(msg)
      }
    }
  }
}

static boolean isDTypeOf(final Object laborValue, final List<LaborValueDType> types) {
  return types.contains(laborValue?.getAt(LaborValue.D_TYPE) as LaborValueDType)
}

static boolean isBoolean(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.BOOLEAN])
}

static boolean isNumeric(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.INTEGER, LaborValueDType.DECIMAL, LaborValueDType.SLIDER])
}

static boolean isDate(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.DATE, LaborValueDType.LONGDATE])
}

static boolean isTime(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.TIME])
}

static boolean isEnumeration(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.ENUMERATION])
}

static boolean isString(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.STRING, LaborValueDType.LONGSTRING])
}

static boolean isCatalog(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.CATALOG])
}

static boolean isOptionGroup(final Object laborValue) {
  return isDTypeOf(laborValue, [LaborValueDType.OPTIONGROUP])
}
