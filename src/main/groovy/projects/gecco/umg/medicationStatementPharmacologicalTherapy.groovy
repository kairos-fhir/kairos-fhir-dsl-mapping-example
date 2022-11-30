package projects.gecco.umg

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.MedicationStatement
import org.hl7.fhir.r4.model.Narrative

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/pharmacologicaltherapy
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */
medicationStatement {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2") {
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "MEDIKATION" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemThera = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_THERAPIE" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemThera) {
    return
  }
  if (crfItemThera[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "MedicationStatement/PharmacologicalTherapy-" + context.source[studyVisitItem().id()]

    meta {
      source = "https://fhir.centraxx.de"
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy"
    }

    status = MedicationStatement.MedicationStatementStatus.UNKNOWN

    medication {
      medicationCodeableConcept {
        crfItemThera[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def ATCcode = matchResponseToATC(item[CatalogEntry.CODE] as String)
          final def Othercode = matchResponseToOther(item[CatalogEntry.CODE] as String)

          if (ATCcode) {
            coding {
              system = "http://fhir.de/CodeSystem/dimdi/atc"
              code = ATCcode
            }
          }
          else if (Othercode){
            coding {
              system = "https://www.netzwerk-universitaetsmedizin.de/fhir/CodeSystem/ecrf-parameter-codes"
              code = Othercode
            }
          }
        }
        /*crfItemThera[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def SNOMEDcode = matchResponseToSNOMED(item[CatalogEntry.CODE] as String)
          if (SNOMEDcode) {
            coding {
              system = "http://snomed.info/sct"
              code = SNOMEDcode
            }
          }
        }*/
      }
    }

    if (getMedicationCodeableConcept().getCoding().isEmpty()) {
      Coding coding = new Coding()
      coding.addExtension(new Extension("http://hl7.org/fhir/StructureDefinition/data-absent-reason", new CodeType("unsupported")))
      getMedicationCodeableConcept().addCoding(coding)
    }

    subject {
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }
    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
      precision = TemporalPrecisionEnum.SECOND.toString()
    }
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String matchResponseToATC(final String resp) {
  switch (resp) {
    case ("COV_HYDROXYVITAMIN_D"):
      return "A11CC06"
    case ("COV_ANTI_TNF"):
      return "L04AB"
    case ("COV_ANTIPYRETIKA"):
      return "N02B"
    case ("COV_ATAZANAVIR"):
      return "J05AE08"
    case ("COV_CAMOSTAT"):
      return "B02AB04"
    case ("COV_CHLORO_PHOS"):
      return "P01BA01"
    case ("COV_CNI"):
      return "L04AD"
    case ("COV_COLCHICINE"):
      return "M04AC01"
    case ("COV_CONVA_PLASMA"):
      return "E9743"
    case ("COV_DARUNAVIR"):
      return "J05AE10"
    case ("COV_FAVIPIRAVIR"):
      return "J05AX27"
    case ("COV_GANCICLOVI"):
      return "J05AB06"
    case ("COV_HYDROXYCHLOROQUIN"):
      return "P01BA02"
    case ("COV_II1_RECEPTOR"):
      return "L04AC"
    case ("COV_INTERFERONE"):
      return "L03AB"
    case ("COV_IVERMECTIN"):
      return "P02CF01"
    case ("COV_KORTIKOSTEROIDE"):
      return "S02B"
    case ("COV_LOPINAVIR"):
      return "J05AR10"
    case ("COV_OSELTAMIVIR"):
      return "J05AH02"
    case ("COV_RIBAVIRIN"):
      return "J05AP01"
    case ("COV_RUXOLITINIB"):
      return "L01XE18"
    case ("COV_SARILUMAB"):
      return "L04AC14"
    case ("COV_TOCILIZUMAB"):
      return "L04AC07"
    case ("COV_ZINC"):
      return "A12CB"
    default: null
  }
}
/*
static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_HYDROXYVITAMIN_D"):
      return "88519001"
    case ("COV_ANTI_TNF"):
      return "416897008"
    case ("COV_ATAZANAVIR"):
      return "413591007"
    case ("COV_CHLORO_PHOS"):
      return "14728000"
    case ("COV_CNI"):
      return "416587008"
    case ("COV_COLCHICINE"):
      return "73133000"
    case ("COV_DARUNAVIR"):
      return "424096001"
    case ("COV_GANCICLOVI"):
      return "78025001"
    case ("COV_HYDROXYCHLOROQUIN"):
      return "83490000"
    case ("COV_II1_RECEPTOR"):
      return "430817009"
    case ("COV_INTERFERONE"):
      return "768865007"
    case ("COV_IVERMECTIN"):
      return "96138006"
    case ("COV_KORTIKOSTEROIDE"):
      return "768759001"
    case ("COV_LOPINAVIR"):
      return "134573001"
    case ("COV_OSELTAMIVIR"):
      return "386142008"
    case ("COV_REMDESIVIR"):
      return "870518005"
    case ("COV_RIBAVIRIN"):
      return "35063004"
    case ("COV_RUXOLITINIB"):
      return "703779004"
    case ("COV_SARILUMAB"):
      return "763522001"
    case ("COV_TOCILIZUMAB"):
      return "444649004"
    case ("COV_ZINC"):
      return "764877006"
    default: null
  }
}*/


static String matchResponseToOther(final String resp) {
  switch (resp) {
    case ("COV_STEROIDS_KLEINER_5MG"):
      return "steroids-lt"
    case ("COV_STEROIDS_GROESSER_5MG"):
      return "steroids-gt"
    default: null
  }
}
