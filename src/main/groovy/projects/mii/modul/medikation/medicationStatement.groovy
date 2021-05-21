package projects.mii.modul.medikation

import org.hl7.fhir.r4.model.MedicationStatement

import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

/**
 * Represented by a CXX Medication
 * Specified by https://simplifier.net/medizininformatikinitiative-modulmedikation/medicationstatement-duplicate-3
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 * TODO: work in progress
 */
medicationStatement {

  id = "MedicationStatement/" + context.source[medication().id()]

  meta {
    source = "urn:centraxx"
    profile("https://www.medizininformatik-initiative.de/fhir/core/modul-medikation/StructureDefinition/MedicationStatement")
  }

  status = MedicationStatement.MedicationStatementStatus.UNKNOWN

  medication {
    medicationReference {
      reference = "Medication/" + context.source[medication().id()]
    }
  }

  subject {
    reference = "Patient/" + context.source[medication().patientContainer().id()]
  }

  context_ {
    reference = "Encounter/" + context.source[medication().episode().id()]
  }

  effectiveDateTime = context.source[medication().trgDate()]

  effectivePeriod {
    start = context.source[medication().observationBegin()]
    end = context.source[medication().observationEnd()]
  }

  dosage {
    text = context.source[medication().dosisSchema()] as String
    patientInstruction = context.source[medication().notes()]

    route {
      coding {
        system = "urn:centraxx"
        code = context.source[medication().methodOfApplication()]
      }
    }


    doseAndRate {
      doseQuantity {
        value = context.source[medication().dosis()]
        system = "http://unitsofmeasure.org"
        unit = context.source[medication().unit().code()]
      }
    }

  }
}
