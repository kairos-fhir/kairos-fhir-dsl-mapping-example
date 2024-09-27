package projects.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.OrganisationUnit
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.ValueReference

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX Diagnosis
 * @author Mike Wähnert
 * @since v.1.6.0, CXX.v.3.17.1.7
 */
condition {

  id = "Condition/" + context.source[diagnosis().id()]

  subject {
    reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
  }

  if (!isFakeEpisode(context.source[diagnosis().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[diagnosis().episode().id()]
    }
  }

  final def diagnosisId = context.source[diagnosis().diagnosisId()]
  if (diagnosisId) {
    identifier {
      value = diagnosisId
      type {
        coding {
          system = "urn:centraxx"
          code = "diagnosisId"
        }
      }
    }
  }


  code {
    if (context.source[diagnosis().icdEntry()]) {
      coding {
        system = "https://fhir.centraxx.de/system/" + context.source[diagnosis().icdEntry().catalogue().name()]
        code = context.source[diagnosis().icdEntry().code()] as String
        version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()]
        display = context.source[diagnosis().icdEntry().preferredLong()]
      }
    }

    if (context.source[diagnosis().userDefinedCatalogEntry()]) {
      coding {
        system = "https://fhir.centraxx.de/system/" + context.source[diagnosis().userDefinedCatalogEntry().catalog().code()]
        version = context.source[diagnosis().userDefinedCatalogEntry().catalog().version()]
        code = context.source[diagnosis().userDefinedCatalogEntry().code()] as String
        display = context.source[diagnosis().userDefinedCatalogEntry().nameMultilingualEntries()]?.find { it[LANG] == "en" }?.getAt(VALUE)
      }
    }

    if (context.source[diagnosis().diagnosisCode()]) {
      coding {
        code = context.source[diagnosis().diagnosisCode()] as String
        display = context.source[diagnosis().diagnosisText()]
      }
    }
  }

  final String diagNote = context.source[diagnosis().comments()] as String
  if (diagNote) {
    note {
      text = diagNote
    }
  }

  final def mapping = context.source[diagnosis().laborMappings()].find { final def lm ->
    lm[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "Condition_profile"
  }

  if (mapping) {
    final def lflvOnset = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == "onsetPeriod.start"
    }

    onsetPeriod {
      if (lflvOnset) {
        start {
          date = lflvOnset[LaborFindingLaborValue.DATE_VALUE][PrecisionDate.DATE]
        }
      }

      final def lflvEnd = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
        lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == "onsetPeriod.end"
      }

      if (lflvEnd) {
        end {
          date = lflvEnd[LaborFindingLaborValue.DATE_VALUE][PrecisionDate.DATE]
        }
      }
    }

    final def lflvSpecialism = mapping[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE] == "specialism"
    }

    if (lflvSpecialism) {
      final def valueRef = lflvSpecialism[LaborFindingLaborValue.MULTI_VALUE_REFERENCES].find()
      if (valueRef) {
        extension {
          url = "https://fhir.iqvia.com/patientfinder/extension/specialism-organization"
          valueReference {
            reference = "Organization/" + valueRef[ValueReference.ORGANIZATION_VALUE][OrganisationUnit.ID]
          }
        }
      }
    }
  } else if (context.source[diagnosis().diagnosisDate().date()]) {
    onsetDateTime {
      date = context.source[diagnosis().diagnosisDate().date()]
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }
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
