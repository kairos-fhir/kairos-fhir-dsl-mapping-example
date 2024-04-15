package projects.dktk.v2

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType

import static de.kairos.fhir.centraxx.metamodel.AbstractSample.PARENT
import static de.kairos.fhir.centraxx.metamodel.RootEntities.abstractSample
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX AbstractSample
 *
 * Specified by https://simplifier.net/oncology/oncospecimen
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 *
 * Hints:
 *  * CCP-IT 2023-07-13: Always export aliquots with parent references
 *
 */
specimen {

  final String sampleTypeCode = context.source[abstractSample().sampleType().code()] as String
  if (matchIgnoreCase(["TBL", "LES", "UBK", "ZZZ", "NRT"], sampleTypeCode)) {
    return //"Leerschnitt", "Unbekannt" are filtered
  }

  id = "Specimen/" + context.source[abstractSample().id()]

  final def idc = context.source[sample().idContainer()].find {
    "EXLIQUID" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Specimen-OncoSpecimen"
  }

  if (idc) {
    identifier {
      value = idc[IdContainer.PSN]
      type {
        coding {
          system = "urn:centraxx"
          code = idc[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
      }
      system = "http://dktk.dkfz.de/fhir/sid/exliquid-specimen"
    }
  }

  status = context.source[abstractSample().restAmount().amount()] > 0 ? "available" : "unavailable"

  if (context.source[PARENT] != null) {
    parent {
      reference = "Specimen/" + context.source[sample().parent().id()]
    }
  }

  type {
    // 0. First coding is the CXX sample type code. If mapping is missing, this code might help to identify the source value.
    coding {
      system = "urn:centraxx"
      code = context.source[abstractSample().sampleType().code()]
    }

    final String sampleKind = context.source[abstractSample().sampleType().kind()] as String
    final String stockType = context.source[abstractSample().stockType().code()] as String

    // 0. Site specific CXX sample type code => BBMRI SampleMaterialType.
    final String bbmriCode0 = codeToSampleType(sampleTypeCode, stockType, sampleKind)
    if (bbmriCode0 != null) {
      coding {
        system = "https://fhir.bbmri.de/CodeSystem/SampleMaterialType"
        code = bbmriCode0
      }
    } // 1. Without mapping, if CXX code and BBMRI code is the same.
    else if (isBbmriSampleTypeCode(sampleTypeCode)) {
      coding {
        system = "https://fhir.bbmri.de/CodeSystem/SampleMaterialType"
        code = sampleTypeCode.toLowerCase()
      }
    } // 2. CXX sample type SPREC => BBMRI SampleMaterialType.
    else if (context.source[abstractSample().sampleType().sprecCode()]) {
      final String bbmriCode = sprecToBbmriSampleType(context.source[abstractSample().sampleType().sprecCode()] as String)
      if (bbmriCode != null) {
        coding {
          system = "https://fhir.bbmri.de/CodeSystem/SampleMaterialType"
          code = bbmriCode
        }
      }
      coding { // SPREC code is exported as second coding to identify mapping leaks.
        system = "https://doi.org/10.1089/bio.2017.0109"
        code = context.source[abstractSample().sampleType().sprecCode()]
      }
    } else { // 3. CXX sample kind => BBMRI SampleMaterialType.
      final String bbmriCode = sampleKindToBbmriSampleType(sampleKind)
      if (bbmriCode != null) {
        coding {
          system = "https://fhir.bbmri.de/CodeSystem/SampleMaterialType"
          code = bbmriCode
        }
      }
    }
  }

  subject {
    reference = "Patient/" + context.source[abstractSample().patientContainer().id()]
  }

  receivedTime {
    date = normalizeDate(context.source[abstractSample().samplingDate().date()] as String)
    precision = TemporalPrecisionEnum.DAY.name()
  }

  final def ucum = context.conceptMaps.builtin("centraxx_ucum")
  collection {
    collectedDateTime {
      date = normalizeDate(context.source[abstractSample().samplingDate().date()] as String)
      quantity {
        value = context.source[abstractSample().initialAmount().amount()] as Number
        unit = ucum.translate(context.source[abstractSample().initialAmount().unit()] as String)?.code
        system = "http://unitsofmeasure.org"
      }
    }
  }

  container {
    if (context.source[abstractSample().receptable()]) {
      identifier {
        value = context.source[abstractSample().receptable().code()]
        system = "urn:centraxx"
      }
    }

    specimenQuantity {
      value = context.source[abstractSample().restAmount().amount()] as Number
      unit = ucum.translate(context.source[abstractSample().restAmount().unit()] as String)?.code
      system = "http://unitsofmeasure.org"
    }
  }

//  if (context.source[abstractSample().organisationUnit()]) {
//    extension {
//      url = "https://fhir.bbmri.de/StructureDefinition/Custodian"
//      valueReference {
//        reference = "Organization/" + context.source[abstractSample().organisationUnit().id()]
//      }
//    }
//  }

  if (context.source[sample().diagnosis()]) {
    extension {
      url = "https://fhir.bbmri.de/StructureDefinition/SampleDiagnosis"
      valueCodeableConcept {
        coding {
          system = "http://hl7.org/fhir/sid/icd-10"
          code = context.source[sample().diagnosis().diagnosisCode()]
        }
      }
    }
  }

  final def temperature = toTemperature(context)
  if (temperature) {
    extension {
      url = "https://fhir.bbmri.de/StructureDefinition/StorageTemperature"
      valueCodeableConcept {
        coding {
          system = "https://fhir.bbmri.de/CodeSystem/StorageTemperature"
          code = temperature
        }
      }
    }
  }

}

static def toTemperature(final ctx) {
  final def temp = ctx.source[abstractSample().sampleLocation().temperature() as String]
  if (null != temp) {
    switch (temp) {
      case { it >= 2.0 && it <= 10 }: return "temperature2to10"
      case { it <= -18.0 && it >= -35.0 }: return "temperature-18to-35"
      case { it <= -60.0 && it >= -85.0 }: return "temperature-60to-85"
    }
  }

  final def sprec = ctx.source[abstractSample().receptable().sprecCode() as String]
  if (null != sprec) {
    switch (sprec) {
      case ['C', 'F', 'O', 'Q']: return "temperatureLN"
      case ['A', 'D', 'J', 'L', 'N', 'O', 'S']: return "temperature-60to-85"
      case ['B', 'H', 'K', 'M', 'T']: return "temperature-18to-35"
      default: return "temperatureOther"
    }
  }

  return null
}

static String sprecToBbmriSampleType(final String sprecCode) {
  if (sprecCode == null) {
    return null
  } else if (sprecCode == "ASC") { //Ascites fluid
    return "ascites"
  } else if (["AMN", //Amniotic fluid
              "BAL", //Bronchoalveolar lavage
              "BMK", //Breast milk
              "BUC", //Buccal cells
              "CEN", //Fresh cells from non blood specimen type
              "CLN", //Cells from non blood specimen type(eg disrupted tissue), viable
              "CRD", //Cord blood
              "NAS", //Nasal washing
              "PEL", //Ficoll mononuclear cells, non viable
              "PEN", //Cells from non blood specimen type (eg disrupted tissue), non viable
              "PFL", //Pleural fluid
              "RBC", //Red blood cells
              "SEM", //Semen
              "SPT", //Sputum
              "SYN", //Synovial fluid
              "TER"  // Tears
  ].contains(sprecCode)) {
    return "liquid-other"
  } else if (sprecCode == "BLD") { //Blood (whole)
    return "whole-blood"
  } else if (sprecCode == "BMA") { //Bone marrow aspirate
    return "bone-marrow"
  } else if (["BUF", //Unficolled buffy coat, viable
              "BFF" //Unficolled buffy coat, non-viable
  ].contains(sprecCode)) {
    return "buffy-coat"
  } else if (sprecCode == "CEL") {// Ficoll mononuclear cells viable
    return "peripheral-blood-cells-vital"
  } else if (sprecCode == "CSF") {//Cerebrospinal fluid
    return "csf-liquor"
  } else if (sprecCode == "DWB") {//Dried whole blood (e.g. Guthrie cards)
    return "dried-whole-blood"
  } else if (["PL1", //Plasma, single spun
              "PL2" //Plasma, double spun
  ].contains(sprecCode)) {
    return "blood-plasma"
  } else if (sprecCode == "SAL") {//Saliva
    return "saliva"
  } else if (sprecCode == "SER") {//Serum
    return "blood-serum"
  } else if (sprecCode == "STL") {//Stool
    return "stool-faeces"
  } else if (["U24", //24 h urine
              "URN", //Urine, random ("spot")
              "URM", //Urine, first morning
              "URT"  //Urine, timed
  ].contains(sprecCode)) {
    return "urine"
  } else if (sprecCode == "ZZZ") {//other
    return "derivative-other"
  } else if (["BON", //Bone
              "FNA", //Cells from fine needle aspirate
              "HAR", //Hair
              "LCM", //Cells from laser capture microdissected tissue
              "NAL", //Nails
              "PLC", //Placenta
              "TIS", //Solid tissue
              "TCM", //Cells from disrupted tissue
              "TTH"  //Teeth
  ].contains(sprecCode)) {
    return "tissue-other"
  }
  return null // no match
}

static String sampleKindToBbmriSampleType(final String sampleKind) {
  if (sampleKind == null) {
    return null
  }

  switch (sampleKind) {
    case "TISSUE": return "tissue-other"
    case "LIQUID": return "liquid-other"
    default: return "derivative-other" // eg CXX cells
  }
}

static boolean isBbmriSampleTypeCode(final String sampleTypeCode) {
  return ["whole-blood",
          "bone-marrow",
          "buffy-coat",
          "dried-whole-blood",
          "peripheral-blood-cells-vital",
          "blood-plasma",
          "plasma-edta",
          "plasma-citrat",
          "plasma-heparin",
          "plasma-cell-free",
          "plasma-other",
          "blood-serum",
          "ascites",
          "csf-liquor",
          "saliva",
          "stool-faeces",
          "urine",
          "swab",
          "liquid-other",
          "tissue-ffpe",
          "tumor-tissue-ffpe",
          "normal-tissue-ffpe",
          "other-tissue-ffpe",
          "tissue-frozen",
          "tumor-tissue-frozen",
          "normal-tissue-frozen",
          "other-tissue-frozen",
          "tissue-other",
          "dna",
          "cf-dna",
          "g-dna",
          "rna",
          "derivative-other"].stream().anyMatch({ it.equalsIgnoreCase(sampleTypeCode) })
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String codeToSampleType(final String sampleTypeCode, final String stockType, final String sampleKindCode) {
  if (null == sampleTypeCode) {
    return null
  }

  switch (sampleTypeCode) {
    case { matchIgnoreCase(["whole-blood", "BLD", "VBL", "Vollblut", "TBL", "EDTA-PB", "Heparinblut"], sampleTypeCode) }: return "whole-blood"
    case { matchIgnoreCase(["KNM", "bone-marrow", "Knochenmark", "BMA", "EDTAKM", "KM", "Knochenmark"], sampleTypeCode) }: return "bone-marrow"
    case { matchIgnoreCase(["BUFFYCOAT", "BuffyCoat", "BUF", "buffy-coat", "BUFFYCOATNOTVIABLE"], sampleTypeCode) }: return "buffy-coat"
    case { matchIgnoreCase(["TBK", "BF"], sampleTypeCode) }: return "dried-whole-blood"
    case { matchIgnoreCase(["PBMC", "PBMC-nl", "PBMCs", "PBMC-l", "PEL"], sampleTypeCode) }: return "peripheral-blood-cells-vital"
    case {
      matchIgnoreCase(["PLA", "blood-plasma", "PL1", "Plasma", "P", "HEPAP", "P-cf", "EDTAFCPMA", "EDTA-APRPMA", "EP, CP", "EPPL2", "EPPL1",
                       "plasma-edta", "plasma-citrat", "plasma-heparin", "plasma-cell-free", "plasma-other", "HEPAPPBS"], sampleTypeCode)
    }: return "blood-plasma"
    case { matchIgnoreCase(["SER", "blood-serum", "Serum"], sampleTypeCode) }: return "blood-serum"
    case { matchIgnoreCase(["ASC"], sampleTypeCode) }: return "ascites"
    case { matchIgnoreCase(["LQR", "CSF", "csf-liquor", "Liquor"], sampleTypeCode) }: return "csf-liquor"
    case { matchIgnoreCase(["SPEICHEL", "SAL"], sampleTypeCode) }: return "saliva"
    case { matchIgnoreCase(["STL", "STLctr"], sampleTypeCode) }: return "stool-faeces"
    case { matchIgnoreCase(["URN", "urine", "Urin"], sampleTypeCode) }: return "urine"
    case { matchIgnoreCase(["swab"], sampleTypeCode) }: return "swab"
    case {
      matchIgnoreCase(["Granulozyten", "PELLET-L", "BUC", "BMA", "liquid-other", "LEUK", "CEN", "MOTH", "LIQUID", "Flüssigprobe", "KMBLUT", "BFF",
                       "EDTA-ZB", "ZB", "Liquid_slides"], sampleTypeCode)
    }: return "liquid-other"
    case { matchIgnoreCase(["FFPE"], sampleTypeCode) }: return "tissue-ffpe"
    case {
      matchIgnoreCase(["Paraffin (FFPE)", "Paraffin", "FFPE", "NBF"], stockType) &&
          (matchIgnoreCase(["NRT", "NGW", "TIS", "TGW", "STUGEW", "NRT", "Tumorgewebe", "Normalgewebe", "RDT", "NNB", "PTM", "RZT", "LMT",
                            "MMT", "GEW", "TM", "BTM", "SMT", "TFL", "NBF", "tumor-tissue-ffpe", "normal-tissue-ffpe", "other-tissue-ffpe"], sampleTypeCode) ||
              matchIgnoreCase(["tissue", "gewebe", "gewebeprobe", "tissue sample"], sampleKindCode))
    }: return "tissue-ffpe"
    case { matchIgnoreCase(["Kryo"], sampleTypeCode) }: return "tissue-frozen"
    case {
      matchIgnoreCase(["Kryo/Frisch (FF)", "Kryo/Frisch", "FF", "SNP"], stockType) &&
          (matchIgnoreCase(["NGW", "TIS", "TGW", "STUGEW", "NRT", "Tumorgewebe", "Normalgewebe", "RDT", "NNB", "PTM", "RZT", "LMT", "MMT", "GEW",
                            "TM", "BTM", "SMT", "TFL", "SNP", "tumor-tissue-frozen", "normal-tissue-frozen", "other-tissue-frozen"], sampleTypeCode) ||
              matchIgnoreCase(["tissue", "gewebe", "gewebeprobe", "tissue sample"], sampleKindCode))
    }: return "tissue-frozen"
    case { matchIgnoreCase(["tissue-other", "NNB", "HE"], sampleTypeCode) }: return "tissue-other"
    case { matchIgnoreCase(["cDNA", "gDNA", "dna", "DNA", "CDNA ", "DNAAMP", "BLDCCFDNASTABIL", "g-dna", "cf-dna"], sampleTypeCode) }: return "dna"
    case { matchIgnoreCase(["RNA", "BLDRNASTABIL"], sampleTypeCode) }: return "rna"
    case { matchIgnoreCase(["derivative-other"], sampleTypeCode) }: return "derivative-other"
    default: return null // no match
  }
}

static boolean matchIgnoreCase(final List<String> stringList, final String stringToMatch) {
  return stringList.stream().anyMatch({ it.equalsIgnoreCase(stringToMatch) })
}
