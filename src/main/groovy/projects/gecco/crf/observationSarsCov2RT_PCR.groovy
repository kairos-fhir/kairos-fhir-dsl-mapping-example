package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
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
  if (studyCode != "SARS-Cov-2") {
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
  if (!crfItemDisc) {
    return
  }

  final def crfItems = context.source[studyVisitItem().crf().items()]
  if (!crfItems || crfItems == []) {
    return
  }

  id = "Observation/SarsCov2RT-PCR-" + context.source[studyVisitItem().id()]
  identifier {
    type {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/v2-0203"
        code = "OBI"
      }
    }
    system = "http://www.acme.com/identifiers/patient"
    value = "Patient/Patient-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    assigner {
      reference = "Assigner/" + context.source[studyVisitItem().creator().id()]
    }
  }
  meta {
    source = "https://fhir.centraxx.de"
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
  effectiveDateTime {
    date = normalizeDate(crfItemDisc[CrfItem.CREATIONDATE] as String)
    precision = TemporalPrecisionEnum.DAY.toString()
  }
  subject {
    reference = "Patient/Patient-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
  }

  final def valIndex = []
  valueCodeableConcept {
    crfItems?.each { final item ->
      final def measParamCode = item[CrfItem.TEMPLATE][CrfTemplateField.LABOR_VALUE][LaborValue.CODE]
      if (measParamCode == "COV_GECCO_ERGEBNIS_ABSTRICH") {
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

  final def crfItemPCRDates = context.source[studyVisitItem().crf().items()]
  if (!crfItemPCRDates || crfItemPCRDates == []) {
    return
  }

  final List pcrDateList = []
  crfItemPCRDates?.each { final dates ->
    final def pcrDateCode = dates[CrfItem.TEMPLATE][CrfTemplateField.LABOR_VALUE][LaborValue.CODE]
    if (pcrDateCode == "COV_UMG_FOLGEABSTRICH_VOM") {
      final String tDs = normalizeDate(dates[CrfItem.DATE_VALUE][PrecisionDate.DATE] as String)
      if (tDs) {
        pcrDateList.add(tDs)
      }
    }
  }

  //Date of last test--> most relevant
  effectiveDateTime {
    date = selectMostRecentDate(pcrDateList)
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

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String selectMostRecentDate(final List<String> vdl) {
  return !vdl ? null : (vdl.sort().last() as String)
}