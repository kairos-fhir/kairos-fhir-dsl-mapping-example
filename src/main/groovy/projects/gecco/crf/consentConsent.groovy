package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.ConsentableAction
import de.kairos.fhir.centraxx.metamodel.OrganisationUnit
import org.hl7.fhir.r4.model.Consent
import org.hl7.fhir.r4.model.DateTimeType

import static de.kairos.fhir.centraxx.metamodel.RootEntities.consent

/**
 * Represented by a CXX Consent
 * Specified by http://fhir.de/ConsentManagement/StructureDefinition/Consent
 * @author Lukas Reinert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */

consent {

  final def consentCode = context.source[consent().consentType().code()]
  if (consentCode != "GECCO_CONSENT") {
    return //no export
  }

  //id = "Consent/Consent-" + context.source[consent().id()]
  meta{
    source = "https://fhir.centraxx.de"
    profile "http://fhir.de/ConsentManagement/StructureDefinition/Consent"
  }


  final def validUntil = context.source[consent().validUntil().date()]
  final def validFrom = context.source[consent().validFrom().date()]
  final String interpretedStatus = validUntilInterpreter(validUntil as String)
  final def studyID = context.source[consent().consentType().flexiStudy().id()]
  extension {
    url = "http://fhir.de/ConsentManagement/StructureDefinition/DomainReference"
    extension{
      url = "domain"
      valueReference {
        reference = "ResearchStudy/" + studyID
      }
    }
    extension{
      url = "status"
      valueCoding {
        system = "http://hl7.org/fhir/publication-status"
        code = interpretedStatus
      }
    }
  }

  status = interpretedStatus

  scope {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/consentscope"
      code = "research"
    }
  }

  category {
    coding {
      system = "http://loinc.org"
      code = "57016-8"
    }
  }

  patient{
    reference = "Patient/" +context.source[consent().patientContainer().id()]
  }

  dateTime = context.source[consent().validFrom()] as DateTimeType


  context.source[consent().patientContainer().organisationUnits()].each { final oe ->
    organization {
      reference = "Organisationseinheit/" + oe[OrganisationUnit.ID]
      display = oe[OrganisationUnit.CODE]
    }
  }

  sourceReference {
    reference = "PatientConsent/" + context.source[consent().id()]
  }

  policyRule {
    text = "Patienteneinwilligung MII|1.6.f"
  }

  final def consentParts = []
  final def consentPartsOnly = context.source[consent().consentPartsOnly()]


  if (consentPartsOnly == false){
    consentParts.addAll("IDAT_bereitstellen_EU_DSGVO_konform",
            "IDAT_erheben",
            "IDAT_speichern_verarbeiten",
            "IDAT_zusammenfuehren_Dritte",
            "MDAT_erheben",
            "MDAT_speichern_verarbeiten",
            "MDAT_wissenschaftlich_nutzen_EU_DSGVO_konform",
            "MDAT_zusammenfuehren_Dritte",
            "Rekontaktierung_Verknuepfung_Datenbanken",
            "Rekontaktierung_weitere_Erhebung",
            "Rekontaktierung_weitere_Studien",
            "MDAT_GECCO83_bereitstellen_NUM_CODEX",
            "MDAT_GECCO83_speichern_verarbeiten_NUM_CODEX",
            "MDAT_GECCO83_wissenschaftlich_nutzen_COVID_19_Forschung_EU_DSGVO_konform",
            "MDAT_GECCO83_wissenschaftlich_nutzen_Pandemie_Forschung_EU_DSGVO_konform",
            "Rekontaktierung_Zusatzbefund")
  }
  else if (consentPartsOnly == true){
    consentParts.addAll(context.source[consent().consentElements().consentableAction().code()])
  }


  provision {
    consentParts.each{ final cA ->
      type = Consent.ConsentProvisionType.PERMIT
      period {
        start = validFrom
        end = validUntil
      }
      code{
        coding{
          system = "https://ths-greifswald.de/fhir/CodeSystem/gics/Policy"
          code = mapConsentCode(cA as String)
          display = mapConsentDisplay(cA as String)
        }
      }
    }
  }


}

static String validUntilInterpreter(String validFromDate){
  def fromDate = Date.parse("yyyy-MM-dd", validFromDate.substring(0,10))
  if(!validFromDate){
    return "active"
  }
  else{
    def currDate = new Date()
    final def res = currDate <=> (fromDate)
    if (res == 0){
      return "active"
    }
    else if (res == 1){
      return "inactive"
    }
  }
}

