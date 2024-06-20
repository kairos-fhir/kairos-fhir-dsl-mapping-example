package projects.dktk.v2

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.RadiationComponent
import de.kairos.fhir.centraxx.metamodel.RadiationTarget
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.radiationTherapy

/**
 * Represented by a CXX RadiationTherapy
 * Specified by https://simplifier.net/oncology/strahlentherapie
 * @author Mike Wähnert
 * @since CXX.v.2024.3.0
 *
 * Hints:
 * * Not yet supported extensions: Boost
 * * The dose units are free text fields in CXX, but only representations of a double will run through.
 *
 */
procedure {
  id = "Procedure/RadiationTherapy-" + context.source[radiationTherapy().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Procedure-Strahlentherapie"
  }

  status = Procedure.ProcedureStatus.UNKNOWN

  category {
    coding {
      system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTTherapieartCS"
      code = "ST"
      display = "Strahlentherapie"
    }
  }

  subject {
    reference = "Patient/" + context.source[radiationTherapy().patientContainer().id()]
  }

  performedPeriod {
    start {
      date = normalizeDate(context.source[radiationTherapy().therapyStart()] as String)
      precision = TemporalPrecisionEnum.DAY.name()
    }
    end {
      date = normalizeDate(context.source[radiationTherapy().therapyEnd()] as String)
      precision = TemporalPrecisionEnum.DAY.name()
    }
  }

  if (context.source[radiationTherapy().tumour()] && hasRelevantCode(context.source[radiationTherapy().tumour().centraxxDiagnosis().diagnosisCode()] as String)) {
    reasonReference {
      reference = "Condition/" + context.source[radiationTherapy().tumour().centraxxDiagnosis().id()]
    }
  }

  // Ende Grund
  if (context.source[radiationTherapy().finalStateDict()]) {
    outcome {
      coding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/EndeGrundCS"
        code = (context.source[radiationTherapy().finalStateDict().code()] as String)?.toUpperCase()
      }
    }
  }

  if (context.source[radiationTherapy().intentionDict()]) {
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-SYSTIntention"
      valueCoding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTIntentionCS"
        code = context.source[radiationTherapy().intentionDict()]?.getAt(CODE)?.toString()?.toUpperCase()
        display = context.source[radiationTherapy().intentionDict().nameMultilingualEntries()]?.find { it[LANG] == "de" }?.getAt(VALUE) as String
      }
    }
  }

  if (context.source[radiationTherapy().therapyKindDict()]) {
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-StellungZurOp"
      valueCoding {
        system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SYSTStellungOPCS"
        code = context.source[radiationTherapy().therapyKindDict()]?.getAt(CODE)?.toString()?.toUpperCase()
        display = context.source[radiationTherapy().therapyKindDict().nameMultilingualEntries()]?.find { it[LANG] == "de" }?.getAt(VALUE) as String
      }
    }
  }

  context.source[radiationTherapy().radiationComponents()].each { def rc ->
    extension {
      url = "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Extension-Bestrahlung"

      //Applikationsart
      if (rc[RadiationComponent.APPLICATION_KIND_DICT]) {
        extension {
          url = "Applikationsart"
          valueCodeableConcept {
            coding {
              system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/ApplikationsartCS"
              code = mapApplicationKind(rc[RadiationComponent.APPLICATION_KIND_DICT]?.getAt(CODE) as String)
            }
          }
        }
      }

      // Strahlenart
      if (rc[RadiationComponent.RADIATION_KIND_DICT]) {
        extension {
          url = "Strahlenart"
          valueCodeableConcept {
            coding {
              system = "http://dktk.dkfz.de/fhir/onco/core/ValueSet/StrahlenartVS"
              code = mapRadiationKind(rc[RadiationComponent.RADIATION_KIND_DICT]?.getAt(CODE) as String)
            }
          }
        }
      }

      if (rc[RadiationComponent.TARGETS]) {
        extension {
          url = "Zielgebiet"
          valueCodeableConcept {
            rc[RadiationComponent.TARGETS].each { def trg ->
              coding {
                system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/ZielgebietCS"
                code = trg[RadiationTarget.TARGET_AREA] as String
              }
            }
          }
        }
      }

      if (rc[RadiationComponent.TARGETS]) {
        extension {
          url = "SeiteZielgebiet"
          valueCodeableConcept {
            rc[RadiationComponent.TARGETS].each { def trg ->
              coding {
                system = "http://dktk.dkfz.de/fhir/onco/core/CodeSystem/SeitenlokalisationCS"
                code = (trg[RadiationTarget.SIDE_DICT]?.getAt(CODE) as String)?.toUpperCase()
              }
            }
          }
        }
      }


      // Einzeldosis + Einheit
      if (rc[RadiationComponent.RADIATION_KIND_DICT]) {
        extension {
          url = "Einzeldosis"
          valueQuantity {
            value = rc[RadiationComponent.SINGLE_DOSE]
            unit = mapUnit(rc[RadiationComponent.UNIT_DICT]?.getAt(CODE) as String)
          }
        }
      }

      // Gesamtdosis + Einheit
      if (rc[RadiationComponent.RADIATION_KIND_DICT]) {
        extension {
          url = "Gesamtdosis"
          valueQuantity {
            value = rc[RadiationComponent.COMPLETE_DOSE]
            unit = mapUnit(rc[RadiationComponent.UNIT_DICT]?.getAt(CODE) as String)
          }
        }
      }
    }
  }
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

