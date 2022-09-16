package projects.uscore

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborValueCatalog
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.Unity
import de.kairos.fhir.centraxx.metamodel.enums.LaborMethodCategory
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractCatalog.CATALOGUE_VERSION
import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractCodeName.NAME_MULTILINGUAL_ENTRIES
import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ID
import static de.kairos.fhir.centraxx.metamodel.CatalogEntry.CATALOG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.OpsEntry.CATALOGUE
import static de.kairos.fhir.centraxx.metamodel.OpsEntry.PREFERRED
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFindingLaborValue

/**
 * Represents a CXX LaborFindingLaborValue that is part of a LaborFinding of Category LABOR.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-observation-lab.html
 *
 * Mapping uses CentraXX code system, since LOINC is not explicitly supported by CentraXX
 * and UCUM can not be set in UI.
 *
 * hint:
 * Does not export LFLV of Type masterDataCatalogEntry
 *
 * @author Jonas Küttner
 * @since v.1.13.0, CXX.v.2022.1.0
 *
 *
 */
final def lang = "de"
observation {
  if (context.source[laborFindingLaborValue().laborFinding().laborMethod().category()] != LaborMethodCategory.LABOR.toString()) {
    return
  }

  final boolean isUsCore = context.source[laborFindingLaborValue().laborValue().idContainers()]?.any {
    final def idc -> idc[IdContainer.ID_CONTAINER_TYPE][CODE].equals("US_CORE_FHIR_EXPORT_DISCRIMINATOR")
  }

  if (isUsCore) {
    return
  }

  id = "Observation/" + context.source[laborFindingLaborValue().id()]
  language = lang
  status = Observation.ObservationStatus.FINAL

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-lab")
  }

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "laboratory"
    }
  }

  code {
    coding {
      system = FhirUrls.System.LaborValue.BASE_URL
      code = context.source[laborFindingLaborValue().laborValue().code()] as String
      display = context.source[laborFindingLaborValue().laborValue().nameMultilingualEntries()]
          .find { final me -> me[LANG] == lang }?.getAt(VALUE)
    }
    text = context.source[laborFindingLaborValue().laborValue().descMultilingualEntries()]
        .find { final me -> me[LANG] == lang }?.getAt(VALUE) as String

  }

  final def laborMappingWithRelatedPatient = context.source[laborFindingLaborValue().laborFinding().laborMappings()].find {
    it[LaborMapping.RELATED_PATIENT] != null
  }

  if (laborMappingWithRelatedPatient) {
    subject {
      reference = "Patient/" + laborMappingWithRelatedPatient[LaborMapping.RELATED_PATIENT][ID]
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source[laborFindingLaborValue().creationDate()] as String)
  }

  final def laborValue = context.source[laborFindingLaborValue().laborValue()]

  final def numericValue = context.source[laborFindingLaborValue().numericValue()]
  final def stringValue = context.source[laborFindingLaborValue().stringValue()]
  final def dateValue = context.source[laborFindingLaborValue().dateValue()]
  final def boolValue = context.source[laborFindingLaborValue().booleanValue()]
  final def timeValue = context.source[laborFindingLaborValue().timeValue()]

  //USAGE ENTRIES
  final List usageEntries = context.source[laborFindingLaborValue().multiValue()] as List

  //CATALOGS
  final def opsCatalog = laborValue[LaborValueCatalog.OPS_CATALOG]
  final def icdCatalog = laborValue[LaborValueCatalog.ICD_CATALOG]
  final def customCatalog = laborValue[LaborValueCatalog.CUSTOM_CATALOG]
  final def valueList = laborValue[LaborValueCatalog.VALUE_LIST]

  if (numericValue) {
    final def unit_ = laborValue[LaborValueNumeric.UNIT]
    final def ucumCode = unit_?.getAt(Unity.UCUM_CODE)
    valueQuantity {
      value = numericValue
      if (unit_ != null) {
        unit = unit_[NAME_MULTILINGUAL_ENTRIES].find { final def me -> me[LANG] == lang }?.getAt(VALUE)
        system = ucumCode != null ? "http://unitsofmeasure.org" : FhirUrls.System.LaborValue.Unit.BASE_URL
        code = (ucumCode != null ? ucumCode : unit_[CODE]) as String
      }
    }
  } else if (stringValue) {
    valueString((String) stringValue)
  } else if (dateValue) {
    valueDateTime {
      date = dateValue[PrecisionDate.DATE]
    }
  } else if (boolValue) {
    valueBoolean((Boolean) boolValue)
  } else if (timeValue) {
    valueTime((String) timeValue)
  } else if (usageEntries) {
    valueCodeableConcept {
      usageEntries.each { final def entry ->
        coding {
          system = "urn:centraxx:CodeSystem/UsageEntry-" + entry[ID]
          code = entry[CODE] as String
          display = entry[NAME_MULTILINGUAL_ENTRIES]
              .find { final def me -> me[LANG] == lang }?.getAt(VALUE)
        }
      }
    }
  } else if (icdCatalog) {
    valueCodeableConcept {
      context.source[laborFindingLaborValue().icdEntryValue()].each { final def entry ->
        coding {
          system = "urn:centraxx:CodeSystem/IcdEntry-" + icdCatalog[ID]
          version = entry[IcdEntry.CATALOGUE]?.getAt(CATALOGUE_VERSION)
          code = entry[CODE] as String
          display = entry[IcdEntry.PREFERRED]
        }
      }
    }
  } else if (opsCatalog) {
    valueCodeableConcept {
      context.source[laborFindingLaborValue().opsEntryValue()].each { final def entry ->
        coding {
          system = "urn:centraxx:CodeSystem/OpsEntry-" + opsCatalog[ID]
          version = entry[CATALOGUE]?.getAt(CATALOGUE_VERSION)
          code = entry[CODE] as String
          display = entry[PREFERRED]
        }
      }
    }
  } else if (customCatalog) {
    valueCodeableConcept {
      context.source[laborFindingLaborValue().catalogEntryValue()].each { final def entry ->
        coding {
          system = "urn:centraxx:CodeSystem/CustomCatalog-" + customCatalog[ID]
          version = entry[CATALOG]?.getAt(CATALOGUE_VERSION)
          code = entry[CODE] as String
          display = entry[NAME_MULTILINGUAL_ENTRIES]
              .find { final def me -> me[LANG] == lang }?.getAt(VALUE)
        }
      }
    }
  } else if (valueList) {
    valueCodeableConcept {
      context.source[laborFindingLaborValue().catalogEntryValue()].each { final def entry ->
        coding {
          system = "urn:centraxx:CodeSystem/ValueList-" + valueList[ID]
          version = entry[CATALOG]?.getAt(CATALOGUE_VERSION)
          code = entry[CODE] as String
          display = entry[NAME_MULTILINGUAL_ENTRIES]
              .find { final def me -> me[LANG] == lang }?.getAt(VALUE)
        }
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

