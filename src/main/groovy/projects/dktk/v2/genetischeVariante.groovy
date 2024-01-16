package projects.dktk.v2

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * Specified by https://simplifier.net/oncology/genetischevariantecs
 * @author Mike Wähnert
 * @since kairos-fhir-dsl.v.1.12.0, CXX.v.3.18.1.19, CXX.v.3.18.2
 *
 * Based on the measurement profile, which has been specified by the CCP-IT group in ../xml/masterdata_molecularmarker.xml
 * It does not cover the specified FHIR observation component slices like Amino-acid-change, DNA-change, etc.
 * It does not specify any LOINC codes.
 * this scrip assumes that the measurement parameter
 * * CCP_MOLECULAR_MARKER_STATUS represents GenetischeVarianteCS
 * * CCP_MOLECULAR_MARKER_NAME represents a name from http://www.genenames.org
 */
observation {

  if ("CCP_MOLECULAR_MARKER" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
    return
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://hl7.org/fhir/observation-category"
      code = "laboratory"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "69548-6"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  final String focusReference = getFocusReference(
      context.source[laborMapping().mappingType()] as LaborMappingType,
      context.source[laborMapping().relatedOid()] as String)

  if (focusReference != null) {
    focus {
      reference = focusReference
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source[laborMapping().laborFinding().findingDate().date()] as String)
  }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->

    final def laborValue = lflv[LaborFindingLaborValue.LABOR_VALUE] != null
        ? lflv[LaborFindingLaborValue.LABOR_VALUE] // before CXX.v.2022.3.0
        : lflv["crfTemplateField"][CrfTemplateField.LABOR_VALUE] // from CXX.v.2022.3.0

    final String laborValueCode = laborValue?.getAt(CODE) as String

    if (laborValueCode.equalsIgnoreCase("CCP_MOLECULAR_MARKER_STATUS")) {
      valueCodeableConcept {
        coding {
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GenetischeVarianteCS"
          code = lflv[LaborFindingLaborValue.STRING_VALUE] as String
        }
      }
    }

    final String loincCode = getLoincCode(laborValueCode)


    component {
      code {
        if (loincCode != null) {
          coding {
            system = "http://loinc.org"
            code = loincCode
          }
        }
        coding { // CXX code as a second coding is exported, if no loinc code exists.
          system = FhirUrls.System.LaborValue.BASE_URL
          code = laborValueCode
        }
      }

      if (laborValueCode == "CCP_MOLECULAR_MARKER_NAME") {
        valueCodeableConcept {
          coding {
            system = "http://www.genenames.org"
            code = lflv[LaborFindingLaborValue.STRING_VALUE] as String
          }
        }
      } else if (laborValueCode == "CCP_MOLECULAR_MARKER_DATE") {
        valueDateTime {
          date = normalizeDate(lflv[LaborFindingLaborValue.DATE_VALUE]?.getAt(PrecisionDate.DATE) as String)
        }
      } else { // other component are strings
        valueString(lflv[LaborFindingLaborValue.STRING_VALUE] as String)
      }
    }
  }
}

static boolean isDTypeOf(final Object laborValue, final List<LaborValueDType> types) {
  return types.contains(laborValue?.getAt(LaborValue.D_TYPE) as LaborValueDType)
}

static String getFocusReference(final LaborMappingType mappingType, final String relatedOid) {
  if (LaborMappingType.PATIENTLABORMAPPING == mappingType) return "Patient/" + relatedOid
  else if (LaborMappingType.SAMPLELABORMAPPING == mappingType) return "Specimen/" + relatedOid
  else return null
}

static String getLoincCode(final String laborValueCode) {
  if (laborValueCode.equalsIgnoreCase("CCP_MOLECULAR_MARKER_NAME")) return "48018-6" // GeneNomenclature
  // TODO: with the current profile is no mapping possible
  else if (laborValueCode.equalsIgnoreCase("Amino-acid-change")) return "48005-3"
  else if (laborValueCode.equalsIgnoreCase("DNA-change")) return "81290-9"
  else if (laborValueCode.equalsIgnoreCase("RefSeq-NCBI")) return "81248-7"
  else if (laborValueCode.equalsIgnoreCase("Ensembl-ID")) return "81249-5"
  else return null
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}