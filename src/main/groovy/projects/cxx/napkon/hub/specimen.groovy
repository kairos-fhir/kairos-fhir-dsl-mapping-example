package projects.cxx.napkon.hub

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
 * @since v.1.11.0, CXX.v.3.8.1.13
 *
 * The mapping transforms specimen from the HUB Hannover system to the DZHK Greifswald system.
 * Intended to be used with POST (createOrUpdateByNaturalIdentifier) methods, because master samples already exists in the target system with a different logical fhir id.
 *
 * Hints:
 * 1. Filter: only master samples, derived samples and aliquot groups are allowed to export.
 * 2. Filter: Only samples of the OrgUnit P-2216-NAP are exported.
 * 3. Mapping: postcentrifugationdate (Einfrierzeitpunkt HUB) to firstrepositiondate (Einfrierzeitpunkt DZHK)
 * 4. Mapping OrgUnit: P-2216-NAP (HUB) to "NUM_Hannover" (DZHK)
 * 5. Mapping IDs: EXTSAMPLEID (HUB) to SAMPLEID (DZHK)
 * 6. Filter: Link only LaborMethod "DZHKFLAB" NUM WF3 -> see observation.groovy
 * 7. Mapping: HUB sampleType.code, receptable.code, sprecPrimarySampleContainer.sprecCode to DZHK sampleType
 * 8. Filter samples that were created not longer than 3 days ago.
 * 9. Mapping: Filter samples that were created not longer than 3 days ago.
 */
