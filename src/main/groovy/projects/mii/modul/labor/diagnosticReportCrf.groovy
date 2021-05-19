import de.kairos.fhir.centraxx.metamodel.CrfItem
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem
/**
 * Represented by a CXX LaborMapping
 * exports a a study crf to a
 * Specified by https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/DiagnosticReportLab
 * @author Jonas KÜttner
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */

diagnosticReport {

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
      system = "http://loinc.org"
      code = "11502-2"
    }
  }

  subject {
    reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
  }

  encounter {
    reference = context.source[studyVisitItem().episode()]? "Encounter/" + context.source[studyVisitItem().episode().id()] : null
  }

  effectiveDateTime {
    date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
  }

  final def issuedDate = context.source[studyVisitItem().crf().lastChangedOn()]
  if (issuedDate){
    issued(issuedDate)
  }

  context.source[studyVisitItem().crf().items()].each{
    result {
      reference = "Observation-SVI/" + it[CrfItem.ID] as String
    }
  }


  context.source[studyVisitItem().crf().annotations()]?.each {
    media {
      comment = "the comment" //TODO: get the actual comment
    }
  }

}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}






