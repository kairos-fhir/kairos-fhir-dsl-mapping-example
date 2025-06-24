package projects.mii.bielefeld

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.AbstractCustomCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.PatientMaster
import de.kairos.fhir.centraxx.metamodel.Unity
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import de.kairos.fhir.centraxx.metamodel.enums.CatalogCategory
import de.kairos.fhir.centraxx.metamodel.enums.LaborFindingValueStatus
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Observation

import javax.annotation.Nullable

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFindingLaborValue
import static org.hl7.fhir.r4.model.Observation.ObservationStatus.CANCELLED
import static org.hl7.fhir.r4.model.Observation.ObservationStatus.CORRECTED
import static org.hl7.fhir.r4.model.Observation.ObservationStatus.ENTEREDINERROR
import static org.hl7.fhir.r4.model.Observation.ObservationStatus.FINAL
import static org.hl7.fhir.r4.model.Observation.ObservationStatus.PRELIMINARY
import static org.hl7.fhir.r4.model.Observation.ObservationStatus.REGISTERED
import static org.hl7.fhir.r4.model.Observation.ObservationStatus.UNKNOWN

// the code of the MII common measurement profile
final String laborMethodName = "MP_DiagnosticReportLab"

// the code of the FHIR DiagnosticReport.status laborValue
final String statusLvCode = "DiagnosticReport.status"

// the issued Date laborValue
final String issuedLvCode = "DiagnosticReport.issued"

// the identifier.assigner laborValue
final String assignerLvCode = "DiagnosticReport.identifier.assigner"

observation {

  if (!isExportable(context, laborMethodName, [statusLvCode, issuedLvCode, assignerLvCode])) {
    return
  }

  id = "Observation/" + context.source[laborFindingLaborValue().id()]

  // Create unique Id from LaborValue code and Lflv Oid
  // assigner not possible
  identifier {
    type {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "OBI"
      }
    }
    system = "urn:centraxx/MessparameterCodeAndMesswertOid"
    value = context.source[laborFindingLaborValue().crfTemplateField().laborValue().code()] + "_" +
        context.source[laborFindingLaborValue().laborFinding().laborFindingId()]
  }

  status(mapStatus(context.source[laborFindingLaborValue().status()] as LaborFindingValueStatus))

  category {
    coding {
      system = "http://loinc.org"
      code = "26436-6"
    }
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "laboratory"
    }
  }

  code {
    final def idContainer = context.source[laborFindingLaborValue().crfTemplateField().laborValue().idContainers()].find { final def idc ->
      idc[IdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE] == "LOINC"
    }

    if (idContainer) {
      coding {
        system = "http://loinc.org"
        code = idContainer[IdContainer.PSN] as String
      }
    }

    coding {
      system = FhirUrls.System.LaborValue.BASE_URL
      code = context.source[laborFindingLaborValue().crfTemplateField().laborValue().code()] as String
    }
  }

  subject {
    reference = "Patient/" + context.source[laborFindingLaborValue().laborFinding().laborMappings()]
        .find()[LaborMapping.RELATED_PATIENT][PatientMaster.ID]
  }

  if (context.source[laborFindingLaborValue().recordedOn()] && context.source[laborFindingLaborValue().recordedOn().date()]) {
    effectiveDateTime = context.source[laborFindingLaborValue().recordedOn().date()]
  }

  final LaborValueDType dType = context.source[laborFindingLaborValue().crfTemplateField().laborValue().dType()] as LaborValueDType

  if (dType == LaborValueDType.DECIMAL || dType == LaborValueDType.INTEGER) {
    valueQuantity {
      value = context.source[laborFindingLaborValue().numericValue()]
      final def lvUnit = context.source[laborFindingLaborValue().crfTemplateField().laborValueDecimal().unit()]
      if (lvUnit) {
        system = "http://unitsofmeasure.org"
        code = lvUnit[Unity.CODE] as String
        unit = lvUnit[Unity.CODE] as String
      }
    }
  }

  if (dType in [LaborValueDType.CATALOG, LaborValueDType.ENUMERATION, LaborValueDType.OPTIONGROUP]) {
    valueCodeableConcept {
      context.source[laborFindingLaborValue().catalogEntryValue()].each { final def ce ->
        coding {
          system = createSystem(ce)
          code = ce[CatalogEntry.CODE] as String
        }
      }
    }

    valueCodeableConcept {
      context.source[laborFindingLaborValue().multiValue()].each { final def ue ->
        coding {
          system = "https://fhir.centraxx.de/system/catalogs/usageEntry"
          code = ue[UsageEntry.CODE] as String
        }
      }
    }
  }
}

static boolean isExportable(final Context context, final String methodCode, final List<String> lvCodes) {
  final def isMiiProfile = context.source[laborFindingLaborValue().laborFinding().laborMethod().code()] == methodCode

  final def isAdditionalDataLv = ((context.source[laborFindingLaborValue().crfTemplateField().laborValue().code()] as String) in lvCodes)

  return isMiiProfile && !isAdditionalDataLv
}

@Nullable
private static String createSystem(final Object catalogEntry) {

  final CatalogCategory category = catalogEntry[CatalogEntry.CATALOG][AbstractCustomCatalog.CATALOG_CATEGORY] as CatalogCategory

  switch (category) {
    case CatalogCategory.VALUELIST:
      return FhirUrls.System.Catalogs.VALUE_LIST + "/" + catalogEntry[CatalogEntry.CATALOG][AbstractCustomCatalog.CODE]
    case CatalogCategory.CUSTOM:
      return FhirUrls.System.Catalogs.CUSTOM_CATALOG + "/" + catalogEntry[CatalogEntry.CATALOG][AbstractCustomCatalog.CODE]

    default: return null
  }
}

private static Observation.ObservationStatus mapStatus(@Nullable final LaborFindingValueStatus cxxStatus){
  if (cxxStatus == null){
    return UNKNOWN
  }
  switch (cxxStatus){
    case LaborFindingValueStatus.R:
      return REGISTERED
    case LaborFindingValueStatus.P:
      return PRELIMINARY
    case LaborFindingValueStatus.F:
      return FINAL
    case LaborFindingValueStatus.C:
      return CORRECTED
    case LaborFindingValueStatus.W:
      return ENTEREDINERROR
    case LaborFindingValueStatus.X:
      return CANCELLED
    default:
      return UNKNOWN
  }
}

