package projects.gecco


import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue

import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/patient
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * hints:
 *  A StudyEpisode is no regular episode and cannot reference an encounter
 */
patient {
  final def studyCode = context.source[studyVisitItem().studyMember().study().code()]
  if (studyCode != "SARS-Cov-2") {
    return //no export
  }
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "SarsCov2_DEMOGRAPHIE" || studyVisitStatus == "OPEN") {
    return //no export
  }

  id = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]

  meta {
    source = "https://fhir.centraxx.de"
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/Patient"
  }

  final def crfItemEthn = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_ETHNISCHE_ZUGEHOERIGKEIT" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemEthn) {
    crfItemEthn[CrfItem.CATALOG_ENTRY_VALUE][CatalogEntry.CODE]?.each { final ethn ->
      extension {
        url = "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/ethnic-group"
        valueCoding {
          system = "http://snomed.info/sct"
          code = mapEthnicityCode(ethn as String)
        }
      }
    }
  }


  extension {
    url = "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/age"
    extension {
      url = "dateTimeOfDocumentation"
      valueDateTime = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
    }


    final def crfItemAge = context.source[studyVisitItem().crf().items()].find {
      "COV_GECCO_GEBURTSDATUM" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
    }


    if (crfItemAge) {
      final def ageDate = crfItemAge[CrfItem.DATE_VALUE]
      extension {
        url = "age"
        valueAge {
          value = computeAge(ageDate as String)
          system = "http://unitsofmeasure.org"
          code = "a"
          unit = "years"
        }
      }
      birthDate = ageDate.toString().substring(6, 16)
    }
  }

  active = context.source[studyVisitItem().studyMember().patientContainer().patientStatus()]

  final def crfItemGender = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_Geschlecht_GEBURT" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemGender) {
    crfItemGender[CrfItem.CATALOG_ENTRY_VALUE][CatalogEntry.CODE]?.each { final gen ->
      gender = mapGender(gen as String)
    }
  }
}


static String mapGender(final String gender) {
  switch (gender) {
    case "COV_MAENNLICH":
      return "male"
    case "COV_WEIBLICH":
      return "female"
    case "COV_KEINE_ANGABE":
      return "unknown"
    case "COV_DIVERS":
      return "other"
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}


//Compute age of patient from birthdate
static int computeAge(final String dateString) {
  final int now = Calendar.getInstance().get(Calendar.YEAR)
  final int doe = dateString.substring(6, 10).toInteger()
  return now - doe
}

//Function to map ethnicities
static String mapEthnicityCode(final String ethnicity) {
  switch (ethnicity) {
    case "COV_KAUKASIER":
      return "14045001"
    case "COV_AFRIKANER":
      return "18167009"
    case "COV_ASIATE":
      return "315280000"
    case "COV_ARABISCH":
      return "90027003"
    case "COV_LATEIN_AMERIKANISCH":
      return "2135-2"
    case "COV_186019001":
      return "26242008"
    default:
      return "261665006"
  }
}


/*

final def DateOfDeath = "2020-01-01"//context.source[studyVisitItem().studyMember().patientContainer().dateOfDeath()] //TODO
static int computeAge(final String dateString, final String dateOfDeath) {
  if (dateOfDeath){
    final int dod = dateOfDeath.substring(0,4).toInteger()
    final int doe = dateString.substring(0, 4).toInteger()
    return dod - doe
  }
  else{
    final int now = Calendar.getInstance().get(Calendar.YEAR)
    final int doe = dateString.substring(0, 4).toInteger()
    return now - doe
  }
}

 */
