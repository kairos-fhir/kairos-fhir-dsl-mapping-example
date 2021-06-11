package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import org.hl7.fhir.r4.model.Consent
import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/donotresuscitateorder
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */
consent {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2"){
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "SarsCov2_ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemDNR = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_DNR_STATUS" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemDNR){
    return //no export
  }
  if (crfItemDNR[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "DoNotResucitateOrder/" + context.source[studyVisitItem().crf().id()]

    meta {
      profile("https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/do-not-resuscitate-order")
    }
    
    status = "active"

    scope {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/consentscope"
        code = "adr"
      }
    }

    category {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/consentcategorycodes"
        code = "dnr"
      }
    }

    patient {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    provision {

      type = Consent.ConsentProvisionType.PERMIT

      code {
        crfItemDNR[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def DNRcode = mapDNR(item[CatalogEntry.CODE] as String)
          if (DNRcode) {
            coding {
              system = "http://snomed.info/sct"
              code = DNRcode
            }
          }
        }
      }
    }
  }
}


static String mapDNR(final String resp) {
  switch (resp) {
    case ("COV_JA"):
      return "304252001"
    case ("COV_NEIN"):
      return "304253006"
    case ("COV_UMG_UNBEKANNT"):
      return "261665006"
    default: null
  }
}