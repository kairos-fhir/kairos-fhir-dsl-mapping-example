package customexport.mii.modul.labor

import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.Multilingual
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * represented by HDRP LaborMapping
 * specified by https://simplifier.net/medizininformatikinitiative-modullabor/diagnosticreportlab
 * @author Jonas Küttner
 * @since v.1.52.0, HDRP.v.2025.3.0
 */
diagnosticReport {

  id = "DiagnosticReport/" + context.source[laborMapping().laborFinding().id()]

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
    value = context.source[laborMapping().laborFinding().laborFindingId()]
  }

  status = DiagnosticReport.DiagnosticReportStatus.UNKNOWN

  category {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/v2-0074"
      code = mapCategories(context.source[laborMapping().laborFinding().laborMethod().category()] as String)
    }
  }

  code {
    coding {
      system = "urn:centraxx"
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
      display = context.source[laborMapping().laborFinding().laborMethod().multilinguals()].find { final def ml ->
        ml[Multilingual.LANGUAGE] == "de" && ml[Multilingual.SHORT_NAME] != null
      }?.getAt(Multilingual.SHORT_NAME) as String
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

  issued = context.source[laborMapping().laborFinding().creationDate()]

  context.source[laborMapping().laborFinding().laborFindingLaborValues()]?.each { final def lflv ->
    result {
      reference = "Observation/" + lflv[LaborFindingLaborValue.ID]
    }
  }

  // there is no field for an interpretation/conclusion of results in HDRP. To export such, a measure parameter must
  // be introduced in HDRP.
  final def interpretation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "INTERPRETATION" == it[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE]
  }

  conclusion = interpretation ? interpretation[LaborFindingLaborValue.STRING_VALUE] : null

}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String mapCategories(final String cxxCategory) {
  switch (cxxCategory) {
    case "LABOR": return "LAB"
    case "NURSING": return "NRS"
    default: return "OTH"
  }
}

