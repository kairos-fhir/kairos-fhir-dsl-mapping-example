package customexport.dktk.v2


import static de.kairos.fhir.centraxx.metamodel.RootEntities.abstractSample
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * This script calculates the availability of sample based on children and will only export the status
 *
 *  * Hints:
 *  * CCP-IT 2025-11-06: Please note the correct path!
 *
 * @author Patrick Skowronek
 * @since HDRP.v.3.17.1.6, v.3.17.2
 *
 */
specimen {
  final String sampleKind = context.source[abstractSample().sampleType().kind()] as String
  final String stockType = context.source[abstractSample().stockType().code()] as String


  final String sampleTypeCode = context.source[abstractSample().sampleType().code()] as String
  if (matchIgnoreCase(["TBL", "LES", "UBK", "ZZZ", "NRT"], sampleTypeCode)) {
    return //"Leerschnitt", "Unbekannt" are filtered
  }

  // Logic for typing
  final String bbmriCode0 = codeToSampleType(sampleTypeCode, stockType, sampleKind)
  String bbmriType

  if (bbmriCode0 != null) {
    bbmriType = bbmriCode0
  } else if (isBbmriSampleTypeCode(sampleTypeCode)) {
    bbmriType = sampleTypeCode.toLowerCase()
  } else if (sampleKindToBbmriSampleType(sampleKind) != null) {
    // 3. CXX sample kind => BBMRI SampleMaterialType.
    bbmriType = sampleKindToBbmriSampleType(sampleKind)
  } else if (context.source[abstractSample().sampleType().sprecCode()] != null) {
    def plasmaSamples = [
        "plasma-edta",
        "plasma-citrat",
        "plasma-heparin",
        "plasma-cell-free",
        "plasma-other"
    ]

    def DnaSamples = [
        "cf-dna",
        "g-dna"
    ]

    def tissueSamples = [
        "tumor-tissue-ffpe",
        "normal-tissue-ffpe",
        "other-tissue-ffpe"
    ]

    bbmriType = sprecToBbmriSampleType(context.source[abstractSample().sampleType().sprecCode()] as String).collect { sample ->
      if (plasmaSamples.contains(sample)) {
        "blood-plasma"
      } else if (tissueSamples.contains(sample)) {
        "tissue-ffpe"
      } else if (DnaSamples.contains(sample)) {
        "dna"
      } else {
        sample
      }
    }
  } else {
    //A sample without typ is not possible
    return
  }

  // Filter all Samples that are not derived liquid samples which are aliquoted
  if (
      ("DERIVED" == context.source["sampleCategory"] && "LIQUID" == sampleKind &&
          ("blood-plasma" == bbmriType ||
              "buffy-coat" == bbmriType ||
              "peripheral-blood-cells-vital" == bbmriType ||
              "blood-serum" == bbmriType ||
              "saliva" == bbmriType ||
              "urine" == bbmriType ||
              "dna" == bbmriType ||
              "rna" == bbmriType
          )
      )

  ) {
    if (context.source[abstractSample().restAmount().amount()] > 0) {

      try {
        final File cacheFile = new File("C:/centraxx-home/groovy-cache/" + context.source[sample().parent().id()])
        if (!cacheFile.exists()) {
          cacheFile.createNewFile()
          cacheFile.write("sample available")
        }
      } catch (IOException e) {
        println "An error occurred while writing to the file: " + e.getMessage()
      }
    }
  }
  return
}

