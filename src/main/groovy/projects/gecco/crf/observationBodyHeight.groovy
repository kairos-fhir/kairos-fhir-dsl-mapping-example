package projects.gecco.crf

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
 * Specified by https://simplifier.net/forschungsnetzcovid-19/bodyheight
 * @author Lukas Reinert, Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * hints:
 *  A StudyEpisode is no regular episode and cannot reference an encounter
 */
observation {
  final def crfName = context.source[studyVisitItem().template().crfTemplate().name()]
  final def studyVisitStatus = context.source[studyVisitItem().status()]
  if (crfName != "DEMOGRAPHIE" || studyVisitStatus == "OPEN") {
    return //no export
  }
  final def crfItemHeight = context.source[studyVisitItem().crf().items()].find {
    "COV_GECCO_GROESSE" == it[CrfItem.TEMPLATE]?.getAt(CrfTemplateField.LABOR_VALUE)?.getAt(LaborValue.CODE)
  }
  if (!crfItemHeight){
    return
  }
  if (crfItemHeight[CrfItem.NUMERIC_VALUE]) {
    id = "BodyHeight/" + context.source[studyVisitItem().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/body-height"
    }

    status = Observation.ObservationStatus.UNKNOWN

    category{
      coding{
        system = "http://terminology.hl7.org/CodeSystem/observation-category"
        code = "vital-signs"
      }
    }

    code {
      coding {
        system = "http://loinc.org"
        code = "8302-2"
      }
      coding {
        system = "http://snomed.info/sct"
        code = "248334005"
      }
    }

    subject {
      reference = "Patient/" + context.source[studyVisitItem().studyMember().patientContainer().id()]
    }

    effectiveDateTime {
      date = normalizeDate(context.source[studyVisitItem().crf().creationDate()] as String)
    }




    crfItemHeight[CrfItem.NUMERIC_VALUE]?.each { final item ->
      if (item) {
        valueQuantity {
          value = item
          unit = "[cm]"
          system = "http://unitsofmeasure.org"
          code = "cm"
        }
      }
    }
  }
}


static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}


