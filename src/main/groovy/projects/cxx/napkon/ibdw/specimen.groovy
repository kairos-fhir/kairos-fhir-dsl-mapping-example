package projects.cxx.napkon.ibdw

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.SampleIdContainer
import de.kairos.fhir.centraxx.metamodel.enums.SampleCategory
import de.kairos.fhir.centraxx.metamodel.enums.SampleKind

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit

import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX AbstractSample
 * @author Jonas Küttner, Mike Wähnert
 * @since v.1.13.0, CXX.v.3.18.1.13
 *
 * The mapping transforms specimen from the ibdw Wuerzburg system to the NUM Greifswald system.
 * Intended to be used with POST (createOrUpdateByNaturalIdentifier) methods, because master samples already exists in the target system with a different logical fhir id.
 *
 * Hints:
 * 1. Filter: only master samples, derived samples and aliquot groups are allowed
 * 2. Filter: Only samples of the OrgUnits napPOP and napSUP
 * 3. Filter: Only master samples and their aliquots for which a NAPKON-ID mapping exists
 * 4. Cross Mapping IDs: NAPKONSMP (ibdw) to SAMPLEID (NUM) and SAMPLEID (ibdw) to EXTSAMPLEID (NUM)
 * 5. Mapping OrgUnit: napPOP (ibdw) to "NUM_Wuerzburg_POP" (NUM) and napSUP (ibdw) to "NUM_Wuerzburg_SUEP" (NUM)
 * 6. Mapping: ibdw sampleType.code, receptable.code to NUM sampleType
 */
