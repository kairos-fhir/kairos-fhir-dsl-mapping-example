package projects.uscore

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.BinaryFile
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ID
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represents a CXX LaborMapping.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-diagnosticreport-note.html
 *
 * The resulting DiagnosticReport contains all the files that are attached to the finding as a laborFindingLaborValue
 *
 *  TODO: export of file as attachment
 * @author Jonas Küttner
 * @since v.1.13.0, CXX.v.2022.1.0
 *
 */

final def lang = "de"

diagnosticReport {
  // filter for lblvs that are files
  final def lflvs = context.source[laborMapping().laborFinding().laborFindingLaborValues()].findAll {
    final def lflv -> lflv[LaborFindingLaborValue.FILE_VALUE]
  }

  if (lflvs.isEmpty()) {
    return
  }

  id = "DiagnosticReport/Note-" + context.source[laborMapping().laborFinding().id()]
  language = lang
  status = DiagnosticReport.DiagnosticReportStatus.UNKNOWN


  category {
    coding {
      system = FhirUrls.System.LaborMethod.Category.BASE_URL
      code = context.source[laborMapping().laborFinding().laborMethod().category()]
    }
  }

  code {
    coding {
      system = FhirUrls.System.LaborMethod.BASE_URL
      code = context.source[laborMapping().laborFinding().laborMethod().code()]
      display = context.source[laborMapping().laborFinding().laborMethod().nameMultilingualEntries()]?.find {
        final def ml -> ml[MultilingualEntry.LANG] == lang
      }?.getAt(MultilingualEntry.VALUE)
    }
  }

  final def relatedPatient = context.source[laborMapping().relatedPatient()]

  if (relatedPatient) {
    subject {
      reference = "Patient/" + relatedPatient[ID]
    }
  }

  final def relatedEncounter = context.source[laborMapping().episode()]
  if (relatedEncounter) {
    encounter {
      reference = "Encounter/" + relatedEncounter[ID]
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  issued = normalizeDate(context.source[laborMapping().laborFinding().creationDate()] as String)

  lflvs.each {
    final def lflv ->
      presentedForm {
        contentType = lflv["contentType"]
        size = lflv["fileSize"] as Integer
        creation = lflv[BinaryFile.CREATIONDATE]
      }
  }


}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}
