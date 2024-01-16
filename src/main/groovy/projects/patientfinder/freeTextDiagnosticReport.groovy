package projects.patientfinder


import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.CrfTemplateField.LABOR_VALUE
import static de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue.CRF_TEMPLATE_FIELD
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * represented by CXX LaborMapping
 * @author Mike Wähnert
 * @since v.1.8.0, CXX.v.3.18.1
 *
 * This writes free text fields into a diagnostic report. if a LaborMethod code contains  '_free_text', all LabFinLabVals are
 * concatenated in to a string. For other profiles, only LabFinLabVals with a LaborValue code that contains _MEMO are written
 * in a concatenated string
 */
diagnosticReport {
  final def laborMethod = context.source[laborMapping().laborFinding().laborMethod()]

  final boolean isFreeText = ((String) laborMethod[CODE]).contains("_free_text")

  def labFinLabVals

  if (isFreeText) {
    labFinLabVals = (Collection) context.source[laborMapping().laborFinding().laborFindingLaborValues()]
  } else {
    labFinLabVals = context.source[laborMapping().laborFinding().laborFindingLaborValues()].findAll {
      final lflv ->
        ((String) lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE])
            .toLowerCase()
            .contains("_memo")
    }
  }

  if (labFinLabVals.isEmpty()) {
    return
  }

  id = "DiagnosticReport/" + context.source[laborMapping().laborFinding().id()]

  identifier {
    system = "urn:centraxx"
    value = context.source[laborMapping().laborFinding().laborFindingId()]
  }

  status = DiagnosticReport.DiagnosticReportStatus.UNKNOWN

  code {
    coding {
      system = "urn:centraxx"
      code = laborMethod[CODE] as String
      display = laborMethod[LaborMethod.NAME_MULTILINGUAL_ENTRIES].find { final def entry ->
        "en" == entry[MultilingualEntry.LANG]
      }?.getAt(MultilingualEntry.VALUE)
    }
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  if (!isFakeEpisode(context.source[laborMapping().episode()])) {
    encounter {
      reference = "Encounter/" + context.source[laborMapping().episode().id()]
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  issued {
    date = context.source[laborMapping().laborFinding().creationDate()]
  }

  result {
    reference = "Observation/" + context.source[laborMapping().laborFinding().id()]
  }

  final def concatString = labFinLabVals.collect {
    final lflv ->
      "${lflv[CRF_TEMPLATE_FIELD][LABOR_VALUE][CODE]}: " +
          "${lflv[LaborFindingLaborValue.STRING_VALUE]}"
  }.join("\n\n")

  conclusion = concatString
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


