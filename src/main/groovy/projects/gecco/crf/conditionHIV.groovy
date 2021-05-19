package projects.gecco.crf

import de.kairos.centraxx.model.service.hl7.hl7messages.helper.dto.CatalogEntryValueDTo
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.Crf
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import org.jboss.jandex.TypeTarget

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient
import static de.kairos.fhir.centraxx.metamodel.RootEntities.studyVisitItem

/**
 * Represented by a CXX StudyVisitItem
 * Specified by https://simplifier.net/forschungsnetzcovid-19/humanimmunodeficiencyvirusinfection
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * NOTE: Due to the Cardinality-restraint (1..1) for "code", multiple selections in CXX for this parameter
 *       will be added as additional codings.
 */


condition {
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "ANAMNESE / RISIKOFAKTOREN" || studyVisitStatus == "OPEN"){
    return //no export
  }
  final def crfItemHIV = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_HIV" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (crfItemHIV[CrfItem.CATALOG_ENTRY_VALUE] != []) {
    id = "HIV/" + context.source[studyVisitItem().crf().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/human-immunodeficiency-virus-infection"
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
    category {
      coding {
        system = "http://snomed.info/sct"
        code = "394807007"
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    code {
      final def ICDcode = matchResponseToICD(crfItemHIV[CrfItem.CATALOG_ENTRY_VALUE][CatalogEntry.CODE] as String)
      if (ICDcode) {
        coding {
          system = "http://fhir.de/CodeSystem/dimdi/icd-10-gm"
          version = "2020"
          code = ICDcode
        }
      }
      final def SNOMEDcode = matchResponseToSNOMED(crfItemHIV[CrfItem.CATALOG_ENTRY_VALUE][CatalogEntry.CODE] as String)
      if (SNOMEDcode) {
        coding {
          system = "http://snomed.info/sct"
          code = SNOMEDcode
        }
      }
    }
    recordedDate {
      recordedDate = crfItemHIV[CrfItem.CREATIONDATE]
    }
  }
}


static String matchResponseToICD(final String resp) {
  switch (resp) {
    case ("COV_NEIN"):
      return null
    case ("[COV_JA]"):
      return "B24"
    case ("[COV_NA]"):
      return "Unknown"
    default: null
  }
}

static String matchResponseToSNOMED(final String resp) {
  switch (resp) {
    case ("COV_NEIN"):
      return null
    case ("[COV_JA]"):
      return "86406008"
    case ("[COV_NA]"):
      return "261665006"
    default: null
  }
}