static String mapRadiationKind(String gtdsDictionaryCode) {
  if (gtdsDictionaryCode == null) return null
  if (gtdsDictionaryCode.equalsIgnoreCase("uh")) return "UH"
  if (gtdsDictionaryCode.equalsIgnoreCase("el")) return "EL"
  if (gtdsDictionaryCode.equalsIgnoreCase("ne")) return "NE"
  if (gtdsDictionaryCode.equalsIgnoreCase("pn")) return "PN"
  if (gtdsDictionaryCode.equalsIgnoreCase("si")) return "SI"
  if (gtdsDictionaryCode.equalsIgnoreCase("ro")) return "RO"
  if (gtdsDictionaryCode.equalsIgnoreCase("co")) return "Co-60"
  if (gtdsDictionaryCode.equalsIgnoreCase("so")) return "SO"
  if (gtdsDictionaryCode.equalsIgnoreCase("lu-177")) return "Lu-177"
  if (gtdsDictionaryCode.equalsIgnoreCase("j2")) return "J-131"
  if (gtdsDictionaryCode.equalsIgnoreCase("yt")) return "Y-90"
  if (gtdsDictionaryCode.equalsIgnoreCase("r2")) return "Ra-223"
  if (gtdsDictionaryCode.equalsIgnoreCase("ac-225")) return "Ac-225"
  if (gtdsDictionaryCode.equalsIgnoreCase("sm")) return "Sm-153"
  if (gtdsDictionaryCode.equalsIgnoreCase("tb-161")) return "Tb-161"
  if (gtdsDictionaryCode.equalsIgnoreCase("s1")) return "Sr-89"
  if (gtdsDictionaryCode.equalsIgnoreCase("ir")) return "Ir-192"
  if (gtdsDictionaryCode.equalsIgnoreCase("sonu")) return "SONU"
  return gtdsDictionaryCode.toUpperCase() // without mapping
}

static String mapApplicationKind(final String gtdsDictionaryCode) {
  if (gtdsDictionaryCode == null) return null
  if (gtdsDictionaryCode.equalsIgnoreCase("P")) return "P"
  if (gtdsDictionaryCode.equalsIgnoreCase("PRCJ")) return "PRCJ"
  if (gtdsDictionaryCode.equalsIgnoreCase("PRCN")) return "PRCN"
  if (gtdsDictionaryCode.equalsIgnoreCase("K")) return "K"
  if (gtdsDictionaryCode.equalsIgnoreCase("KHDR")) return "KHDR"
  if (gtdsDictionaryCode.equalsIgnoreCase("KPDR")) return "KPDR"
  if (gtdsDictionaryCode.equalsIgnoreCase("KLDR")) return "KLDR"
  if (gtdsDictionaryCode.equalsIgnoreCase("I")) return "I"
  if (gtdsDictionaryCode.equalsIgnoreCase("IHDR")) return "IHDR"
  if (gtdsDictionaryCode.equalsIgnoreCase("IPDR")) return "IPDR"
  if (gtdsDictionaryCode.equalsIgnoreCase("ILDR")) return "ILDR"
  if (gtdsDictionaryCode.equalsIgnoreCase("M")) return "M"
  if (gtdsDictionaryCode.equalsIgnoreCase("MSIRT")) return "MSIRT"
  if (gtdsDictionaryCode.equalsIgnoreCase("MPRRT")) return "MPRRT"
  if (gtdsDictionaryCode.equalsIgnoreCase("S")) return "S"
  if (gtdsDictionaryCode.equalsIgnoreCase("RCJ")) return "RCJ"
  if (gtdsDictionaryCode.equalsIgnoreCase("RCN")) return "RCN"
  if (gtdsDictionaryCode.equalsIgnoreCase("ST")) return "ST"
  if (gtdsDictionaryCode.toUpperCase().contains("4D")) return "4D"
  if (gtdsDictionaryCode.toUpperCase().contains("HDR")) return "HDR"
  if (gtdsDictionaryCode.toUpperCase().contains("LDR")) return "LDR"
  if (gtdsDictionaryCode.toUpperCase().contains("PDR")) return "PDR"
  if (gtdsDictionaryCode.toUpperCase().contains("SIRT")) return "SIRT"
  if (gtdsDictionaryCode.toUpperCase().contains("PRRT")) return "PRRT"
  if (gtdsDictionaryCode.toUpperCase().contains("PSMA")) return "PSMA"
  if (gtdsDictionaryCode.toUpperCase().contains("RJT")) return "RJT"
  if (gtdsDictionaryCode.toUpperCase().contains("RIT")) return "RIT"
  return gtdsDictionaryCode.toUpperCase() // without mapping
}

static String mapUnit(final String gtdsDictionaryCode) {
  if (gtdsDictionaryCode == null) return null
  if (gtdsDictionaryCode.equalsIgnoreCase("gy")) return "Gy"
  if (gtdsDictionaryCode.equalsIgnoreCase("gbq")) return "GBq"
  if (gtdsDictionaryCode.equalsIgnoreCase("mbq")) return "MBq"
  if (gtdsDictionaryCode.equalsIgnoreCase("kbq")) return "kBq"
  return gtdsDictionaryCode.toUpperCase() // without mapping
}