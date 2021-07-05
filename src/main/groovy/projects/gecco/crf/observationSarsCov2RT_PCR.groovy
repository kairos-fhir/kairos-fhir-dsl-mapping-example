package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import org.hl7.fhir.r4.model.CodeableConcept
import org.hl7.fhir.r4.model.Observation
import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/sarscov2rtpcr
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * hints:
 *  A StudyEpisode is no regular episode and cannot reference an encounter
 */

observation {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2"){
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "SarsCov2_OUTCOME BEI ENTLASSUNG" || studyVisitStatus == "OPEN") {
    return //no export
  }

  final def crfItemDisc = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_ERGEBNIS_ABSTRICH" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemDisc){
    return
  }

  final def crfItems = context.source[studyVisitItem().crf().items()]
  if (!crfItems || crfItems == []) {
    return
  }

  id = "Observation/SarsCov2RT_PCR-" + context.source[studyVisitItem().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sars-cov-2-rt-pcr"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category {
    coding {
      system = "http://loinc.org"
      code = "26436-6"
    }
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "laboratory"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "94500-6"
    }
  }

  subject {
    reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
  }


  final valIndex = []

  //Vaccine codes
  valueCodeableConcept {
    crfItems?.each { final item ->
      final def measParamCode = item[CrfItem.TEMPLATE][CrfTemplateField.LABOR_VALUE][LaborValue.CODE]
      if (measParamCode == "COV_GECCO_ERGEBNIS_ABSTRICH"){
        valIndex.add(item[CrfItem.VALUE_INDEX])
        item[CrfItem.CATALOG_ENTRY_VALUE]?.each { final ite ->
          final def SNOMEDcode = mapDiscSNOMED(ite[CatalogEntry.CODE] as String)
          if (SNOMEDcode) {
            coding {
              system = "http://snomed.info/sct"
              code = SNOMEDcode
            }
          }
        }
      }
    }
  }

  //effective DateTime
  crfItems?.each { final item ->
    final def measParamCode = item[CrfItem.TEMPLATE][CrfTemplateField.LABOR_VALUE][LaborValue.CODE]
    if (measParamCode == "COV_UMG_FOLGEABSTRICH_VOM"){
      final def valIndexDate = item[CrfItem.VALUE_INDEX]
      if (valIndex.contains(valIndexDate)){
        item[CrfItem.DATE_VALUE]?.each { final tD ->
          final def testDate = normalizeDate(tD.toString())
          if (testDate) {
            effectiveDateTime {
              date = testDate
              precision = TemporalPrecisionEnum.DAY.toString()
            }
          }
        }
      }
    }
  }
}

static String normalizeDate(final String dateTimeString) {
  if (dateTimeString.contains("DAY")){
    return null
  }
  else{
    return dateTimeString != null ? dateTimeString.substring(5) : null
  }
}

static String mapDiscSNOMED(final String discharge) {
  switch (discharge) {
    default:
      return null
    case "COV_POSITIV":
      return "260373001"
    case "COV_NEGATIV":
      return "260415000"
  }
}