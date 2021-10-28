package projects.dktk.v2


import org.hl7.fhir.r4.model.MedicationStatement

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.systemTherapy

/**
 * Represented by a CXX SystemTherapy
 * Specified by https://simplifier.net/oncology/systemtherapie
 *
 * Hints:
 * There is no representation in a CXX SystemTherapy for the Extensions StellungZurOp, LokaleResidualstatus and GesamtbeurteilungResidualstatus
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6
 */
medicationStatement {

  id = "MedicationStatement/SystemTherapy-" + context.source[systemTherapy().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-MedicationStatement-Systemtherapie"
  }

  status = MedicationStatement.MedicationStatementStatus.UNKNOWN

  identifier {
    value = context.source[systemTherapy().systemTherapyId()]
  }

  subject {
    reference = "Patient/" + context.source[systemTherapy().patientContainer().id()]
  }

  if (context.source[systemTherapy().episode()]) {
    context_ {
      reference = "Encounter/" + context.source[systemTherapy().episode().id()]
    }
  }

  if (context.source[systemTherapy().tumour()]) {
    reasonReference {
      reference = "Condition/" + context.source[systemTherapy().tumour().centraxxDiagnosis().id()]
    }
  }

  if (context.source[systemTherapy().intentionDict()]) {
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SYSTIntention"
      valueCoding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTIntentionCS"
        code = context.source[systemTherapy().intentionDict()]?.getAt(CODE)?.toString()?.toUpperCase()
      }
    }
  }
}
