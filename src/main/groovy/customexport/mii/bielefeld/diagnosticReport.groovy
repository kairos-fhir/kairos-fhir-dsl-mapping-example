package customexport.mii.bielefeld

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
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
final String laborMethodName = "MP_DiagnosticReportLab"

// the code of the FHIR DiagnosticReport.status laborValue
final String statusLvCode = "DiagnosticReport.status"

// the issued Date laborValue
final String issuedLvCode = "DiagnosticReport.issued"

// the identifier.assigner laborValue
final String assignerLvCode = "DiagnosticReport.identifier.assigner"

final String orgUnitDizId = "ukowl.de"

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

  identifier {
    type {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "FILL"
      }
    }
    system = FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME // this needs to unique in CXX anyway
    value = context.source[laborFinding().shortName()]


    assigner {
      identifier {
        system = "https://www.medizininformatik-initiative.de/fhir/core/CodeSystem/core-location-identifier"
        value = orgUnitDizId
      }
    }

  }

  final List serviceRequestMappings = context.source[laborFinding().laborMappings()].findAll { final def mapping ->
    mapping[LaborMapping.MAPPING_TYPE] == LaborMappingType.SERVICEREQUEST as String
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
    final String s = lflvStatus[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].find()?.getAt(CODE) as String
    status(DiagnosticReport.DiagnosticReportStatus
        .fromCode(s.toLowerCase()))
  } else {
    status(DiagnosticReport.DiagnosticReportStatus.UNKNOWN)
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


