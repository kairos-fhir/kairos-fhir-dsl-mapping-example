package projects.gecco.umg

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.Consent
import org.hl7.fhir.r4.model.DateTimeType
import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/donotresuscitateorder
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */
consent {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2") {
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "GECCO - ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemDNR = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_DNR_STATUS" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemDNR) {
    return //no export
  }
  if (crfItemDNR[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "Consent/DoNotResucitateOrder-" + context.source[studyVisitItem().crf().id()]

    meta {
      source = "https://fhir.centraxx.de"
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
      reference = "Patient/UMG-CXX-" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    // When this Consent was issued / created / indexed. - Comments: This is not the time of the original consent, but the time that this statement was made or derived.
    // Gibt wohl besser dafuer geeignetes Quelldatenelement (bisher noch nicht so tief in alle verfügbaren ECRF Felder geschaut)
    //dateTime = new DateTimeType(normalizeDate(context.source[studyVisitItem().studyMember().patientContainer().creationDate()] as String))
    dateTime = new DateTimeType(normalizeDate(context.source[studyVisitItem().creationDate()] as String))

    policy {
      uri = "https://www.aerzteblatt.de/archiv/65440/DNR-Anordnungen-Das-fehlende-Bindeglied"
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
    case ("COV_UNBEKANNT"):
      return "261665006"
    default: null
  }
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15"
 */
static String normalizeDate(final String dateTimeString) {
  if (dateTimeString){
    if (dateTimeString.length() >= 10){
      return dateTimeString.substring(0, 10)
    }
  }
  else{
    return dateTimeString
  }
}
