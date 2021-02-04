package projects.dktk_with_jpa_navigation

import de.kairos.fhir.centraxx.metamodel.AbstractGtdsDictionary
import de.kairos.fhir.centraxx.metamodel.RootEntities
import de.kairos.fhir.centraxx.metamodel.SystemTherapy
import org.hl7.fhir.r4.model.MedicationStatement

import static de.kairos.fhir.centraxx.metamodel.AbstractGtdsDictionary.*
import static de.kairos.fhir.centraxx.metamodel.RootEntities.systemTherapy

/**
 * Represented by a CXX SystemTherapy
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.4
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

  reasonReference {
    reference = "Condition/" + context.source[systemTherapy().tumour().centraxxDiagnosis().id()]
  }

  // TODO which CXX field?
//  extension {
//    url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-StellungZurOp"
//    valueCoding {
//      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTStellungOPCS"
//    }
//  }

  extension {
    url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SYSTIntention"
    valueCoding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTIntentionCS"
      code = context.source[systemTherapy().intentionDict()]?.getAt(CODE)?.toString()?.toUpperCase()
    }
  }

  if (context.source["rClassificationLocalDict.code"]) {
    extension {
      //TODO Declared is a reference to Observation, but Operation uses a valueCoding
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-LokaleResidualstatus"
      valueCoding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/LokaleBeurteilungResidualstatusCS"
        version = context.source["rClassificationLocalDict.version"]
        code = context.source["rClassificationLocalDict.nameMultilingualEntries2"]?.find { it["lang"] == "en" }?.getAt("value")
      }
    }
  }
  //TODO: systemTherapy has no method for rClassificationLocalDict

  if (context.source["rClassificationDict"]) {
    extension {
      //TODO Declared is a reference to Observation, but Operation uses a valueCoding
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-GesamtbeurteilungResidualstatus"
      valueCoding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/GesamtbeurteilungResidualstatusCS"
        version = context.source["rClassificationDict.version"]
        code = context.source["rClassificationDict.nameMultilingualEntries2"]?.find { it["lang"] == "en" }?.getAt("value")
      }
    }
  }
}
