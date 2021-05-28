package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborValue
import org.hl7.fhir.r4.model.MedicationStatement
import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/pharmacologicaltherapyaceinhibitors
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */

medicationStatement {
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "MEDIKATION" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemThera = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_ACE_HEMMER" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemThera){
    return
  }
  if (crfItemThera[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "ACEInhibitors/" + context.source[studyVisitItem().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/pharmacological-therapy-ace-inhibitors"
    }

    status = MedicationStatement.MedicationStatementStatus.UNKNOWN

    medication {
      medicationCodeableConcept {
        crfItemThera[CrfItem.CATALOG_ENTRY_VALUE]?.each { final item ->
          final def ATCcode = matchResponseToATC(item[CatalogEntry.CODE] as String)
          if (ATCcode) {
            coding {
              system = "http://fhir.de/CodeSystem/dimdi/atc"
              code = ATCcode
            }
          }
        }
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }
    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
    }
  }
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}


static String matchResponseToATC(final String resp) {
  switch (resp) {
    case ("COV_JA"):
      return "C09A"
    default: null
  }
}