static String mapConsentCode(final String cxxConsentPart){
  switch(cxxConsentPart){
    case ("IDAT_bereitstellen_EU_DSGVO_konform"):
      return "IDAT_bereitstellen_EU_DSGVO_konform"
    case ("IDAT_erheben"):
      return "IDAT_erheben"
    case ("IDAT_speichern_verarbeiten"):
      return "IDAT_speichern_verarbeiten"
    case ("IDAT_zusammenfuehren_Dritte"):
      return "IDAT_zusammenfuehren_Dritte"
    case ("MDAT_erheben"):
      return "MDAT_erheben"
    case ("MDAT_speichern_verarbeiten"):
      return "MDAT_speichern_verarbeiten"
    case ("MDAT_wissenschaftlich_nutzen_EU_DSGVO_konform"):
      return "MDAT_wissenschaftlich_nutzen_EU_DSGVO_konform"
    case ("MDAT_zusammenfuehren_Dritte"):
      return "MDAT_zusammenfuehren_Dritte"
    case ("Rekontaktierung_Verknuepfung_Datenbanken"):
      return "Rekontaktierung_Verknuepfung_Datenbanken"
    case ("Rekontaktierung_weitere_Erhebung"):
      return "Rekontaktierung_weitere_Erhebung"
    case ("Rekontaktierung_weitere_Studie"):
      return "Rekontaktierung_weitere_Studie"
    case ("MDAT_GECCO83_bereitstellen_NUM_CODEX"):
      return "MDAT_GECCO83_bereitstellen_NUM_CODEX"
    case ("MDAT_GECCO83_speichern_verarbeiten_NUM_CODEX"):
      return "MDAT_GECCO83_speichern_verarbeiten_NUM_CODEX"
    case ("MDAT_GECCO83_wissenschaftlich_nutzen_COVID_19_Forschung_EU_DSGVO_konform"):
      return "MDAT_GECCO83_wissenschaftlich_nutzen_COVID_19_Forschung_EU_DSGVO_konform"
    case ("MDAT_GECCO83_wissenschaftlich_nutzen_Pandemie_Forschung_EU_DSGVO_konform"):
      return "MDAT_GECCO83_wissenschaftlich_nutzen_Pandemie_Forschung_EU_DSGVO_konform"
    case ("Rekontaktierung_Zusatzbefund"):
      return "Rekontaktierung_Zusatzbefund"
  }
}

static String mapConsentDisplay(final String cxxConsentPart){
  switch(cxxConsentPart){
    case ("IDAT_bereitstellen_EU_DSGVO_konform"):
      return "Herausgabe identifizierender Daten (IDAT)[...]"
    case ("IDAT_erheben"):
      return "Erfassung neuer identifizierender Daten (IDAT)"
    case ("IDAT_speichern_verarbeiten"):
      return "Speicherung und Verarbeitung identifizierender Daten (IDAT)[...]"
    case ("IDAT_zusammenfuehren_Dritte"):
      return "Zusammenführung identifizierender Daten (IDAT)[...]"
    case ("MDAT_erheben"):
      return "Erfassung medizinischer Daten"
    case ("MDAT_speichern_verarbeiten"):
      return "Speicherung_Verarbeitung von medizinischen Daten[...]"
    case ("MDAT_wissenschaftlich_nutzen_EU_DSGVO_konform"):
      return "Bereitstellung medizinischer Daten (MDAT)[...]"
    case ("MDAT_zusammenfuehren_Dritte"):
      return "Zusammenführung medizinischer Daten (MDAT)[...]"
    case ("Rekontaktierung_Verknuepfung_Datenbanken"):
      return "Rekontaktierung zur Verknüpfung[...]"
    case ("Rekontaktierung_weitere_Erhebung"):
      return "Rekontaktierung bezüglich Erhebung zusätzlicher Daten"
    case ("Rekontaktierung_weitere_Studie"):
      return "Rekontaktierung bezüglich Information[...]"
    case ("MDAT_GECCO83_bereitstellen_NUM_CODEX"):
      return "Medizinische Daten des GECCO83 Datensatz[...]"
    case ("MDAT_GECCO83_speichern_verarbeiten_NUM_CODEX"):
      return "Medizinische Daten des GECCO83 Datensatz[...]"
    case ("MDAT_GECCO83_wissenschaftlich_nutzen_COVID_19_Forschung_EU_DSGVO_konform"):
      return "Nutzung des Covid-19-Datensatzes[...]"
    case ("MDAT_GECCO83_wissenschaftlich_nutzen_Pandemie_Forschung_EU_DSGVO_konform"):
      return "Nutzung des Covid-19-Datensatzes[...]"
    case ("Rekontaktierung_Zusatzbefund"):
      return "Rekontaktierung bezüglich Zusatzbefund"
  }
}


