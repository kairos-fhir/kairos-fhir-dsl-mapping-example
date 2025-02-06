package projects.mii.bielefeld.experimental

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.AbstractCustomCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.OrganisationUnit
import de.kairos.fhir.centraxx.metamodel.PatientMaster
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.ValueReference
import de.kairos.fhir.centraxx.metamodel.enums.CatalogCategory
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.DiagnosticReport
import org.hl7.fhir.r4.model.Observation

import javax.annotation.Nullable

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.CrfTemplateField.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFindingLaborValue
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * represented by CXX LaborMapping
 * specified by https://simplifier.net/medizininformatikinitiative-modullabor/diagnosticreportlab
 * @author Jonas Küttner
 * @since v.1.41.0, CXX.v.2024.4.3
 */


// the code of the MII common measurement profile
final String laborMethodName = "MII_MeasurementProfile"

// the code of the FHIR DiagnosticReport.status laborValue
final String statusLvCode = "DiagnosticReport.status"

// the issued Date laborValue
final String issuedLvCode = "DiagnosticReport.issued"

// the identifier.assigner laborValue
final String assignerLvCode = "DiagnosticReport.identifier.assigner"

diagnosticReport {

  // export only patient Labor Mappings here
  if (context.source[laborMapping().laborFinding().laborMethod().code()] != laborMethodName) {
    return
  }

  // there may be multiple mappings for the report, only export patient mapping
  if (context.source[laborMapping().mappingType()] as LaborMappingType != LaborMappingType.PATIENTLABORMAPPING) {
    return
  }

  id = "DiagnosticReport/" + context.source[laborMapping().laborFinding().id()]

  meta {
    source = "urn:centraxx"
    profile("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/DiagnosticReportLab")
  }

  final def assignerLFLV = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    assignerLvCode == it[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE]
  }

  if (context.source[laborMapping().laborFinding().laborFindingId()]) {
    identifier {
      type {
        coding {
          system = "http://terminology.hl7.org/CodeSystem/v2-0203"
          code = "FILL"
        }
      }
      system = FhirUrls.System.Finding.LABOR_FINDING_ID // this needs to unique in CXX anyway
      value = context.source[laborMapping().laborFinding().laborFindingId()]

      // assigner would have to be coded as measurement value in the Observation. Remember to filter out of the actual observations
      if (assignerLFLV && assignerLFLV[LaborFindingLaborValue.MULTI_VALUE_REFERENCES]) {
        final def orgUnit = assignerLFLV[LaborFindingLaborValue.MULTI_VALUE_REFERENCES].find()?.getAt(ValueReference.ORGANIZATION_VALUE)
        if (orgUnit) {
          assigner {
            reference = "Organization/" + orgUnit[OrganisationUnit.ID]
          }
        }
      }
    }
  }

  //TODO: add in dsl metamodel enum and check if initialized
  final List serviceRequestMappings = context.source[laborMapping().laborFinding().laborMappings()].findAll { final def mapping ->
    mapping[LaborMapping.MAPPING_TYPE] == "SERVICEREQUEST"
  }

  serviceRequestMappings.forEach {
    basedOn {
      reference = "ServiceRequest/" + serviceRequestMappings[LaborMapping.RELATED_OID]
    }
  }

  final def lflvStatus = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find { final def lflv ->
    lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == statusLvCode
  }

  if (lflvStatus && lflvStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE]) {
    status = DiagnosticReport.DiagnosticReportStatus
        .fromCode(lflvStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].find()?.getAt(CODE) as String)
  } else {
    status = DiagnosticReport.DiagnosticReportStatus.UNKNOWN
  }


  category {
    coding {
      system = "http://loinc.org"
      code = "26436-6"
    }
    coding {
      system = "http://terminology.hl7.org/CodeSystem/v2-0074"
      code = "LAB"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "11502-2"
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  if (context.source[laborMapping().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[laborMapping().episode().id()]
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  final def lvIssued = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find { final def lflv ->
    lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == issuedLvCode
  }

  if (lvIssued && lvIssued[LaborFindingLaborValue.DATE_VALUE]) {
    issued = lvIssued[LaborFindingLaborValue.DATE_VALUE][PrecisionDate.DATE]
  }

  final List<Map<String, Object>> lfvls = context.source[laborMapping().laborFinding().laborFindingLaborValues()]?.findAll { final def lflv ->
    !((lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] as String) in [issuedLvCode, statusLvCode, assignerLvCode])
  }

  lfvls.each { final def lflv ->
    result {
      reference = "#Observation/" + lflv[LaborFindingLaborValue.ID]
    }
  }

  lfvls.collect { final Map<String, Object> lflv ->
    new Context(lflv)
  }.each { final def lflvContext ->
    contained {
      observation {
        id = "Observation/" + lflvContext.source[laborFindingLaborValue().id()]

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
          value = lflvContext.source[laborFindingLaborValue().crfTemplateField().laborValue().code()] + "_" + lflvContext.source[laborFindingLaborValue().id()]

          if (assignerLFLV && assignerLFLV[LaborFindingLaborValue.MULTI_VALUE_REFERENCES]) {
            final def orgUnit = assignerLFLV[LaborFindingLaborValue.MULTI_VALUE_REFERENCES].find()?.getAt(ValueReference.ORGANIZATION_VALUE)
            if (orgUnit) {
              assigner {
                reference = "Organization/" + orgUnit[OrganisationUnit.ID]
              }
            }
          }
        }

        status = Observation.ObservationStatus.FINAL

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

        final def idContainer = lflvContext.source[laborFindingLaborValue().crfTemplateField().laborValue().idContainers()].find { final def idc ->
          idc[IdContainer.ID_CONTAINER_TYPE][CODE] == "LOINC"
        }

        if (idContainer) {
          code {
            coding {
              system = "http://loinc.org"
              code = idContainer[IdContainer.PSN] as String
            }
          }
        }

        subject {
          "Patient/" + lflvContext.source[laborFindingLaborValue().laborFinding().laborMappings()].find()[LaborMapping.RELATED_PATIENT][PatientMaster.ID]
        }

        effectiveDateTime = context.source[laborMapping().laborFinding().findingDate().date()]

        final LaborValueDType dType = lflvContext.source[laborFindingLaborValue().crfTemplateField().laborValue().dType()] as LaborValueDType

        if (dType == LaborValueDType.DECIMAL || dType == LaborValueDType.INTEGER) {
          valueQuantity {
            value = lflvContext.source[laborFindingLaborValue().numericValue()]
            final def lvUnit = lflvContext.source[laborFindingLaborValue().crfTemplateField().laborValueDecimal().unit()]
            if (lvUnit) {
              system = "http://unitsofmeasure.org"
              code = lvUnit[CODE] as String
              unit = lvUnit[CODE] as String
            }
          }
        }

        if (dType in [LaborValueDType.CATALOG, LaborValueDType.ENUMERATION, LaborValueDType.OPTIONGROUP]) {
          valueCodeableConcept {
            lflvContext.source[laborFindingLaborValue().catalogEntryValue()].each { final def ce ->
              coding {
                system = createSystem(ce)
                code = ce[CODE] as String
              }
            }
          }

          valueCodeableConcept {
            lflvContext.source[laborFindingLaborValue().multiValue()].each { final def ue ->
              coding {
                system = "https://fhir.centraxx.de/system/catalogs/usageEntry"
                code = ue[CODE] as String
              }
            }
          }
        }
      }
    }
  }
}


@Nullable
private static String createSystem(final Object catalogEntry) {
  final CatalogCategory category = catalogEntry[CatalogEntry.CATALOG][AbstractCustomCatalog.CATALOG_CATEGORY] as CatalogCategory

  switch (category) {
    case CatalogCategory.VALUELIST:
      return FhirUrls.System.Catalogs.VALUE_LIST
    case CatalogCategory.CUSTOM:
      return FhirUrls.System.Catalogs.CUSTOM_CATALOG

    default: return null
  }
}