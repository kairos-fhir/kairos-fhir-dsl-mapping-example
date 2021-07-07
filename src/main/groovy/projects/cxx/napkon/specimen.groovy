package projects.cxx.napkon

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
 * @since v.1.8.0, CXX.v.3.8.1.1
 *
 * The mapping transforms specimen from the HUB Hannover system to the DZHK Greifswald system.
 *
 * Hints:
 * 1. Filter: only master samples, derived samples and aliquot groups are allowed to export.
 * 2. Filter: Only samples of the OrgUnit P-2216-NAP are exported.
 * 3. Mapping: postcenrtifugationdate (Einfrierzeitpunkt HUB) to firstrepositiondate (Einfrierzeitpunkt DZHK)
 * 4. Mapping OrgUnit: P-2216-NAP (HUB) to "NUM_Hannover" (DZHK) TODO: verify codes
 * 5. Mapping IDs: EXTSAMPLEID (HUB) to SAMPLEID (DZHK) TODO: Turn around for reverse sync
 * 6. Filter: Link only LaborMethod "DZHKFLAB" NUM WF3 -> see observation.groovy
 *
 * TODO: 7. Check mapping of the sample type.
 */
specimen {

  // 1. Filter sample category
  (!([SampleCategory.DERIVED, SampleCategory.MASTER, SampleCategory.ALIQUOTGROUP].contains(context.source[sample().sampleCategory()] as SampleCategory))) {
    return
  }

  // 2. Filter OrgUnit
  if ("P-2216-NAP" != context.source[sample().organisationUnit().code()]) {
    return
  }

  id = "Specimen/" + context.source[sample().id()]

  final def externalSampleIdContainer = context.source[sample().idContainer()]?.find { final def entry ->
    "EXTSAMPLEID" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  final def sampleIdContainer = context.source[sample().idContainer()]?.find { final def entry ->
    "SAMPLEID" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  // 5: cross-mapping of the ids
  if (externalSampleIdContainer) {
    identifier {
      type {
        coding {
          system = "urn:centraxx"
          code = "SAMPLEID"
          display = "Proben ID"
        }
      }
      value = externalSampleIdContainer?.getAt(SampleIdContainer.PSN)
    }
  }

  if (sampleIdContainer) {
    identifier {
      type {
        coding {
          system = "urn:centraxx"
          code = "EXTSAMPLEID"
          display = "Externe Proben ID"
        }
      }
      value = sampleIdContainer?.getAt(SampleIdContainer.PSN)
    }
  }

  //2: mapping of centrifugation date on reposition date.
  //The first reposition date is not exported. Therefore, the reposition date is used here.
  extension {
    url = "https://fhir.centraxx.de/extension/sample/repositionDate" + FhirUrls.Extension.Sample.REPOSITION_DATE
    valueDateTime = context.source[sample().sprecPostCentrifugationDelayDate()]
  }


  //3: Standard location path
  extension {
    url = "https://fhir.centraxx.de/extension/sample/sampleLocation"
    extension {
      url = "https://fhir.centraxx.de/extension/sample/sampleLocationPath"
      valueString = "NUM --> Klinikum Hannover --> Aliqoute"
    }
  }

  //TODO: Standard organization unit attached to sample
  // extension{
  //  url = "https://fhir.centraxx.de/extension/sample/sampleOrgunit"
  //  valueCoding{
  //    system = "https://fhir.centraxx.de/extension/sample/sampleOrgunit"
  //    code = "NUM_Hannover"
  //  }
  //}
  //organization {
  //  identifier{
  //    value = "OrganisationUnit/" + context.source[sample().organisationUnit().id()]
  //    type {
  //      coding {
  //        system = "https://fhir.centraxx.de/extension/sample/sampleOrgunit"
  //        code = context.source[sample().organisationUnit().code()]
  //      }
  //    }
  //  }
  //}


  status = context.source[sample().restAmount().amount()] > 0 ? "available" : "unavailable"

  type {
    coding {
      system = "urn:centraxx"
      code = toNumType(context.source[sample().sampleType().code()])
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
      reference = "Specimen/" + context.source[sample().parent().id()]
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
        value = context.source[sample().receptable().code()]
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
          url = FhirUrls.Extension.Sprec.SPFREC_FIXATION_TIME
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
            code = context.source[sample().sprecPostCentrifugationDelay()]
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
            code = toNUMProcessing(context.source[sample().stockProcessing().code()] as String)
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
            code = toNUMProcessing(context.source[sample().secondProcessing().code()] as String)
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

static String toNumType(final Object sourceType) {
  switch (sourceType) {
    case "BAL":
      return "NUM_bal"
    case "ZZZ(nab)":
      return "NUM_abstrich"
    case "ZZZ(pbm)":
      return "NUM_pbmc"
    case "SAL":
      return "NUM_speichel"
    case ["SPT", "SPT(ind)"]:
      return "NUM_sputum"
    case "ZZZ(usd)":
      return "NUM_urins"
    default:
      return sourceType
  }
}

static String toNUMProcessing(final String sourceProcessing) {
  if (sourceProcessing.startsWith("A"))
    return "Sprec-A"
  if (sourceProcessing.startsWith("B"))
    return "Sprec-B"
  if (sourceProcessing.startsWith("C"))
    return "Sprec-C"
  if (sourceProcessing.startsWith("D"))
    return "Sprec-D"
  if (sourceProcessing.startsWith("E"))
    return "Sprec-E"
  if (sourceProcessing.startsWith("F"))
    return "Sprec-F"
  if (sourceProcessing.startsWith("G"))
    return "Sprec-G"
  if (sourceProcessing.startsWith("H"))
    return "Sprec-H"
  if (sourceProcessing.startsWith("I"))
    return "Sprec-I"
  if (sourceProcessing.startsWith("J"))
    return "Sprec-J"
  if (sourceProcessing.startsWith("M"))
    return "Sprec-M"
  if (sourceProcessing.startsWith("N"))
    return "Sprec-N"
  if (sourceProcessing.startsWith("X"))
    return "Sprec-X"
  if (sourceProcessing.startsWith("Z"))
    return "Sprec-Z"
  else
    return sourceProcessing;
}
