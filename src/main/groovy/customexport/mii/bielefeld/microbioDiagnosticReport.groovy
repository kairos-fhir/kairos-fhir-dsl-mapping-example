package customexport.mii.bielefeld


import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import org.hl7.fhir.r4.model.DiagnosticReport

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFinding

final String STATUS = "DiagnosticReport.status"
final String ISSUED = "DiagnosticReport.issued"
final String EFFECTIVE = "DiagnosticReport.effective[x]"
final String KL_BEZUGS_DATUM = "DiagnosticReport.effective[x].extension:QuelleKlinischesBezugsdatum.value[x].code"

final Map PROFILE_TYPES = [
    (STATUS)         : LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
    (ISSUED)         : LaborFindingLaborValue.DATE_VALUE,
    (EFFECTIVE)      : LaborFindingLaborValue.DATE_VALUE,
    (KL_BEZUGS_DATUM): LaborFindingLaborValue.CATALOG_ENTRY_VALUE,
]


diagnosticReport {

  if (context.source[laborFinding().laborMethod().code()] != "MP_Mikrobio_DiagnosticReport") {
    return
  }

  id = "DiagnosticReport/" + context.source[laborFinding().id()]

  identifier {
    type {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "FILL"
      }
    }
    system = "https://ukowl.de/sid/mii-exa-mikrobio-diagnosticreport"
    value = context.source[laborFinding().laborFindingId()]

    assigner {
      identifier {
        system = "https://www.medizininformatik-initiative.de/fhir/core/CodeSystem/core-location-identifier"
        value = "ukowl.de"
      }
    }
  }

  final Map<String, Object> lflvMap = getLflvMap(context.source, PROFILE_TYPES)

  final def diagnosticReportStatus = lflvMap.get(STATUS)?.find()

  if (diagnosticReportStatus) {
    status = diagnosticReportStatus[CODE]
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
    coding {
      system = "http://snomed.info/sct"
      code = "4341000179107"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "11502-2"
    }
  }

  final def lm = context.source[laborFinding().laborMappings()].find {
    it[LaborMapping.RELATED_PATIENT] != null
  }

  if (lm) {
    subject {
      reference = "Patient/" + lm[LaborMapping.RELATED_PATIENT][PatientContainer.ID]
    }
  }

  final def effectivePD = lflvMap.get(EFFECTIVE)
  final def klinischesBD = lflvMap.get(KL_BEZUGS_DATUM)?.find()


  if (effectivePD != null && effectivePD[PrecisionDate.DATE] != null) {
    effectiveDateTime {
      date = effectivePD[PrecisionDate.DATE]

      if (klinischesBD) {
        extension {
          url = "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/QuelleKlinischesBezugsdatum"
          valueCoding {
            system = "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/ValueSet/QuelleKlinischesBezugsdatum"
            code = klinischesBD[CODE] as String
          }
        }
      }
    }
  }

  final def issuedPD = lflvMap.get(ISSUED)

  if (issuedPD != null && issuedPD[PrecisionDate.DATE] != null) {
    issued {
      date = issuedPD[PrecisionDate.DATE]
    }
  }

  context.source[laborFinding().children()].each { final def child ->
    result {
      reference = "Observation/" + child[LaborFinding.ID]
    }
  }
}


static Map<String, Object> getLflvMap(final def finding, final Map<String, String> types) {
  final Map<String, Object> lflvMap = [:]
  if (!finding) {
    return lflvMap
  }

  types.each { final String lvCode, final String lvType ->
    final def lflvForLv = finding[LaborFinding.LABOR_FINDING_LABOR_VALUES].find { final def lflv ->
      lflv[LaborFindingLaborValue.CRF_TEMPLATE_FIELD][CrfTemplateField.LABOR_VALUE][CODE] == lvCode
    }

    if (lflvForLv && lflvForLv[lvType]) {
      lflvMap[(lvCode)] = lflvForLv[lvType]
    }
  }
  return lflvMap
}

