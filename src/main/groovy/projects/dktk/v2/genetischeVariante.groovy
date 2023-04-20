package projects.dktk.v2


import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
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
 * TODO:
 *  Set codes of CXX LaborMethod and LaborValues for the values to export
 *  The script assumes that all values are stored with LaborValueType String. If other types are needed, the export has to be extended.
 */
observation {

  // TODO: set code of mole marker lab method
  if ("MolMarker" != context.source[laborMapping().laborFinding().laborMethod().code()]) {
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

  if (context.source[laborMapping().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[laborMapping().episode().id()]
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->

    final def laborValue = lflv[LaborFindingLaborValue.LABOR_VALUE] != null
        ? lflv[LaborFindingLaborValue.LABOR_VALUE] // before CXX.v.2022.3.0
        : lflv["crfTemplateField"][CrfTemplateField.LABOR_VALUE] // from CXX.v.2022.3.0

    final String laborValueCode = laborValue?.getAt(CODE) as String

    if (laborValueCode.equalsIgnoreCase("Genetische Variante")) {//TODO
      valueCodeableConcept {
        coding {
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GenetischeVarianteCS"
          code = lflv[LaborFindingLaborValue.STRING_VALUE] as String
        }
      }
    }


    final String loincCode = getLoincCode(laborValueCode)
    if (loincCode == null) {
      return
    }

    component {
      code {
        coding {
          system = "http://loinc.org"
          code = loincCode
        }
      }

      if (laborValueCode == "GeneNomenclature") { //TODO set code
        valueCodeableConcept {
          coding {
            system = "http://www.genenames.org"
            code = lflv[LaborFindingLaborValue.STRING_VALUE] as String
          }
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
  else if (LaborMappingType.DIAGNOSIS == mappingType) return "Condition/" + relatedOid
  else if (LaborMappingType.EPISODE == mappingType) return "Encounter/" + relatedOid
  else return null;
}

static String getLoincCode(final String laborValueCode) {
  if (laborValueCode.equalsIgnoreCase("GeneNomenclature")) return "48018-6"
  else if (laborValueCode.equalsIgnoreCase("Amino-acid-change")) return "48005-3"
  else if (laborValueCode.equalsIgnoreCase("DNA-change")) return "81290-9"
  else if (laborValueCode.equalsIgnoreCase("RefSeq-NCBI")) return "81248-7"
  else if (laborValueCode.equalsIgnoreCase("Ensembl-ID")) return "81249-5"
  else return null
}