specimen {

  // 1. Filter sample category
  final SampleCategory category = context.source[sample().sampleCategory()] as SampleCategory
  final boolean containsCategory = [SampleCategory.DERIVED, SampleCategory.MASTER, SampleCategory.ALIQUOTGROUP].contains(category)

  if (!containsCategory) {
    return
  }

  // 2. Filter OrgUnit
  String orgUnit = ""
  if ("napPOP" == context.source[sample().organisationUnit().code()] || "napPOP" == context.source[sample().parent().organisationUnit().code()]) {
    id = "Specimen/" + context.source[sample().id()]
    orgUnit = "NUM_W_POP"
  }
  else if ("napSUP" == context.source[sample().organisationUnit().code()] || "napSUP" == context.source[sample().parent().organisationUnit().code()]) {
    id = "Specimen/" + context.source[sample().id()]
    orgUnit = "NUM_W_SUEP"
  }
  else {
    return
  }

  // 3. Filter NAPKON-ID Mapping exists
  String napkonMappingExists = ""
  if (category == SampleCategory.MASTER) {
    napkonMappingExists = context.source[sample().idContainer()]?.find { final def entry ->
      "NAPKONSMP" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE) }
  }
  else if (category == SampleCategory.ALIQUOTGROUP) {
    napkonMappingExists = context.source[sample().parent().idContainer()]?.find { final def entry ->
      "NAPKONSMP" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE) }
  }
  else if (category == SampleCategory.DERIVED) {
    napkonMappingExists = context.source[sample().parent().parent().idContainer()]?.find { final def entry ->
      "NAPKONSMP" == entry [ SampleIdContainer.ID_CONTAINER_TYPE ]?.getAt (IdContainerType.CODE) }
  }
  if (!napkonMappingExists) {
    return
  }


  final def idContainerCodeMap = ["SAMPLEID": "EXTSAMPLEID", "NAPKONSMP": "SAMPLEID"]
  final Map<String, Object> idContainersMap = idContainerCodeMap.collectEntries { String idContainerCode, String _ ->
    return [
            (idContainerCode): context.source[sample().idContainer()]?.find { final def entry ->
              idContainerCode == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
            }
    ]
  } as Map<String, Object>

  // 4: cross-mapping of the ids of MASTER samples. The sample id of the HUB is provided as external sampled id to NUM.
  // The external sample id of HUB is provided as sample id to NUM.
  if (context.source[sample().sampleCategory()] as SampleCategory == SampleCategory.MASTER) {
    idContainersMap.each { final String idContainerCode, final Object idContainer ->
      if (idContainer) {
        identifier {
          type {
            coding {
              system = "urn:centraxx"
              code = idContainerCodeMap[idContainerCode]
            }
          }
          value = idContainer[SampleIdContainer.PSN]
        }
      }
    }
  } else { // Providing HUB sample id as sample id to NUM.
    if (idContainersMap["SAMPLEID"]) {
      identifier {
        type {
          coding {
            system = "urn:centraxx"
            code = "SAMPLEID"
          }
        }
        value = idContainersMap["SAMPLEID"][SampleIdContainer.PSN]
      }
    }
  }


  if (context.source[sample().repositionDate()]) {
    extension {
      url = FhirUrls.Extension.Sample.REPOSITION_DATE
      valueDateTime = context.source[sample().repositionDate().date()]
    }
  }

  // Standard location path
  extension {
    url = FhirUrls.Extension.Sample.SAMPLE_LOCATION
    extension {
      url = FhirUrls.Extension.Sample.SAMPLE_LOCATION_PATH
      valueString = toNumStorage(
              context.source[sample().sampleType().code()] as String
      )
    }
  }

  //5: Mapped organization unit attached to sample
  extension {
    url = FhirUrls.Extension.Sample.ORGANIZATION_UNIT
    valueReference {
      // by identifier
      identifier {
        value = orgUnit
      }
    }
  }

  if (context.source[sample().derivalDate()]) {
    extension {
      url = FhirUrls.Extension.Sample.DERIVAL_DATE
      valueDateTime = context.source[sample().derivalDate().date()]
    }
  }

  status = context.source[sample().restAmount().amount()] > 0 ? "available" : "unavailable"

  // 6: sample type mapping
  type {
    coding {
      system = "urn:centraxx"
      code = toNumType(
              context.source[sample().sampleType().code()] as String,
              context.source[sample().receptable().code()] as String
      )
    }
  }

  final def patIdContainer = context.source[sample().patientContainer().idContainer()]?.find {
    "NAPKON" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  if (patIdContainer) {
    subject {
      identifier {
        value = patIdContainer[IdContainer.PSN]
        type {
          coding {
            system = "urn:centraxx"
            code = patIdContainer[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
          }
        }
      }
    }
  }

  if (context.source[sample().parent()] != null) {
    parent {
      // Reference by identifier SampleId, because parent MasterSample might already exists in the target system
      final def extSampleIdParent = context.source[sample().parent().idContainer()]?.find { final def entry ->
        "NAPKONSMP" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
      }
      if (SampleCategory.MASTER == context.source[sample().parent().sampleCategory()] as SampleCategory && extSampleIdParent) {
        identifier {
          type {
            coding {
              code = "SAMPLEID"
            }
          }
          value = extSampleIdParent[SampleIdContainer.PSN]
        }
      } else {
        reference = "Specimen/" + context.source[sample().parent().id()]
      }
    }
  }

  receivedTime {
    date = context.source[sample().samplingDate().date()]
  }

  collection {
    collectedDateTime {
      date = context.source[sample().samplingDate().date()]
      quantity {
        value = context.source[sample().initialAmount().amount()] as Number
        unit = context.source[sample().initialAmount().unit()]
        system = "urn:centraxx"
      }
    }
  }

  container {
    if (context.source[sample().receptable()]) {
      identifier {
        value = toNumContainer(context.source[sample().sampleType().code()] as String, context.source[sample().receptable().code()] as String)
        system = "urn:centraxx"
      }

      capacity {
        value = context.source[sample().receptable().size()]
        unit = context.source[sample().restAmount().unit()]
        system = "urn:centraxx"
      }
    }

    specimenQuantity {
      value = context.source[sample().restAmount().amount()] as Number
      unit = context.source[sample().restAmount().unit()]
      system = "urn:centraxx"
    }
  }

  extension {
    url = FhirUrls.Extension.SAMPLE_CATEGORY
    valueCoding {
      system = "urn:centraxx"
      code = context.source[sample().sampleCategory()]
    }
  }

  if (context.source[sample().concentration()]) {
    extension {
      url = FhirUrls.Extension.Sample.CONCENTRATION
      valueQuantity {
        value = context.source[sample().concentration().amount()] as Number
        unit = context.source[sample().concentration().unit()]
      }
    }
  }

// SPREC Extensions
  extension {
    url = FhirUrls.Extension.SPREC
    extension {
      url = FhirUrls.Extension.Sprec.USE_SPREC
      valueBoolean = context.source[sample().useSprec()]
    }

    //
    // SPREC TISSUE
    //
    if (SampleKind.TISSUE == context.source[sample().sampleKind()] as SampleKind) {
      if (context.source[sample().sprecTissueCollectionType()]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_TISSUE_COLLECTION_TYPE
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().sprecTissueCollectionType().code()]
          }
        }
      }
      if (context.source[sample().warmIschTime()]) {
        extension {
          url = FhirUrls.Extension.Sprec.WARM_ISCH_TIME
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().warmIschTime().code()]
          }
        }
      }
      if (context.source[sample().warmIschTimeDate()]) {
        extension {
          url = FhirUrls.Extension.Sprec.WARM_ISCH_TIME_DATE
          valueDateTime = context.source[sample().warmIschTimeDate().date()]
        }
      }
      if (context.source[sample().coldIschTime()]) {
        extension {
          url = FhirUrls.Extension.Sprec.COLD_ISCH_TIME
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().coldIschTime().code()]
          }
        }
      }
      if (context.source[sample().coldIschTimeDate()]) {
        extension {
          url = FhirUrls.Extension.Sprec.COLD_ISCH_TIME_DATE
          valueDateTime = context.source[sample().coldIschTimeDate().date()]
        }
      }
      if (context.source[sample().stockType()]) {
        extension {
          url = FhirUrls.Extension.Sprec.STOCK_TYPE
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().stockType().code()]
          }
        }
      }
      if (context.source[sample().sprecFixationTime()]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_FIXATION_TIME
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().sprecFixationTime().code()]
          }
        }
      }
      if (context.source[sample().sprecFixationTimeDate()]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_FIXATION_TIME_DATE
          valueDateTime = context.source[sample().sprecFixationTimeDate().date()]
        }
      }
    }

    //
    // SPREC LIQUID
    //
    if (SampleKind.LIQUID == context.source[sample().sampleKind()] as SampleKind) {
      if (context.source[sample().sprecPrimarySampleContainer()]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_PRIMARY_SAMPLE_CONTAINER
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().sprecPrimarySampleContainer().code()]
          }
        }
      }
      if (context.source[sample().sprecPreCentrifugationDelay()]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_PRE_CENTRIFUGATION_DELAY
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().sprecPreCentrifugationDelay().code()]
          }
        }
      }
      if (context.source[sample().sprecPreCentrifugationDelayDate()]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_PRE_CENTRIFUGATION_DELAY_DATE
          valueDateTime = context.source[sample().sprecPreCentrifugationDelayDate().date()]
        }
      }
      if (context.source[sample().sprecPostCentrifugationDelay()]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_POST_CENTRIFUGATION_DELAY
          valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().sprecPostCentrifugationDelay().code()]
          }
        }
      }
      if (context.source[sample().sprecPostCentrifugationDelayDate()]) {
        extension {
          url = FhirUrls.Extension.Sprec.SPREC_POST_CENTRIFUGATION_DELAY_DATE
          valueDateTime = context.source[sample().sprecPostCentrifugationDelayDate().date()]
        }
      }
      if (context.source[sample().stockProcessing()]) {
        extension {
          url = FhirUrls.Extension.Sprec.STOCK_PROCESSING
          valueCoding {
            system = "urn:centraxx"
            code = toNumProcessing(context.source[sample().stockProcessing().code()] as String)
          }
        }
      }
      if (context.source[sample().stockProcessingDate()]) {
        extension {
          url = FhirUrls.Extension.Sprec.STOCK_PROCESSING_DATE
          valueDateTime = context.source[sample().stockProcessingDate().date()]
        }
      }
      if (context.source[sample().secondProcessing()]) {
        extension {
          url = FhirUrls.Extension.Sprec.SECOND_PROCESSING
          valueCoding {
            system = "urn:centraxx"
            code = toNumProcessing(context.source[sample().secondProcessing().code()] as String)
          }
        }
      }
      if (context.source[sample().secondProcessingDate()]) {
        extension {
          url = FhirUrls.Extension.Sprec.SECOND_PROCESSING_DATE
          valueDateTime = context.source[sample().secondProcessingDate().date()]
        }
      }
    }
  }
}

