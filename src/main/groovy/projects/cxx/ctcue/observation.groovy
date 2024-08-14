package projects.cxx.ctcue

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.ValueReference
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.2022.2.0
 * TODO: extend example for Enumerations and RadioOptionGroups
 * The first code of each component represents the LaborValue.Code in CXX. Further codes could be representations in LOINC, SNOMED-CT etc.
 * LaborValueIdContainer in CXX are just an export example, but not intended to be imported by CXX FHIR API yet.
 */
observation {
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

  // filter for labor values that are annotated with a LOINC code
  context.source[laborMapping().laborFinding().laborFindingLaborValues()].findAll { final lflv ->
    lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.IDCONTAINERS)?.any { final idc ->
      idc?.getAt(ID_CONTAINER_TYPE)?.getAt(CODE)?.equals("LOINC")
    }
  }.each { final lflv ->
    component {
      code {
        coding {
          system = FhirUrls.System.LaborValue.BASE_URL
          code = lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(CODE) as String
        }
        lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.IDCONTAINERS)?.each { final idContainer ->
          coding {
            system = idContainer[ID_CONTAINER_TYPE]?.getAt(CODE)?.equals("LOINC") ? "http://loinc.org" : idContainer[ID_CONTAINER_TYPE]?.getAt(CODE)
            code = idContainer[PSN] as String
          }
        }
      }


      if (isNumeric(lflv)) {
        valueQuantity {
          value = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
          unit = lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValueNumeric.UNIT)?.getAt(CODE) as String
        }
      }
      if (isBoolean(lflv)) {
        valueBoolean(lflv[LaborFindingLaborValue.BOOLEAN_VALUE] as Boolean)
      }

      if (isDate(lflv)) {
        valueDateTime {
          date = lflv[LaborFindingLaborValue.DATE_VALUE]?.getAt(PrecisionDate.DATE)
        }
      }

      if (isTime(lflv)) {
        valueTime(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
      }

      if (isString(lflv)) {
        valueString(lflv[LaborFindingLaborValue.STRING_VALUE] as String)
      }

      if (isCatalog(lflv)) {
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
                // CXX uses the reference embedded in a coding to support multi selects
                code = "Practitioner/" + attendingDoctor?.getAt(AbstractCatalog.ID) as String
              }
            }
          }
        }
      }
    }
  }
}

private static boolean isDTypeOf(final Object lflv, final List<LaborValueDType> types) {
  return types.contains(lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as LaborValueDType)
}

static boolean isBoolean(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.BOOLEAN])
}

static boolean isNumeric(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.INTEGER, LaborValueDType.DECIMAL, LaborValueDType.SLIDER])
}


static boolean isDate(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.DATE, LaborValueDType.LONGDATE])
}

static boolean isTime(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.TIME])
}

static boolean isEnumeration(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.ENUMERATION])
}

static boolean isString(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.STRING, LaborValueDType.LONGSTRING])
}

static boolean isCatalog(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.CATALOG])
}

static boolean isOptionGroup(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.OPTIONGROUP])
}
