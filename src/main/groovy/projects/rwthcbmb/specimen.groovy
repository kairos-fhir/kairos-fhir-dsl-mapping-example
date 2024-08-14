package projects.rwthcbmb

import de.kairos.fhir.centraxx.metamodel.IdContainer

import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.abstractSample
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample
/**
 * Represented by a CXX AbstractSample
 * Specified by https://simplifier.net/bbmri.de/specimen
 *
 * hints:
 * The CCP-IT JF on 2020-12-18 has decides/informed that only master samples should be exported for the BBMRI-Sample locator.
 * Because the DKTK uses also all Aliquots (yet), a separate Groovy mapping for the same profile is necessary for DKTK.
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
 */
specimen {

  if (!"MASTER".equals(context.source["sampleCategory"])) {
    return  // All not master are filtered.
  }

  final def idContainer = context.source[abstractSample().patientContainer().idContainer()]?.find {
    "MPI" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(CODE)
  }

  if (idContainer == null) {
    return // If sample belongs to a patient without MPI, sample is not exported.
  }

  final def idc = context.source[sample().idContainer()].find {
    "Lab-ID" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(CODE)
  }

  if (idc == null) {
    return // If sample has no Lab-ID, sample is not exported.
  }

  id = "Specimen/" + idc[IdContainer.ID_CONTAINER_TYPE]?.getAt(CODE)

  meta {
    profile "https://fhir.bbmri.de/StructureDefinition/Specimen"
  }

  if (idc) {
    identifier {
      value = idc[IdContainer.PSN]
      type {
        coding {
          system = "urn:centraxx"
          code = idc[IdContainer.ID_CONTAINER_TYPE]?.getAt(CODE)
        }
      }
      system = "https://dktk.dkfz.de/fhir/NamingSystem/exliquid-specimen"
    }
  }

  status = context.source[abstractSample().restAmount().amount()] > 0 ? "available" : "unavailable"

  type {
    // 0. First coding is the CXX sample type code. If mapping is missing, this code might help to identify the source value.
    coding {
      system = "urn:centraxx"
      code = context.source[abstractSample().sampleType().code()]
    }
    // 1. Without mapping, if CXX code and BBMRI code is the same.
    if (isBbmriSampleTypeCode(context.source[abstractSample().sampleType().code()] as String)) {
      coding {
        system = "https://fhir.bbmri.de/CodeSystem/SampleMaterialType"
        code = (context.source[abstractSample().sampleType().code()] as String).toLowerCase()
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
      final String bbmriCode = sampleKindToBbmriSampleType(context.source[abstractSample().sampleType().kind()] as String)
      if (bbmriCode != null) {
        coding {
          system = "https://fhir.bbmri.de/CodeSystem/SampleMaterialType"
          code = bbmriCode
        }
      }
    }
  }

  subject {
    reference = "Patient/" + idContainer[IdContainer.ID_CONTAINER_TYPE]?.getAt(CODE)
  }

  // disabled on 2023-09-13 because not needed yet
//  if (context.source[abstractSample().episode()]) {
//    encounter {
//      reference = "Encounter/" + context.source[abstractSample().episode().id()]
//    }
//  }

  receivedTime {
    date = normalizeDate(context.source[abstractSample().samplingDate().date()] as String)
  }

  final def ucum = context.conceptMaps.builtin("centraxx_ucum")
  collection {
    collectedDateTime {
      date = context.source[abstractSample().samplingDate().date()]
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

      capacity {
        value = context.source[abstractSample().receptable().size()]
        unit = ucum.translate(context.source[abstractSample().restAmount().unit()] as String)?.code
        system = "http://unitsofmeasure.org"
      }
    }

    specimenQuantity {
      value = context.source[abstractSample().restAmount().amount()] as Number
      unit = ucum.translate(context.source[abstractSample().restAmount().unit()] as String)?.code
      system = "http://unitsofmeasure.org"
    }
  }

  if (context.source[abstractSample().organisationUnit()]) {
    extension {
      url = "https://fhir.bbmri.de/StructureDefinition/Custodian"
      valueReference {
        reference = "Organization/" + context.source[abstractSample().organisationUnit().code()]
      }
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
      case ['C', 'F', 'O', 'Q']:
        return "temperatureLN"
      case ['A', 'D', 'J', 'L', 'N', 'O', 'S']:
        return "temperature-60to-85"
      case ['B', 'H', 'K', 'M', 'T']:
        return "temperature-18to-35"
      default:
        return "temperatureOther"
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
  return null
}

static String sampleKindToBbmriSampleType(final String sampleKind) {
  if (sampleKind == null) {
    return null
  } else if (sampleKind == "TISSUE") {
    return "tissue-other"
  } else if (sampleKind == "LIQUID") {
    return "liquid-other"
  }
  return "derivative-other" // eg CXX cells
}

static boolean isBbmriSampleTypeCode(final String sampleTypeCode) {

  if (sampleTypeCode == "ASZ") return "ascites"
  else if (sampleTypeCode == "ASZ") return "ascites"
  else if (sampleTypeCode == "ASZP") return "ascites"
  else if (sampleTypeCode == "ASZÜ") return "ascites"
  else if (sampleTypeCode == "BAL") return "liquid-other"
  else if (sampleTypeCode == "BUFC") return "buffy-coat"
  else if (sampleTypeCode == "DNA") return "dna"
  else if (sampleTypeCode == "BMA") return "bone-marrow"
  else if (sampleTypeCode == "CSFP") return "csf-liquor"
  else if (sampleTypeCode == "CSF") return "csf-liquor"
  else if (sampleTypeCode == "PANKJUICE") return "liquid-other"
  else if (sampleTypeCode == "FFPE") return "tissue-ffpe"
  else if (sampleTypeCode == "PELE") return "liquid-other"
  else if (sampleTypeCode == "PELH") return "liquid-other"
  else if (sampleTypeCode == "PELK") return "liquid-other"
  else if (sampleTypeCode == "PELLH") return "liquid-other"
  else if (sampleTypeCode == "CPL") return "plasma-citrat"
  else if (sampleTypeCode == "PLH") return "plasma-heparin"
  else if (sampleTypeCode == "PL1") return "plasma-edta"
  else if (sampleTypeCode == "PFL") return "liquid-other"
  else if (sampleTypeCode == "SWAB") return "swab"
  else if (sampleTypeCode == "RNA") return "rna"
  else if (sampleTypeCode == "SAL") return "saliva"
  else if (sampleTypeCode == "SLIDE") return "tissue-other"
  else if (sampleTypeCode == "SER") return "blood-serum"
  else if (sampleTypeCode == "TIS") return "tissue-frozen"
  else if (sampleTypeCode == "STL") return "stool-faeces"
  else if (sampleTypeCode == "SF") return "liquid-other"
  else if (sampleTypeCode == "URN") return "urine"
  else if (sampleTypeCode == "URNP") return "urine"
  else if (sampleTypeCode == "URNÜ") return "urine"
  else if (sampleTypeCode == "VSZE") return "peripheral-blood-cells-vital"
  else if (sampleTypeCode == "VSZH") return "peripheral-blood-cells-vital"
  else if (sampleTypeCode == "VSZK") return "peripheral-blood-cells-vital"
  else if (sampleTypeCode == "VSZLIH") return "peripheral-blood-cells-vital"
  else if (sampleTypeCode == "EDTA") return "whole-blood"
  else if (sampleTypeCode == "WSF") return "swab"
  else if (sampleTypeCode == "ZP") return "liquid-other"
  else if (sampleTypeCode == "ZERV") return "swab"
  else return null
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
}