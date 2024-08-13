package projects.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.ValueReference
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractCodeName.NAME_MULTILINGUAL_ENTRIES
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Mike Wähnert
 * @since kairos-fhir-dsl.v.1.12.0, CXX.v.3.18.1.19, CXX.v.3.18.2
 * The first code of each component represents the LaborValue.Code in CXX. Further codes could be representations in LOINC, SNOMED-CT etc.
 * LaborValueIdContainer in CXX are just an export example, but not intended to be imported by CXX FHIR API yet.
 */
observation {

  final def laborMethod = context.source[laborMapping().laborFinding().laborMethod()]

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      system = FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME
      code = context.source[laborMapping().laborFinding().shortName()] as String
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source[laborMapping().laborFinding().findingDate().date()] as String)
    precision = TemporalPrecisionEnum.DAY.toString()
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  if (context.source[laborMapping().mappingType()] as LaborMappingType == LaborMappingType.SAMPLELABORMAPPING){
    specimen {
      reference = "Specimen/" + context.source[laborMapping().relatedOid()]
    }
  }


  method {
    coding {
      system = FhirUrls.System.LaborMethod.BASE_URL
      version = context.source[laborMapping().laborFinding().laborMethod().version()]
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
    }
  }

  if (laborMethod[CODE].equals("SACT_Profile")) {
    basedOn {
      reference = "CarePlan/SACT-" + context.source[laborMapping().laborFinding().id()]
    }
    partOf {
      reference = "MedicationAdministration/SACT-" + context.source[laborMapping().laborFinding().id()]
    }
  }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->

    final def laborValue = lflv[LaborFindingLaborValue.LABOR_VALUE] != null
        ? lflv[LaborFindingLaborValue.LABOR_VALUE] // before CXX.v.2022.3.0
        : lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE] // from CXX.v.2022.3.0

    final String laborValueCode = laborValue?.getAt(CODE) as String

    if (laborMethod[CODE].equals("SACT_Profile") && isMappedElseWhere(laborValueCode)) {
      return
    }


    final String laborValueDisplay = laborValue?.getAt(NAME_MULTILINGUAL_ENTRIES)?.find { final mle -> mle[LANG] == "en" }?.getAt(VALUE) as String

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
            display = idContainer[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
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
              display = entry[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
            }
          }
          lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/ValueList-" + entry[CatalogEntry.CATALOG]?.getAt(AbstractCatalog.ID)
              code = entry[CODE] as String
              display = entry[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
            }
          }
        }
      } else if (isOptionGroup(laborValue)) {
        valueCodeableConcept {
          lflv[LaborFindingLaborValue.MULTI_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/UsageEntry"
              code = entry[CODE] as String
              display = entry[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
            }
          }
          lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/ValueList-" + entry[CatalogEntry.CATALOG]?.getAt(AbstractCatalog.ID)
              code = entry[CODE] as String
              display = entry[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
            }
          }
        }
      } else if (isCatalog(laborValue)) {
        valueCodeableConcept {
          lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/ValueList-" + entry[CatalogEntry.CATALOG]?.getAt(AbstractCatalog.ID)
              code = entry[CODE] as String
              display = entry[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
            }
          }
          lflv[LaborFindingLaborValue.ICD_ENTRY_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/IcdCatalog-" + entry[IcdEntry.CATALOGUE]?.getAt(AbstractCatalog.ID)
              code = entry[CODE] as String
              display = entry[IcdEntry.PREFERRED_LONG] as String
            }
          }
          // example for master data catalog entries of blood group
          lflv[LaborFindingLaborValue.MULTI_VALUE_REFERENCES].each { final entry ->
            final def bloodGroup = entry[ValueReference.BLOOD_GROUP_VALUE]
            if (bloodGroup != null) {
              coding {
                system = FhirUrls.System.Patient.BloodGroup.BASE_URL
                code = bloodGroup?.getAt(CODE) as String
                display = entry[NAME_MULTILINGUAL_ENTRIES]?.find { it[LANG] == "en" }?.getAt(VALUE) as String
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

static boolean isFakeEpisode(final def episode) {
  if (episode == null) {
    return true
  }

  if (["SACT", "COSD"].contains(episode[Episode.ENTITY_SOURCE])) {
    return true
  }

  final def fakeId = episode[Episode.ID_CONTAINER]?.find { (it[PSN] as String).toUpperCase().startsWith("FAKE") }
  return fakeId != null
}

static boolean isMappedElseWhere(final String code) {
  final boolean mappedInCarePlan = mappedInCarePlan(code)
  final boolean mappedInMedAdmin = mappedInMedAdmin(code)
  return mappedInCarePlan || mappedInMedAdmin
}

private static boolean mappedInMedAdmin(final String code) {
  final List mappedInMedAdmin = ["Drug_Name",
                                 "DM+D",
                                 "Actual_Dose_Per_Administration",
                                 "Unit_Of_Measurement_(SNOMED_CT_DM+D)",
                                 "SACT_Administration_Route",
                                 "Route_Of_Administration_(SNOMED_CT_DM+D)",
                                 "Administration_Date"]
  return code in mappedInMedAdmin
}

private static boolean mappedInCarePlan(final String code) {
  final List mappedInCarePlan = ["Regimen", "Date_Decision_To_Treat", "Start_Date_Of_Regimen"]
  return code in mappedInCarePlan
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}