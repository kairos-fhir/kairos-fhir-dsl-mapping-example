package customexport.patientfinder

import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.Multilingual
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by HDRP LaborMapping
 * @author Mike Wähnert
 * @since v.1.52.0, HDRP.v.2025.3.0
 *
 * Hints:
 *  This is a special mapping for FNUSA.
 */
diagnosticReport {

  final String laborMethodCode = context.source[laborMapping().laborFinding().laborMethod().code()]
  final boolean isExportRelevant = "Histology".equalsIgnoreCase(laborMethodCode)
  if (!isExportRelevant) {
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
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
      display = context.source[laborMapping().laborFinding().laborMethod().multilinguals()].find { final def ml ->
        ml[Multilingual.LANGUAGE] == "en" && ml[Multilingual.SHORT_NAME] != null
      }?.getAt(Multilingual.SHORT_NAME) as String
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
    date = normalizeDate(context.source[laborMapping().laborFinding().findingDate().date()] as String)
  }

  issued {
    date = context.source[laborMapping().laborFinding().creationDate()]
  }

  result {
    reference = "Observation/" + context.source[laborMapping().laborFinding().id()]
  }


  // there is no field for an interpretation/conclusion of results in HDRP. To export such, a measure parameter must
  // be introduced in HDRP.
  final def interpretation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "ZAVER" == it[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][LaborValue.CODE]
  }
  conclusion = interpretation ? interpretation[LaborFindingLaborValue.STRING_VALUE] : null
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

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}