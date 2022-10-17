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

  category {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTTherapieartCS"
      code = findCategoryByProtocolCode(context.source[systemTherapy().protocolTypeDict().code()] as String)
    }
  }

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

static String findCategoryByProtocolCode(final String protocolCode) {
  if (protocolCode == null) {
    return "SO" // Sonstiges
  }
  // Active Surveillance
  if ("a".equalsIgnoreCase(protocolCode)) {
    return "AS"
  }
  // Bisphosphonate, Chemotherapie, Mono-Chemotherapie, Poly-Chemotherapie, Regionale Perfusion
  else if (containsIgnoreCase(["bp", "c", "ch", "cm", "cp", "cr"], protocolCode)) {
    return "CH"
  }
  // Hormontherapie
  else if (containsIgnoreCase(["h", "ho"], protocolCode)) {
    return "HO"
  }
  // Immun- und Antikörpertherapie, Immuntherapie o.n.A., Immunchemotherapie, Immun- und Antikörpertherapie,Unspezifische Immuntherapie
  else if (containsIgnoreCase(["i", "ic", "im", "is", "iu"], protocolCode)) {
    return "IM"
  }
  // Knochenmark-/Stammzelltransplantation, Knochenmarktransplantation, Konditionierung für KMT
  else if (containsIgnoreCase(["k", "km"], protocolCode)) {
    return "KM"
  }
  // Wait and see
  else if (containsIgnoreCase(["w", "ws"], protocolCode)) {
    return "WS"
  }
  // Zielgerichtete Substanzen
  else if (containsIgnoreCase(["z", "zs"], protocolCode)) {
    return "ZS"
  }
  // Sonstiges, Unbekannt
  else {
    return "SO"
  }
}

static boolean containsIgnoreCase(final List<String> codeList, final String codeToCheck) {
  return codeList.stream().anyMatch({ it.equalsIgnoreCase(codeToCheck) })
}