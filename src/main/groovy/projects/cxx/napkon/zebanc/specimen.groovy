package projects.cxx.napkon.zebanc

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.SampleIdContainer
import de.kairos.fhir.centraxx.metamodel.enums.SampleCategory
import de.kairos.fhir.centraxx.metamodel.enums.SampleKind

import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX AbstractSample
 * @author Jonas Küttner, Mike Wähnert
 * @since v.1.8.0, CXX.v.3.18.1.1
 *
 * The mapping transforms specimen from the BB Charité system to the DZHK Greifswald system.
 *
 */
specimen {

  // 1. Filter sample category
  final SampleCategory category = context.source[sample().sampleCategory()] as SampleCategory
  final boolean containsCategory = [SampleCategory.DERIVED, SampleCategory.MASTER, SampleCategory.ALIQUOTGROUP].contains(category)
  if (!containsCategory) {
    return
  }

  // 2. Filter OrgUnit (NAPKON(-HAP), NAPKON-POP)
  final String[] list = ["NAPKON", "NAPKON-POP"]
  final boolean containsOrgUnit = list.contains(context.source[sample().organisationUnit().code()] as String) ||
      list.contains(context.source[sample().parent().organisationUnit().code()] as String)
  if (!containsOrgUnit) {
    return
  }

  // 3. get sample Ids
  final def externalSampleIdContainer = context.source[sample().idContainer()]?.find { final def entry ->
    "NAPKONProbenID" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }
  final def derivedSampleIdContainer = context.source[sample().idContainer()]?.find { final def entry ->
    "SAMPLEID" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  // 4a Filter HEPFIX master samples (empty napkon id or suffix _BB)
  if ((category == SampleCategory.MASTER && !externalSampleIdContainer) ||
      (externalSampleIdContainer && (externalSampleIdContainer?.getAt(SampleIdContainer.PSN) as String).contains("_BB"))) {
    return
  }

  // 4b Filter HEPFIX for derived and aliquot groups
  if (context.source[sample().sampleType().code()] == "HEPARINFIXATED") {
    return
  }

  // 5. Get pooled samples and set appropriate napkon sample id (left part)
  String napkonId = externalSampleIdContainer?.getAt(SampleIdContainer.PSN) as String
  if (napkonId && napkonId.length() == 20) {
    napkonId = napkonId.substring(0, 10)
  }

  // set the fhir resource id
  id = "Specimen/" + context.source[sample().id()]

  // 5: mapping of the ids
  // the sample id of the BB is provided as external sampled id to DZHK.
  // The external sample id of BB is provided as sample id to DZHK
  if (externalSampleIdContainer) { // must be master
    if (napkonId) {
      identifier {
        type {
          coding {
            system = "urn:centraxx"
            code = "SAMPLEID"
          }
        }
        value = napkonId
      }
    }
    if (derivedSampleIdContainer) {
      identifier {
        type {
          coding {
            system = "urn:centraxx"
            code = "EXTSAMPLEID"
          }
        }
        value = derivedSampleIdContainer?.getAt(SampleIdContainer.PSN)
      }
    }
  } else if (derivedSampleIdContainer) // must be a derived sample
  {
    identifier {
      type {
        coding {
          system = "urn:centraxx"
          code = "SAMPLEID"
        }
      }
      value = derivedSampleIdContainer?.getAt(SampleIdContainer.PSN)
    }
  }

  //6: setting the repositionDate
  if (context.source[sample().repositionDate()]) {
    extension {
      url = FhirUrls.Extension.Sample.REPOSITION_DATE
      valueDateTime = context.source[sample().repositionDate().date()]
    }
  }

  // TODO this is only valid for aliquots - check available temperature and handle case if no temperature exists
  //7: Standard location path
  if (category == SampleCategory.DERIVED) {
    String locPath = context.source[sample().sampleLocation().locationPath()] as String
    if (context.source[sample().sampleLocation().temperature()] == -175.0) {
      locPath = "N2 Tank -196°C"
    } else if (context.source[sample().sampleLocation().temperature()] == -80.0) {
      locPath = "Ultra-Tiefkühlschrank -80°C"
    }

    extension {
      url = FhirUrls.Extension.Sample.SAMPLE_LOCATION
      extension {
        url = FhirUrls.Extension.Sample.SAMPLE_LOCATION_PATH
        valueString = "NUM --> Berlin CVK --> " + "Testlager"// später: locPath // (Tiefkühler -20°C, Ultra-Tiefkühlschrank -80°C)
      }
    }
  }

  // mapping of organization unit
  String OE = ""
  if (context.source[sample().organisationUnit().code()] == "NAPKON") {
    OE = "NUM_BER_HAP" // "NUM_Berlin"
  } else if (context.source[sample().organisationUnit().code()] == "NAPKON-POP") {
    OE = "NUM_BER_POP"
  }

  //4: Organization unit attached to sample
  extension {
    url = FhirUrls.Extension.Sample.ORGANIZATION_UNIT
    valueReference {
      // by identifier
      identifier {
        value = OE
      }
    }
  }

  // Mapping of the derivalDate
  if (context.source[sample().derivalDate()]) {
    extension {
      url = FhirUrls.Extension.Sample.DERIVAL_DATE
      valueDateTime = context.source[sample().derivalDate().date()]
    }
  }

  // Set availability of sample
  status = context.source[sample().restAmount().amount()] > 0 ? "available" : "unavailable"

  // sampleType
  type {
    coding {
      system = "urn:centraxx"
      code = toDzhkType(context.source[sample().sampleType().code()])
    }
  }

  final def patIdContainer = context.source[sample().patientContainer().idContainer()]?.find {
    "NAPKONPATID" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  if (patIdContainer) {
    subject {
      identifier {
        value = patIdContainer[IdContainer.PSN]
        type {
          coding {
            system = "urn:centraxx"
            code = "LIMSPSN" // patIdContainer[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
          }
        }
      }
    }
  }

  if (context.source[sample().parent()] != null) {
    parent {
      // Reference by identifier SampleId, because parent MasterSample might already exists in the target system
      final def extSampleIdParent = context.source[sample().parent().idContainer()]?.find { final def entry ->
        "NAPKONProbenID" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
      }
      if (SampleCategory.MASTER == context.source[sample().parent().sampleCategory()] as SampleCategory && extSampleIdParent) {
        identifier {
          type {
            coding {
              code = "SAMPLEID"
            }
          }

          final def psn = extSampleIdParent[SampleIdContainer.PSN] as String
          value = psn.length() == 20 ? psn.substring(0, 10) : psn
        }
      } else {
        reference = "Specimen/" + context.source[sample().parent().id()]
      }
    }
  }

  // Eingangsdatum
  // TODO receiptDate partly available, choose that or samplingDate, but not in case of HEP
  if (context.source[sample().sampleType().code()] != "Heparin") {
    receivedTime {
      date = context.source[sample().receiptDate().date()] ? context.source[sample().receiptDate().date()] : context.source[sample().samplingDate().date()]
    }
  }

  // Entnahmedatum
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

  // Probenbehälter
  container {
    if (context.source[sample().receptable()]) {
      identifier {
        value = toDzhkContainer(context.source[sample().receptable().code()])
        system = "urn:centraxx"
      }

      // info: not mapped because it would overwrite the capacity in destination system
      /*capacity {
          value = context.source[sample().receptable().size()]
          unit = context.source[sample().restAmount().unit()]
          system = "urn:centraxx"
      }*/
    }

    specimenQuantity {
      value = context.source[sample().restAmount().amount()] as Number
      unit = context.source[sample().restAmount().unit()]
      system = "urn:centraxx"
    }
  }

  // Probenkategorie
  extension {
    url = FhirUrls.Extension.SAMPLE_CATEGORY
    valueCoding {
      system = "urn:centraxx"
      code = context.source[sample().sampleCategory()]
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
            code = toDzhkPrimaryContainer(context.source[sample().sprecPrimarySampleContainer().code()] as String)
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


// Mappings of the SampleType-Codes
static String toDzhkType(final Object sourceType) {
  switch (sourceType) {
    case "ZB": return "DHZB_ZB"
    case "URN": return "URN"
    case "URINSEDI": return "NUM_urins"
    case "SER": return "SER"
    case "SAL": return "NUM_speichel"
    case "NASO-A": return "NUM_nasen-rachenabstrich"
    case "ORO-A": return "NUM_rachenabstrich"
    case "Heparin": return "NUM_pbmc_hep"
    case ["EP", "EPPL1"]: return "EDTA"
    case ["CP", "Citrat"]: return "CIT"
    case "BLDRNASTABIL": return "NUM_pax"
    case "EDTA": return "EDTAWB"
      // case ["SPT", "SPT(ind)"]:

      // CTU processed
    case "HEPAP": return "NUM_heppl"
    case "PBMC-l": return "NUM_PBMC_C"
    default: return sourceType
  }
}

// Mapping of the stockProcessing codes.
static String toDzhkProcessing(final String sourceProcessing) {
  switch (sourceProcessing) {
    case "1000g10minob": return "NUM_BEGINN_ZENT"
    case "1000g30smb": return "NUM_BEGINN_ZENT"
    case "2000 g 15 min 18°C mB": return "NUM_RT15min2000g"
    case "2000 g 15 min 18C": return "NUM_RT15min2000g"
    case "400g10minrt": return "NUM_BEGINN_ZENT"
    default: return sourceProcessing
  }
}


// Mappings for container codes (Probenbehälter)
static String toDzhkContainer(final Object sourceType) {
  switch (sourceType) {
    case ["CITRATVACUTAINER", "EDTAVACU", "eSwab", "ET 1.5", "ET2ml", "ORG", "PAXgeneRNA", "SALIVETTE", "SER", "TempusBlutRNATube", "UR"]: return "ORG"
    case "LVL-2D-Cryo-0.5-A": return "NUMCryoAliquot500"
    case "LVL-2D-Cryo-2.0-A": return "NUMCryoAliquot2000"
    default: return sourceType
  }
}

// Mapping for primary container (SPREC)
static String toDzhkPrimaryContainer(final String sourceContainer) {
  switch (sourceContainer) {
    case "ESWAB": return "ZZZ"
    case "HEP": return "HEP"
    case "ORG": return "ORG"
    case "PAX": return "PAX"
    case "PED": return "PED"
    case "SALIVETTE": return "ORG"
    case "SCI": return "SCI"
    case "SST": return "SST"
    case "TEM": return "TEM"
    case "URIN": return "ZZZ"
    default: return sourceContainer
  }
}
