package customexport.dktk.v2

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType

import static de.kairos.fhir.centraxx.metamodel.RootEntities.abstractSample
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a HDRP AbstractSample
 *
 * Specified by https://simplifier.net/oncology/oncospecimen
 *
 * @author Mike Wähnert
 * @since HDRP.v.3.17.1.6, v.3.17.2
 *
 * Hints:
 *  * CCP-IT 2025-11-06: Export liquid mothersamples and master tissue samples
 *                       The available status is depended on the specimenstatus script
 *                       Please note the correct path!
 *                       To export the local pseudonym, replace the placeholder with the internal code
 */
specimen {
    final String sampleKind = context.source[abstractSample().sampleType().kind()] as String
    final String stockType = context.source[abstractSample().stockType().code()] as String


    final String sampleTypeCode = context.source[abstractSample().sampleType().code()] as String
    if (matchIgnoreCase(["TBL", "LES", "UBK", "ZZZ", "NRT"], sampleTypeCode)) {
        return //"Leerschnitt", "Unbekannt" are filtered
    }

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
            !("ALIQUOTGROUP" == context.source["sampleCategory"] && "LIQUID" == sampleKind &&
                    ("blood-plasma" == bbmriType ||
                            "buffy-coat" == bbmriType ||
                            "peripheral-blood-cells-vital" == bbmriType ||
                            "blood-serum" == bbmriType ||
                            "saliva" == bbmriType ||
                            "urine" == bbmriType ||
                            "dna" == bbmriType ||
                            "rna" == bbmriType
                    )
            ) &&
                    // Filter all Samples that are not master liquid samples which are not aliquoted
                    !("MASTER" == context.source["sampleCategory"] && "LIQUID" == sampleKind &&
                            ("dried-whole-blood" == bbmriType ||
                                    "bone-marrow" == bbmriType ||
                                    "ascites" == bbmriType ||
                                    "csf-liquor" == bbmriType ||
                                    "stool-faeces" == bbmriType ||
                                    "swab" == bbmriType ||
                                    "liquid-other" == bbmriType
                            )
                    ) &&
                    // Filter all Samples that are not master tissue samples
                    !("MASTER" == context.source["sampleCategory"] && "TISSUE" == sampleKind)
    ) {
        return
    }


    id = "Specimen/" + context.source[abstractSample().id()]

    def idc = ""

    if ("ALIQUOTGROUP" == context.source["sampleCategory"] && "LIQUID" == sampleKind &&
            ("blood-plasma" == bbmriType ||
                    "buffy-coat" == bbmriType ||
                    "peripheral-blood-cells-vital" == bbmriType ||
                    "blood-serum" == bbmriType ||
                    "saliva" == bbmriType ||
                    "urine" == bbmriType ||
                    "dna" == bbmriType ||
                    "rna" == bbmriType
            )
    ) {
        idc = context.source[sample().parent().idContainer()].find {
            "EXLIQUID" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
    } else {
        idc = context.source[sample().idContainer()].find {
            "EXLIQUID" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
    }

    meta {
        profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Specimen-OncoSpecimen"
    }


    identifier {
        // This sets the pseudonym for the sample. Please replace the "REPLACE" with your ID CentraXX internal name
        value = context.source[sample().idContainer()].find {
            "REPLACE" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
        if (idc) {
            system = "http://dktk.dkfz.de/fhir/sid/exliquid-specimen"
        }
    }

    if ("TISSUE".equals(sampleKind)) {
        status = context.source[abstractSample().restAmount().amount()] > 0 ? "available" : "unavailable"
    } else {
        final File cacheFile = new File("C:/centraxx-home/groovy-cache/" + context.source[abstractSample().id()])
        if (cacheFile.exists()) {
            status = "available"
        } else {
            status = "unavailable"
        }
    }

    type {
        // 0. Site specific CXX sample type code => BBMRI SampleMaterialType.
        if (bbmriType != null) {
            coding {
                system = "https://fhir.bbmri.de/CodeSystem/SampleMaterialType"
                code = bbmriType
            }
        }
    }

    subject {
        reference = "Patient/" + context.source[abstractSample().patientContainer().id()]
    }

    final org_units = ["TODO for Site", "EXLIQUID"]

    if (org_units.contains(context.source[abstractSample().organisationUnit()])) {
        extension {
            url = "https://fhir.bbmri.de/StructureDefinition/Custodian"
            valueString = "BBMRI_DIR_ID"
        }
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
    return dateTimeString != null ? dateTimeString.substring(0, 10) : null
}

static String codeToSampleType(final String sampleTypeCode, final String stockType, final String sampleKindCode) {
    if (null == sampleTypeCode) {
        return null
    }

    switch (sampleTypeCode) {
        case { matchIgnoreCase(["whole-blood", "BLD", "VBL", "Vollblut", "TBL", "EDTA-PB", "Heparinblut", "BLD_CITRATE", "BLD_EDTA", "BLD_LIHE", "EDTA_NH4_Heparin_peripheres Blut", "EDTA_VOLLBLUT", "Heparin-Vollblut"], sampleTypeCode) }: return "whole-blood"
        case { matchIgnoreCase(["KNM", "bone-marrow", "Knochenmark", "BMA", "EDTAKM", "KM", "Knochenmark", "BMA_EDTA", "BMA_LIHE", "EDTA_Knochenmark_Blut", "EDTA_KNOCHENMARK_ZELLEN", "EDTA_NH4_Heparin_Knochenmark_Blut", "HYB", "KNO", "ZELLEN_AUS_KNOCHENMARKSTANZE", "KM"], sampleTypeCode) }: return "bone-marrow"
        case { matchIgnoreCase(["BUFFYCOAT", "BuffyCoat", "EDTA-BC", "BUF", "buffy-coat", "BUFFYCOATNOTVIABLE", "BFF", "BFF_BM", "BC"], sampleTypeCode) }: return "buffy-coat"
        case { matchIgnoreCase(["TBK", "BF"], sampleTypeCode) }: return "dried-whole-blood"
        case { matchIgnoreCase(["PBMC", "PBMC-nl", "PBMCs", "PBMC-l", "PEL", "CEL", "CEL_BM", "EDTA_PERIPHERES_BLUT_ZELLEN", "PB", "PMN", "PMNC"], sampleTypeCode) }: return "peripheral-blood-cells-vital"
        case {
            matchIgnoreCase(["PLA", "blood-plasma", "PL1", "Plasma", "CIT-PLASMA", "P", "HEPAP", "P-cf", "EDTAFCPMA", "EDTA-APRPMA", "EP, CP", "EP", "CP", "EDTA_PLASMA", "EPPL2", "EPPL1", "LH", "NA_HEPARIN_PLASMA", "AMMONIUM_HEPARIN", "CIT", "EDTA", "HEP",
                             "plasma-edta", "plasma-citrat", "plasma-heparin", "plasma-cell-free", "plasma-other", "HEPAPPBS", "EDTA_PLASMA_PERIPHERES_BLUT", "Heparin-Plasma", "PL1_BM_EDTA", "PL1_BM_LIHE", "PL2"], sampleTypeCode)
        }: return "blood-plasma"
        case { matchIgnoreCase(["SER", "blood-serum", "Serum", "K3_EDTA"], sampleTypeCode) }: return "blood-serum"
        case { matchIgnoreCase(["ASC", "ASZ"], sampleTypeCode) }: return "ascites"
        case { matchIgnoreCase(["LQR", "CSF", "csf-liquor", "LIQ-ÜS", "LIQ-CSF", "Liquor", "LI"], sampleTypeCode) }: return "csf-liquor"
        case { matchIgnoreCase(["SPEICHEL", "SAL", "SALIVA-SED", "SALIVA-UES", "SPEICHEL"], sampleTypeCode) }: return "saliva"
        case { matchIgnoreCase(["STL", "STLctr", "STUHL"], sampleTypeCode) }: return "stool-faeces"
        case { matchIgnoreCase(["URN", "urine", "Urin", "URIN-ÜS", "URIN-SED", "URIN_SEDIMENT", "URN_SPONTANEOUS-URINE"], sampleTypeCode) }: return "urine"
        case { matchIgnoreCase(["swab", "ZZZ_OROPHARYNGEAL_SWAB", "ZZZ_NASOPHARYNGEAL_SWAB", "REKTALABSTRICH", "Abstrich", "9"], sampleTypeCode) }: return "swab"
        case {
            matchIgnoreCase(["Granulozyten", "PELLET-L", "BUC", "BAL-PEL", "BAL-ÜS", "THRO", "BMA", "liquid-other", "LEUK", "BALÜ", "BALZ", "BZE", "DEN", "FIB", "MAK", "MON", "MUTTERMILCH", "NKZ", "TZE", "TZL", "ZEL", "ZELLKULTUR_PRIMÄRKULTUR", "CEN",
                             "MOTH", "LIQUID", "Flüssigprobe", "KMBLUT", "ZELLKULTUR_PRIMÄRKULTUR", "ZELLKULTUR_STABILE_ZELLINIE", "Zelllinie etabliert", "LEUKA", "PLE", "PLEURA", "PBL", "Angiolipom", "Atypisches_Fibroaxanthom", "B-Zellen", "CD4_T-Zellen",
                             "CD8_T-Zellen", "Fibroblasten_Kultur", "MCC_Zelllinie", "Melanom_Zelllinie", "SCC", "TIL", "T-Zellen", "ZEP",
                             "EDTA-ZB", "ZB", "Liquid_slides"], sampleTypeCode)
        }: return "liquid-other"
        case { matchIgnoreCase(["FFPE", "PG", "PS", "PS_HE", "PS_IHC"], sampleTypeCode) }: return "tissue-ffpe"
        case {
            matchIgnoreCase(["Paraffin (FFPE)", "Paraffin", "FFPE", "NBF"], stockType) &&
                    (matchIgnoreCase(["NRT", "NGW", "TIS", "TGW", "STUGEW", "NRT", "Tumorgewebe", "Normalgewebe", "RDT", "NNB", "PTM", "RZT", "LMT",
                                      "MMT", "GEW", "TM", "BTM", "SMT", "TFL", "NBF", "tumor-tissue-ffpe", "normal-tissue-ffpe", "other-tissue-ffpe",
                                      "TIS_NORMAL", "TIS_TUMOR", "PG", "PS", "PS_HE", "PS_IHC"], sampleTypeCode) ||
                            matchIgnoreCase(["tissue", "gewebe", "gewebeprobe", "tissue sample"], sampleKindCode))
        }: return "tissue-ffpe"
        case { matchIgnoreCase(["Kryo", "KS", "Gewebe_EM", "KS_HE"], sampleTypeCode) }: return "tissue-frozen"
        case {
            matchIgnoreCase(["Kryo/Frisch (FF)", "Kryo/Frisch", "FF", "SNP"], stockType) &&
                    (matchIgnoreCase(["NGW", "TIS", "TGW", "STUGEW", "NRT", "Tumorgewebe", "Normalgewebe", "RDT", "NNB", "PTM", "RZT", "LMT", "MMT", "GEW",
                                      "TM", "BTM", "SMT", "TFL", "SNP", "tumor-tissue-frozen", "normal-tissue-frozen", "other-tissue-frozen",
                                      "TIS_NORMAL", "TIS_TUMOR"], sampleTypeCode) ||
                            matchIgnoreCase(["tissue", "gewebe", "gewebeprobe", "tissue sample"], sampleKindCode))
        }: return "tissue-frozen"
        case { matchIgnoreCase(["tissue-other", "FG", "2", "7", "Knie", "LIP", "THR", "NTG", "ST", "TF", "TGW", "NNB", "HE", "TIS_NORMAL", "TIS_TUMOR"], sampleTypeCode) }: return "tissue-other"
        case { matchIgnoreCase(["cDNA", "gDNA", "dna", "DNA", "DNS", "PS_DNA", "CDNA ", "DNAAMP", "BLDCCFDNASTABIL", "g-dna", "cf-dna", "BLD_CIRCULATING_CELL-FREE_DNA"], sampleTypeCode) }: return "dna"
        case { matchIgnoreCase(["RNA", "BLDRNASTABIL", "BLD_CIRCULATING_CELL-FREE_RNA", "RNS", "PS_RNS"], sampleTypeCode) }: return "rna"
        case { matchIgnoreCase(["derivative-other", "AE"], sampleTypeCode) }: return "derivative-other"
        default: return null // no match
    }
}

static boolean matchIgnoreCase(final List<String> stringList, final String stringToMatch) {
    return stringList.stream().anyMatch({ it.equalsIgnoreCase(stringToMatch) })
}