static String codeToSampleType(final String sampleTypeCode, final String stockType, final String sampleKindCode) {
  if (null == sampleTypeCode) {
    return null
  }

  switch (sampleTypeCode) {
    case { matchIgnoreCase(["whole-blood", "BLD", "VBL", "Vollblut", "TBL", "EDTA-PB", "Heparinblut", "BLD_CITRATE", "BLD_EDTA", "BLD_LIHE"], sampleTypeCode) }: return "whole-blood"
    case { matchIgnoreCase(["KNM", "bone-marrow", "Knochenmark", "BMA", "EDTAKM", "KM", "Knochenmark", "BMA_EDTA", "BMA_LIHE"], sampleTypeCode) }: return "bone-marrow"
    case { matchIgnoreCase(["BUFFYCOAT", "BuffyCoat", "EDTA-BC", "BUF", "buffy-coat", "BUFFYCOATNOTVIABLE", "BFF", "BFF_BM"], sampleTypeCode) }: return "buffy-coat"
    case { matchIgnoreCase(["TBK", "BF"], sampleTypeCode) }: return "dried-whole-blood"
    case { matchIgnoreCase(["PBMC", "PBMC-nl", "PBMCs", "PBMC-l", "PEL", "CEL", "CEL_BM"], sampleTypeCode) }: return "peripheral-blood-cells-vital"
    case {
      matchIgnoreCase(["PLA", "blood-plasma", "PL1", "Plasma", "CIT-PLASMA", "P", "HEPAP", "P-cf", "EDTAFCPMA", "EDTA-APRPMA", "EP, CP", "EPPL2", "EPPL1",
                       "plasma-edta", "plasma-citrat", "plasma-heparin", "plasma-cell-free", "plasma-other", "HEPAPPBS",
                       "PL1_BM_EDTA", "PL1_BM_LIHE", "PL2"], sampleTypeCode)
    }: return "blood-plasma"
    case { matchIgnoreCase(["SER", "blood-serum", "Serum"], sampleTypeCode) }: return "blood-serum"
    case { matchIgnoreCase(["ASC"], sampleTypeCode) }: return "ascites"
    case { matchIgnoreCase(["LQR", "CSF", "csf-liquor", "LIQ-ÜS", "LIQ-CSF", "Liquor"], sampleTypeCode) }: return "csf-liquor"
    case { matchIgnoreCase(["SPEICHEL", "SAL", "SALIVA-SED", "SALIVA-UES"], sampleTypeCode) }: return "saliva"
    case { matchIgnoreCase(["STL", "STLctr"], sampleTypeCode) }: return "stool-faeces"
    case { matchIgnoreCase(["URN", "urine", "Urin", "URIN-ÜS", "URIN-SED", "URN_SPONTANEOUS-URINE"], sampleTypeCode) }: return "urine"
    case { matchIgnoreCase(["swab", "ZZZ_OROPHARYNGEAL_SWAB", "ZZZ_NASOPHARYNGEAL_SWAB"], sampleTypeCode) }: return "swab"
    case {
      matchIgnoreCase(["Granulozyten", "PELLET-L", "BUC", "BAL-PEL", "BAL-ÜS", "THRO", "BMA", "liquid-other", "LEUK", "CEN", "MOTH", "LIQUID", "Flüssigprobe", "KMBLUT",
                       "EDTA-ZB", "ZB", "Liquid_slides"], sampleTypeCode)
    }: return "liquid-other"
    case { matchIgnoreCase(["FFPE"], sampleTypeCode) }: return "tissue-ffpe"
    case {
      matchIgnoreCase(["Paraffin (FFPE)", "Paraffin", "FFPE", "NBF"], stockType) &&
          (matchIgnoreCase(["NRT", "NGW", "TIS", "TGW", "STUGEW", "NRT", "Tumorgewebe", "Normalgewebe", "RDT", "NNB", "PTM", "RZT", "LMT",
                            "MMT", "GEW", "TM", "BTM", "SMT", "TFL", "NBF", "tumor-tissue-ffpe", "normal-tissue-ffpe", "other-tissue-ffpe",
                            "TIS_NORMAL", "TIS_TUMOR"], sampleTypeCode) ||
              matchIgnoreCase(["tissue", "gewebe", "gewebeprobe", "tissue sample"], sampleKindCode))
    }: return "tissue-ffpe"
    case { matchIgnoreCase(["Kryo"], sampleTypeCode) }: return "tissue-frozen"
    case {
      matchIgnoreCase(["Kryo/Frisch (FF)", "Kryo/Frisch", "FF", "SNP"], stockType) &&
          (matchIgnoreCase(["NGW", "TIS", "TGW", "STUGEW", "NRT", "Tumorgewebe", "Normalgewebe", "RDT", "NNB", "PTM", "RZT", "LMT", "MMT", "GEW",
                            "TM", "BTM", "SMT", "TFL", "SNP", "tumor-tissue-frozen", "normal-tissue-frozen", "other-tissue-frozen",
                            "TIS_NORMAL", "TIS_TUMOR"], sampleTypeCode) ||
              matchIgnoreCase(["tissue", "gewebe", "gewebeprobe", "tissue sample"], sampleKindCode))
    }: return "tissue-frozen"
    case { matchIgnoreCase(["tissue-other", "NNB", "HE", "TIS_NORMAL", "TIS_TUMOR"], sampleTypeCode) }: return "tissue-other"
    case { matchIgnoreCase(["cDNA", "gDNA", "dna", "DNA", "CDNA ", "DNAAMP", "BLDCCFDNASTABIL", "g-dna", "cf-dna", "BLD_CIRCULATING_CELL-FREE_DNA"], sampleTypeCode) }: return "dna"
    case { matchIgnoreCase(["RNA", "BLDRNASTABIL", "BLD_CIRCULATING_CELL-FREE_RNA"], sampleTypeCode) }: return "rna"
    case { matchIgnoreCase(["derivative-other"], sampleTypeCode) }: return "derivative-other"
    default: return null // no match
  }
}

static boolean matchIgnoreCase(final List<String> stringList, final String stringToMatch) {
  return stringList.stream().anyMatch({ it.equalsIgnoreCase(stringToMatch) })
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
