package projects.mii.modul.labor

import de.kairos.fhir.centraxx.metamodel.Annotation
import de.kairos.fhir.centraxx.metamodel.CrfItem
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/DiagnosticReportLab
 * This export study crf as DiagnosticReport. The Export of single observations from CentraXX CrfItems is not yet implemented.
 * @author Jonas KÜttner
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */
diagnosticReport {

  if (!context.source[studyVisitItem().crf()]) {
    return
  }

  id = "DiagnosticReport/SVI-" + context.source[studyVisitItem().id()]

  meta {
    source = "urn:centraxx"
    profile("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/DiagnosticReportLab")
  }

  identifier {
    type {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "FILL"
      }
    }
    system = "urn:centraxx"
    value = context.source[studyVisitItem().crf().id()]
  }

  status = DiagnosticReport.DiagnosticReportStatus.UNKNOWN

  category {
    coding {
      system = "urn:centraxx"
      code = "study crf"
    }
  }

  code {
    coding {
      system = "urn:centraxx"
      code = context.source[studyVisitItem().crf().template().name()] as String
      version = context.source[studyVisitItem().crf().template().version()] as String
    }
    text = context.source[studyVisitItem().crf().template().description()] as String
  }

  subject {
    reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
  }

  if (context.source[studyVisitItem().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[studyVisitItem().episode().id()]
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
  }

  final def issuedDate = context.source[studyVisitItem().crf().lastChangedOn()]
  if (issuedDate) {
    issued = issuedDate
  }

  context.source[studyVisitItem().crf().items()]?.each { final def item ->
    result {
      reference = "Observation/SVI" + item[CrfItem.ID]
    }
  }

  context.source[studyVisitItem().crf().annotations()]?.each { final def annotation ->
    media {
      comment = annotation[Annotation.VALUE]
    }
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}