static String toNumType(final String sampleType, final String sampleReceptacleCode) {
  //MASTER
  if (sampleType == "URINE" && sampleReceptacleCode == "URINE") return "URN" 								//Urin
  else if (sampleType == "BLDCELLS" && sampleReceptacleCode == "CPTHEP") return "NUM_pbmc_cpt" 				//CPT Heparin
  else if (sampleType == "EDTAPLASMA" && sampleReceptacleCode == "EDTA") return "EDTAWB" 					//EDTA
  else if (sampleType == "PAXGEN") return "ORG" 															//PAX-Gene
  else if (sampleType == "SALIVA") return "ORG" 															//Speichel
  else if (sampleType == "NASLSWAB") return "ORG-rachenabstrich" 											//Oropharynx Abstrich
  else if (sampleType == "THRTSWAB") return "ORG" 															//Rachen Abstrich

  //ALIQUOT
  else if (sampleType == "URINE" && sampleReceptacleCode == "MIC750") return "NUM_urinf" 					//Urin-Überstand
  else if (sampleType == "URINESED" && sampleReceptacleCode == "MIC750") return "NUM_urins" 				//Urin-Sediment
  else if (sampleType == "BLDCELLS" && sampleReceptacleCode == "MIC750") return "NUM_PBMC_C" 				//PBMC Zellen
  else if (sampleType == "EDTAPLASMA" && sampleReceptacleCode == "MIC750") return "EDTA" 					//EDTA-Plasma
  else if (sampleType == "BFFYCOAT" && sampleReceptacleCode == "MIC750") return "EDTABUF" 					//Buffy Coat

  //MASTER and ALIQUOT
  else if (sampleType == "SERUM") return "SER" 																//Serum
  else if (sampleType == "CITRATE") return "CIT"															//Citrat
  else return "Unbekannt (XXX)"
}

