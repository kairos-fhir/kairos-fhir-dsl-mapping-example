package projects.dktk.v2

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import org.hl7.fhir.r4.model.MedicationStatement

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
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

  identifier {
    value = context.source[systemTherapy().systemTherapyId()]
  }

  status = MedicationStatement.MedicationStatementStatus.UNKNOWN

  category {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTTherapieartCS"
      code = findCategoryByProtocolCode(context.source[systemTherapy().protocolTypeDict().code()] as String)
    }
  }

  medication {
    medicationCodeableConcept {
      text = context.source[systemTherapy().description()] as String
    }
  }

  subject {
    reference = "Patient/" + context.source[systemTherapy().patientContainer().id()]
  }

  effectivePeriod {
    start {
      date = normalizeDate(context.source[systemTherapy().therapyStart()] as String)
      precision = TemporalPrecisionEnum.DAY.name()
    }
    end {
      date = normalizeDate(context.source[systemTherapy().therapyEnd()] as String)
      precision = TemporalPrecisionEnum.DAY.name()
    }
  }

  if (context.source[systemTherapy().tumour()] && hasRelevantCode(context.source[systemTherapy().tumour().centraxxDiagnosis().diagnosisCode()] as String)) {
    reasonReference {
      reference = "Condition/" + context.source[systemTherapy().tumour().centraxxDiagnosis().id()]
    }
  }

  if (context.source[systemTherapy().intentionDict()]) {
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SYSTIntention"
      valueCodeableConcept {
        coding {
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTIntentionCS"
          code = context.source[systemTherapy().intentionDict()]?.getAt(CODE)?.toString()?.toUpperCase()
        }
      }
    }
  }

  if (context.source[systemTherapy().protocolTypeDict()]) {
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SystemischeTherapieProtokoll"
      valueCodeableConcept {
        coding {
          // see http://fhir.org/guides/stats/codesystem-hl7.org.nz.fhir.ig.cca-sact-regimen-code.html
          system = "https://standards.digital.health.nz/ns/sact-regimen-code"
          code = context.source[systemTherapy().protocolTypeDict()]?.getAt(CODE)?.toString()
        }
      }
    }
  }

  // Systemtherapie-Art => StellungZurOp
  if (context.source[systemTherapy().therapyKindDict()]) {
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-StellungZurOp"
      valueCodeableConcept {
        coding {
          system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTStellungOPCS"
          code = context.source[systemTherapy().therapyKindDict()]?.getAt(CODE)?.toString()?.toUpperCase()
          display = context.source[systemTherapy().therapyKindDict().nameMultilingualEntries()]?.find { it[LANG] == "de" }?.getAt(VALUE) as String
        }
      }
    }
  }

  // Systemtherapie-Typ => StellungZurOp
  if (context.source[systemTherapy().therapyTypeDict()]) {
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-StellungZurOp"
      valueCodeableConcept {
        coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTStellungOPCS"
        code = context.source[systemTherapy().therapyTypeDict()]?.getAt(CODE)?.toString()?.toUpperCase()
        display = context.source[systemTherapy().therapyTypeDict().nameMultilingualEntries()]?.find { it[LANG] == "de" }?.getAt(VALUE) as String
        }
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

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static boolean hasRelevantCode(final String icdCode) {
  return icdCode != null && (icdCode.toUpperCase().startsWith('C') || icdCode.toUpperCase().startsWith('D'))
}