specimen {

  // 8. CreationDate Filter
  if (isMoreThanNDaysAgo(context.source[sample().creationDate()] as String, 3)) {
    return
  }

  // 1. Filter sample category
  final SampleCategory category = context.source[sample().sampleCategory()] as SampleCategory
  final boolean containsCategory = [SampleCategory.DERIVED, SampleCategory.MASTER, SampleCategory.ALIQUOTGROUP].contains(category)

  if (!containsCategory) {
    return
  }

  // 2. Filter OrgUnit
  if ("P-2216-NAP" == context.source[sample().organisationUnit().code()] || "P-2216-NAP" == context.source[sample().parent().organisationUnit().code()]) {
    id = "Specimen/" + context.source[sample().id()]
  } else {
    return
  }

  final def idContainerCodeMap = ["SAMPLEID": "EXTSAMPLEID", "EXTSAMPLEID": "SAMPLEID"]
  final Map<String, Object> idContainersMap = idContainerCodeMap.collectEntries { String idContainerCode, String _ ->
    return [
        (idContainerCode): context.source[sample().idContainer()]?.find { final def entry ->
          idContainerCode == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
    ]
  } as Map<String, Object>

  // 5: cross-mapping of the ids of MASTER samples. The sample id of the HUB is provided as external sampled id to DZHK.
  // The external sample id of HUB is provided as sample id to DZHK.
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
  } else { // Providing HUB sample id as sample id to DZHK.
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

  //2: mapping of centrifugation date on reposition date.
  //The first reposition date is not exported. Therefore, the reposition date is used here.
  if (context.source[sample().sprecPostCentrifugationDelayDate()]) {
    extension {
      url = FhirUrls.Extension.Sample.REPOSITION_DATE
      valueDateTime = context.source[sample().sprecPostCentrifugationDelayDate().date()]
    }
  }

  //3: Standard location path
  extension {
    url = FhirUrls.Extension.Sample.SAMPLE_LOCATION
    extension {
      url = FhirUrls.Extension.Sample.SAMPLE_LOCATION_PATH
      valueString = "NUM --> Klinikum Hannover --> Aliqoute"
    }
  }

  //4: Standard organization unit attached to sample
  extension {
    url = FhirUrls.Extension.Sample.ORGANIZATION_UNIT
    valueReference {
      // by identifier
      identifier {
        value = "NUM_H_SUEP"
      }
    }
  }

  // TODO: Mapping of the derival date
  if (context.source[sample().derivalDate()]) {
    extension {
      url = FhirUrls.Extension.Sample.DERIVAL_DATE
      valueDateTime = context.source[sample().derivalDate().date()]
    }
  }

  status = context.source[sample().restAmount().amount()] > 0 ? "available" : "unavailable"

  // 7: sample type mapping
  type {
    coding {
      system = "urn:centraxx"
      code = toDzhkType(
          context.source[sample().sampleType().code()] as String,
          context.source[sample().receptable().code()] as String,
          context.source[sample().sprecPrimarySampleContainer().sprecCode()] as String
      )
    }
  }

  final def patIdContainer = context.source[sample().patientContainer().idContainer()]?.find {
    "LIMSPSN" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
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
        "EXTSAMPLEID" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
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
        value = toDzhkContainer(context.source[sample().sampleType().code()] as String, context.source[sample().receptable().code()] as String)
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
        value = context.source[sample().concentration().amount()]
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
            code = toDzhkProcessing(context.source[sample().stockProcessing().code()] as String)
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
            code = toDzhkProcessing(context.source[sample().secondProcessing().code()] as String)
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

static boolean isMoreThanNDaysAgo(String dateString, int days) {
  final Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(dateString)
  final long differenceInMillis = (System.currentTimeMillis() - date.getTime())
  return TimeUnit.DAYS.convert(differenceInMillis, TimeUnit.MILLISECONDS) > days
}

static String toDzhkType(final String sampleType, final String sampleReceptacleCode, final String primaryContainerSprecCode) {
  //MASTER
  if (sampleType == "BLD" && sampleReceptacleCode == "StMono075" && primaryContainerSprecCode == "SST") return "SER" //Serum
  else if (sampleType == "BLD" && sampleReceptacleCode == "StMono075" && primaryContainerSprecCode == "PED") return "EDTAWB" //EDTA Vollblut
  else if (sampleType == "BLD" && sampleReceptacleCode == "StMono075" && primaryContainerSprecCode == "SCI") return "CIT" //Zitrat
  else if (sampleType == "BLD" && sampleReceptacleCode == "BDPax025") return "NUM_pax" //PAX-Gene
  else if (sampleType == "SAL" && sampleReceptacleCode == "StSali001") return "NUM_speichel" //Speichel
  else if (sampleType == "URN" && sampleReceptacleCode == "StMono085") return "URN" //Urin

  //ALIQUOT
  else if (sampleType == "ZZZ(pbm)" && sampleReceptacleCode == "Ma2D010ScT") return "NUM_pbmc_edta" //PBMC
  else if (sampleType == "SER" && sampleReceptacleCode == "Ma2D005ScT" && primaryContainerSprecCode == "SST") return "SER" //Serum
  else if (sampleType == "PL1" && sampleReceptacleCode == "Ma2D005ScT" && primaryContainerSprecCode == "SCI") return "CIT" //Citrat
  else if (sampleType == "PL1" && sampleReceptacleCode == "Ma2D005ScT") return "EDTA" //EDTA-Plasma
  else if (sampleType == "BFF" && sampleReceptacleCode == "Ma2D010ScT") return "EDTABUF" //Buffy Coat
  else if (sampleType == "ZZZ(ppu)" && sampleReceptacleCode == "Ma2D005ScT" && primaryContainerSprecCode == "URN") return "NUM_urinf" //Urin-Überstand
  else if (sampleType == "ZZZ(ppu)" && sampleReceptacleCode == "Ma2D005ScT" && primaryContainerSprecCode == "ZZZ(usd)") return "NUM_urins" //Urin-Sediment
  else if (sampleType == "ZZZ(ppm)" && sampleReceptacleCode == "Ma2D010ScT") return "NUM_PBMC_C" //PBMC Zellen
  else return "Unbekannt (XXX)"
}

static String toDzhkProcessing(final String sourceProcessing) {

  if (sourceProcessing == "A(RT_15_1000)") return "Sprec-A"
  else if (sourceProcessing == "B(15)") return "NUM_RT15min2000g"
  else if (sourceProcessing == "B(20)") return "NUM_RT20min1650g"
  else if (sourceProcessing == "N") return ""
  else if (sourceProcessing == "Z(RT_5_800_b)") return "NUM_BEGINN_ZENT"
  else if (sourceProcessing.startsWith("A")) return "Sprec-A"
  else if (sourceProcessing.startsWith("B")) return "Sprec-B"
  else if (sourceProcessing.startsWith("C")) return "Sprec-C"
  else if (sourceProcessing.startsWith("D")) return "Sprec-D"
  else if (sourceProcessing.startsWith("E")) return "Sprec-E"
  else if (sourceProcessing.startsWith("F")) return "Sprec-F"
  else if (sourceProcessing.startsWith("G")) return "Sprec-G"
  else if (sourceProcessing.startsWith("H")) return "Sprec-H"
  else if (sourceProcessing.startsWith("I")) return "Sprec-I"
  else if (sourceProcessing.startsWith("J")) return "Sprec-J"
  else if (sourceProcessing.startsWith("M")) return "Sprec-M"
  else if (sourceProcessing.startsWith("N")) return "Sprec-N"
  else if (sourceProcessing.startsWith("X")) return "Sprec-X"
  else if (sourceProcessing.startsWith("Z")) return "Sprec-Z"
  else return "Sprec-X"
}

static String toDzhkContainer(final String sampleType, final String sampleReceptacleCode) {
  //MASTER
  if (sampleType == "BLD" && sampleReceptacleCode == "StMono075") return "ORG" //Serum + EDTA Vollblut + Citrat
  else if (sampleType == "BLD" && sampleReceptacleCode == "BDPax025") return "ORG" //PAX-Gene
  else if (sampleType == "SAL" && sampleReceptacleCode == "StSali001") return "ORG" //Speichel
  else if (sampleType == "URN" && sampleReceptacleCode == "StMono085") return "ORG" //Urin

  //ALIQUOT
  else if (sampleType == "ZZZ(pbm)" && sampleReceptacleCode == "Ma2D020ScT") return "NUMCryoAliquot500" //PBMC
  else if (sampleType == "SER" && sampleReceptacleCode == "Ma2D005ScT") return "NUMCryoAliquot500" //Serum
  else if (sampleType == "PL1" && sampleReceptacleCode == "Ma2D005ScT") return "NUMCryoAliquot500" //Citrat
  else if (sampleType == "PL1" && sampleReceptacleCode == "Ma2D005ScT") return "NUMCryoAliquot500" //EDTA-Plasma
  else if (sampleType == "BFF" && sampleReceptacleCode == "Ma2D010ScT") return "NUMCryoAliquot500" //Buffy Coat
  else if (sampleType == "ZZZ(ppu)" && sampleReceptacleCode == "Ma2D005ScT") return "NUMCryoAliquot500" //Urin-Überstand
  else if (sampleType == "ZZZ(ppu)" && sampleReceptacleCode == "Ma2D005ScT") return "AliquotFluidX"     //Urin-Sediment
  else if (sampleType == "ZZZ(ppm)" && sampleReceptacleCode == "Ma2D010ScT") return "NUMCryoAliquot2000"//PBMC Zellen
  else return "Unbekannt (XXX)"
}
