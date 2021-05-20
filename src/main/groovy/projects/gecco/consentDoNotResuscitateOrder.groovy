package projects.gecco


import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.LaborFinding
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborMethod
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Patient

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since CXX.v.3.18.1*
 * Maps the following profile:
 *  - Do-Not-Resuscitate Order
 */


patient {

  context.source[patient().patientContainer().consents()]


  final def lM = context.source[patient().patientContainer().laborMappings() as String].find {
    it[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_METHOD][LaborMethod.CODE] == "SYMPTOMS_COVID19_PROFILE_CODE"
  }

  if (!lM) {
    return // no export
  }

  id = "SymptomsCovid19/" + lM[LaborMapping.LABOR_FINDING][LaborFinding.ID]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/symptoms-covid-19"
  }

  extension {
    url = "https://simplifier.net/forschungsnetzcovid-19/uncertaintyofpresence"
    valueCodeableConcept {
      coding {
        system = "http://snomed.info/sct"
        code = "261665006"
      }
    }
  }


  final def clinicStat = lM[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find {
    "CLINICAL_STATUS_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (clinicStat) {
    clinicalStatus {
      clinicStat[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
        coding {
          system = "http://terminology.hl7.org/CodeSystem/condition-clinical"
          code = entry[CatalogEntry.CODE] as String
        }
      }
    }
  }

  final def verifStat = lM[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find {
    "VERIFICATION_STATUS_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (verifStat) {
    verificationStatus {
      verifStat[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
        coding {
          system = "http://snomed.info/sct"
          code = entry[CatalogEntry.CODE] as String
        }
      }
    }
  }

  category {
    coding {
      system = "http://loinc.org"
      code = "75325-1"
    }
  }

  final def sympSever = lM[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find {
    "SYMPTOM_SEVERITY_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (sympSever) {
    sympSever[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
      severity {
        coding {
          system = "http://snomed.info/sct"
          code = entry[CatalogEntry.CODE] as String
        }
      }
    }
  }

  final def sympCode = lM[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find {
    "COVID_SYMPTOMS_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (sympCode) {
    sympCode[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
      code {
        coding {
          system = "http://snomed.info/sct"
          code = entry[CatalogEntry.CODE] as String
        }
      }
    }
  }

  subject {
    reference = "Patient/" + lM[LaborMapping.RELATED_PATIENT][Patient.IDENTIFIER as String]
  }
  final def episodeID = lM[LaborMapping.EPISODE][Episode.ID]
  if (episodeID) {
    encounter {
      reference = "Episode/" + episodeID
    }
  }

  final def onsetDate = lM[LaborMapping.LABOR_FINDING][LaborFinding.LABOR_FINDING_LABOR_VALUES].find {
    "ONSET_DATE_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (onsetDate) {
    onsetDateTime {
      onsetDateTime = onsetDate[LaborFindingLaborValue.DATE_VALUE]
    }
  }

//If the measurement profile contains a measurement parameter with code "ANNOTATION_CODE" and type "String"
  final def sympAnnotation = context.source[laborMapping().laborFinding().laborFindingLaborValues()].find {
    "ANNOTATION_CODE" == it[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE)
  }
  if (sympAnnotation) {
    note {
      text = sympAnnotation[LaborFindingLaborValue.STRING_VALUE]
    }
  }
}
