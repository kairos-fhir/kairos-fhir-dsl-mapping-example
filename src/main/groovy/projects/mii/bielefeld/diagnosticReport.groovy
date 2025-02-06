package projects.mii.bielefeld

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.OrganisationUnit
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.ValueReference
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.CrfTemplateField.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding

/**
 * represented by CXX LaborMapping
 * specified by https://simplifier.net/medizininformatikinitiative-modullabor/diagnosticreportlab
 * @author Jonas Küttner
 * @since v.1.43.0, CXX.v.2024.5.0
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
  if (context.source[laborFinding().laborMethod().code()] != laborMethodName) {
    return
  }

  id = "DiagnosticReport/" + context.source[laborFinding().id()]

  meta {
    source = "urn:centraxx"
    profile("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/DiagnosticReportLab")
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "11502-2"
    }
  }

  final def assignerLFLV = context.source[laborFinding().laborFindingLaborValues()].find {
    assignerLvCode == it[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] as String
  }


  identifier {
    type {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "FILL"
      }
    }
    system = FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME // this needs to unique in CXX anyway
    value = context.source[laborFinding().shortName()]

    println(assignerLFLV)
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

  //TODO: add in dsl metamodel enum and check if initialized
  final List serviceRequestMappings = context.source[laborFinding().laborMappings()].findAll { final def mapping ->
    mapping[LaborMapping.MAPPING_TYPE] == "SERVICEREQUEST"
  }

  serviceRequestMappings.forEach {
    basedOn {
      reference = "ServiceRequest/" + serviceRequestMappings[LaborMapping.RELATED_OID]
    }
  }

  final def lflvStatus = context.source[laborFinding().laborFindingLaborValues()].find { final def lflv ->
    lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == statusLvCode
  }

  if (lflvStatus && lflvStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE]) {
    status(DiagnosticReport.DiagnosticReportStatus
        .fromCode(lflvStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].find()?.getAt(CODE) as String))
  } else {
    status(DiagnosticReport.DiagnosticReportStatus.UNKNOWN)
  }

  basedOn {
    reference = "ServiceRequest/" + 1
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

  final def lmPatient = context.source[laborFinding().laborMappings()]
      .find { final def lm -> lm[LaborMapping.RELATED_PATIENT] != null }


  if (lmPatient) {
    subject {
      reference = "Patient/" + lmPatient[LaborMapping.RELATED_PATIENT][PatientContainer.ID]
    }
  }

  final def lmEpisode = context.source[laborFinding().laborMappings()]
      .find { final def lm -> lm[LaborMapping.EPISODE] != null }

  if (lmEpisode) {
    encounter {
      reference = "Encounter/" + lmEpisode[LaborMapping.EPISODE][Episode.ID]
    }
  }

  effectiveDateTime {
    date = context.source[laborFinding().findingDate().date()]
  }

  final def lvIssued = context.source[laborFinding().laborFindingLaborValues()].find { final def lflv ->
    lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] == issuedLvCode
  }

  if (lvIssued && lvIssued[LaborFindingLaborValue.DATE_VALUE]) {
    issued = lvIssued[LaborFindingLaborValue.DATE_VALUE][PrecisionDate.DATE]
  }

  context.source[laborFinding().laborFindingLaborValues()]?.findAll { final def lflv ->
    !((lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE] as String) in [issuedLvCode, statusLvCode, assignerLvCode])
  }.each { final def lflv ->
    result {
      reference = "Observation/" + lflv[LaborFindingLaborValue.ID]
    }
  }
}


