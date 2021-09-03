package projects.izi.frankfurt

import de.kairos.centraxx.fhir.r4.utils.FhirUrls

/**
 * Represented by a CXX AbstractSample
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.2
 */
specimen {

  id = "Specimen/" + context.source["id"]

  final def idContainer = context.source["idContainer"]?.find {
    "SAMPLEID" == it["idContainerType"]?.getAt("code")
  }

  if (idContainer) {
    identifier {
      value = idContainer["psn"]
      type {
        coding {
          system = "urn:centraxx"
          code = idContainer["idContainerType"]?.getAt("code")
        }
      }
    }
  }

  status = context.source["restAmount.amount"] > 0 ? "available" : "unavailable"

  type {
    coding {
      system = "urn:centraxx"
      code = toNumType(context.source["sampleType.code"])
    }
    if (context.source["sampleType.sprecCode"]) {
      coding += context.translateBuiltinConcept("sprec3_bbmri_sampletype", context.source["sampleType.sprecCode"])
      coding {
        system = "https://doi.org/10.1089/bio.2017.0109"
        code = context.source["sampleType.sprecCode"]
      }
    } else {
      coding += context.translateBuiltinConcept("centraxx_bbmri_samplekind", context.source["sampleType.kind"] ?: "")
    }
  }

  final def patIdContainer = context.source["patientcontainer.idContainer"]?.find {
    "PaIdTMP" == it["idContainerType"]?.getAt("code")
  }

  if (patIdContainer) {
    subject {
      identifier {
        value = patIdContainer["psn"]
        type {
          coding {
            system = "urn:centraxx"
            code = patIdContainer["idContainerType"]?.getAt("code")
          }
        }
      }
    }
  }

  subject {
    reference = "Patient/" + context.source["patientcontainer.id"]
  }

  if (context.source["parent"] != null) {
    parent {
      reference = "Specimen/" + context.source["parent.id"]
    }
  }

  receivedTime {
    date = context.source["samplingDate.date"]
  }

  collection {
    collectedDateTime {
      date = context.source["samplingDate.date"]
      quantity {
        value = context.source["initialAmount.amount"] as Number
        unit = context.source["initialAmount.unit"]
        system = "urn:centraxx"
      }
    }
  }

  container {
    if (context.source["receptable"]) {
      identifier {
        value = toSampleReceptacleType(context.source["receptable.code"])
        system = "urn:centraxx"
      }

      capacity {
        value = context.source["receptable.size"]
        unit = context.source["restAmount.unit"]
        system = "urn:centraxx"
      }
    }

    specimenQuantity {
      value = context.source["restAmount.amount"] as Number
      unit = context.source["restAmount.unit"]
      system = "urn:centraxx"
    }
  }

  extension {
    url = FhirUrls.Extension.SAMPLE_CATEGORY
    valueCoding {
      system = "urn:centraxx"
      code = context.source["sampleCategory"]
    }
  }

  // SPREC Extensions
  extension {
    url = FhirUrls.Extension.SPREC
    extension {
      url = FhirUrls.Extension.Sprec.USE_SPREC
      valueBoolean = context.source["useSprec"]
    }
//    if (context.source["sprecCode"]) {
//      extension {
//        url = FhirUrls.Extension.Sprec.SPREC_CODE
//        valueCoding {
//          system = "https://doi.org/10.1089/bio.2017.0109"
//          code = context.source["sprecCode"]
//        }
//      }
//    }

    //
    // SPREC TISSUE
    //
    if ("TISSUE" == context.source["sampleKind"]) {
      if (context.source["sprecTissueCollectionType"]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_TISSUE_COLLECTION_TYPE
          valueCoding {
            system = "urn:centraxx"
            code = context.source["sprecTissueCollectionType.code"]
          }
        }
      }
      if (context.source["warmIschTime"]) {
        extension {
          url = FhirUrls.Extension.Sprec.WARM_ISCH_TIME
          valueCoding {
            system = "urn:centraxx"
            code = context.source["warmIschTime.code"]
          }
        }
      }
      if (context.source["warmIschTimeDate"]) {
        extension {
          url = FhirUrls.Extension.Sprec.WARM_ISCH_TIME_DATE
          valueDateTime = context.source["warmIschTimeDate.date"]
        }
      }
      if (context.source["coldIschTime"]) {
        extension {
          url = FhirUrls.Extension.Sprec.COLD_ISCH_TIME
          valueCoding {
            system = "urn:centraxx"
            code = context.source["coldIschTime.code"]
          }
        }
      }
      if (context.source["coldIschTimeDate"]) {
        extension {
          url = FhirUrls.Extension.Sprec.COLD_ISCH_TIME_DATE
          valueDateTime = context.source["coldIschTimeDate.date"]
        }
      }
      if (context.source["stockType"]) {
        extension {
          url = FhirUrls.Extension.Sprec.STOCK_TYPE
          valueCoding {
            system = "urn:centraxx"
            code = context.source["stockType.code"]
          }
        }
      }
      if (context.source["sprecFixationTime"]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_FIXATION_TIME
          valueCoding {
            system = "urn:centraxx"
            code = context.source["sprecFixationTime.code"]
          }
        }
      }
      if (context.source["sprecFixationTimeDate"]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_FIXATION_TIME_DATE
          valueDateTime = context.source["sprecFixationTimeDate.date"]
        }
      }
    }

    //
    // SPREC LIQUID
    //
    if ("LIQUID" == context.source["sampleKind"]) {
      if (context.source["sprecPrimarySampleContainer"]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_PRIMARY_SAMPLE_CONTAINER
          valueCoding {
            system = "urn:centraxx"
            code = toPrimaryContainerType(context.source["sprecPrimarySampleContainer.code"])
          }
        }
      }
      if (context.source["sprecPreCentrifugationDelay"]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_PRE_CENTRIFUGATION_DELAY
          valueCoding {
            system = "urn:centraxx"
            code = context.source["sprecPreCentrifugationDelay.code"]
          }
        }
      }
      if (context.source["sprecPreCentrifugationDelayDate"]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_PRE_CENTRIFUGATION_DELAY_DATE
          valueDateTime = context.source["sprecPreCentrifugationDelayDate.date"]
        }
      }
      if (context.source["sprecPostCentrifugationDelay"]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_POST_CENTRIFUGATION_DELAY
          valueCoding {
            system = "urn:centraxx"
            code = context.source["sprecPostCentrifugationDelay.code"]
          }
        }
      }
      if (context.source["sprecPostCentrifugationDelayDate"]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_POST_CENTRIFUGATION_DELAY_DATE
          valueDateTime = context.source["sprecPostCentrifugationDelayDate.date"]
        }
      }
      if (context.source["stockProcessing"]) {
        extension {
          url = FhirUrls.Extension.Sprec.STOCK_PROCESSING
          valueCoding {
            system = "urn:centraxx"
            code = context.source["stockProcessing.code"] as String
          }
        }
      }
      if (context.source["stockProcessingDate"]) {
        extension {
          url = FhirUrls.Extension.Sprec.STOCK_PROCESSING_DATE
          valueDateTime = context.source["stockProcessingDate.date"]
        }
      }
      if (context.source["secondProcessing"]) {
        extension {
          url = FhirUrls.Extension.Sprec.SECOND_PROCESSING
          valueCoding {
            system = "urn:centraxx"
            code = context.source["secondProcessing.code"] as String
          }
        }
      }
      if (context.source["secondProcessingDate"]) {
        extension {
          url = FhirUrls.Extension.Sprec.SECOND_PROCESSING_DATE
          valueDateTime = context.source["secondProcessingDate.date"]
        }
      }
    }
  }
}