static String toNumProcessing(final String sourceProcessing) {

  if (sourceProcessing == "NUM_RT20min1650g") return "NUM_RT20min1650g"
  else if (sourceProcessing == "NUM_RT15min300gBremse") return "NUM_RT15min300gBremse"
  else if (sourceProcessing == "RT5min400g") return "NUM_BEGINN_ZENT"
  else if (sourceProcessing == "RT15min2500g") return "Sprec-B"
  else return "Sprec-Z"
}

static String toNumContainer(final String sampleType, final String sampleReceptacleCode) {
  //MASTER
  if (sampleType == "URINE" && sampleReceptacleCode == "URINE") return "ORG" 								//Urin
  else if (sampleType == "BLDCELLS" && sampleReceptacleCode == "CPTHEP") return "ORG" 						//CPT Heparin
  else if (sampleType == "EDTAPLASMA" && sampleReceptacleCode == "EDTA") return "ORG" 						//EDTA
  else if (sampleType == "SERUM" && sampleReceptacleCode == "SERUM") return "ORG" 							//Serum
  else if (sampleType == "CITRATE" && sampleReceptacleCode == "CITRATE") return "ORG"						//Citrat
  else if (sampleType == "PAXGEN") return "ORG" 															//PAX-Gene
  else if (sampleType == "SALIVA") return "ORG" 															//Speichel
  else if (sampleType == "NASLSWAB") return "ORG" 															//Oropharynx Abstrich
  else if (sampleType == "THRTSWAB") return "ORG" 															//Rachen Abstrich

  //ALIQUOT
  else if (sampleType == "URINE" && sampleReceptacleCode == "MIC750") return "NUM_AliContainer"			    //Urin-Überstand
  else if (sampleType == "URINESED" && sampleReceptacleCode == "MIC750") return "NUM_AliContainer" 		    //Urin-Sediment
  else if (sampleType == "BLDCELLS" && sampleReceptacleCode == "MIC750") return "NUM_AliContainer" 		    //PBMC Zellen
  else if (sampleType == "EDTAPLASMA" && sampleReceptacleCode == "MIC750") return "NUM_AliContainer" 	    //EDTA-Plasma
  else if (sampleType == "BFFYCOAT" && sampleReceptacleCode == "MIC750") return "NUM_AliContainer" 		    //Buffy Coat
  else if (sampleType == "SERUM" && sampleReceptacleCode == "MIC750") return "NUM_AliContainer" 			//Serum
  else if (sampleType == "CITRATE" && sampleReceptacleCode == "MIC750") return "NUM_AliContainer"		    //Citrat
  else return "Unbekannt (XXX)"
}

static String toNumStorage(final String sampleType) {
  switch (sampleType) {
    case "BLDCELLS":
      return "NUM --> Klinikum Wuerzburg --> N2 Tank POP -196°C"
    default:
      return "NUM --> Klinikum Wuerzburg --> Ultra-Tiefkühlschrank POP -80°C"
  }
}