static String toNumType(final Object sourceType) {
  switch (sourceType) {
    case "PBMC": return "ZZZ(pbm)"
    case "CIT_PL": return "CIT_PL"
    case "CPT_PL": return "CPT_PL"
    case "EDTA_PL": return "EDTA_PL"
    case "PAX": return "PAX"
    case "STL_STAB": return "STL_STAB"
    case "URIN": return "URN"
    case "EDTA": return "BLD"
    case "SER": return "SER"
    case "STL": return "STL"
    default: return sourceType
  }
}

static String toSampleReceptacleType(final Object sourceReceptacle) {
  switch (sourceReceptacle) {
    case "CPT_HEP_8": return "BDCPT080"
    case "RNA_TEMP_2_5": return "BDVac100"
    case "STU_CONV": return "StSTL101"
    case "EDTA_7_5": return "7_5_ML_BLUTROEHRCHEN"
    case "URIN_3_2": return "URIN_3_2"
    case "STU_CONV": return "STU_CONV" //TODO can never be reached because of duplicated case in line 325
    case "STU_STAB_CONV": return "STU_STAB_CONV"
    case "CIT_10": return "CIT_10"
    case "SER_GEL_4_7": return "SER_GEL_4_7"
    case "UTK_SAR_2_SER": return "UTK_SAR_2_SER"
    case "UTK_SAR_2_SER": return "UTK_SAR_2_SER" //TODO duplicate line 333. Is there a third UTK_SAR_2_ variant missing?
    case "UTK_SAR_2_PL": return "UTK_SAR_2_PL"
    case "CRYO_SAR_2": return "CRYO_SAR_2"
    case "SER_GEL_9": return "SER_GEL_9"
    case "LVL_300_SE": return "LVL_300_SE"
    case "LVL_1000_SE": return "LVL_1000_SE"
    case "LVL_1000_PL": return "LVL_1000_PL"
    default: return sourceReceptacle
  }
}

static String toPrimaryContainerType(final Object sourcePrimContainer) {
  switch (sourcePrimContainer) {
    case "RNA_TEMP_2_5": return "TEM"
    case "EDTA_7_5": return "EDTA_7_5"
    case "URIN_3_2": return "URIN_3_2"
    case "STU_CONV": return "STU_CONV"
    case "STU_STAB_CONV": return "STU_STAB_CONV"
    case "CIT_10": return "CIT_10"
    case "CPT_HEP_8": return "CPT_HEP_8"
    default: return sourcePrimContainer
  }
}
