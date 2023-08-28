package projects.cxx.napkon.ibdw

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.SampleIdContainer
import de.kairos.fhir.centraxx.metamodel.enums.SampleCategory
import de.kairos.fhir.centraxx.metamodel.enums.SampleKind
import org.hl7.fhir.r4.model.CodeableConcept
import org.slf4j.ILoggerFactory

import java.text.SimpleDateFormat

import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample
/**
 * Represented by a CXX AbstractSample
 * @author Jonas Küttner, Mike Wähnert
 * @since v.1.13.0, CXX.v.3.18.1.13
 *
 * The mapping transforms specimen from the ibdw Wuerzburg system to the NUM Greifswald system.
 * Intended to be used with POST (createOrUpdateByNaturalIdentifier) methods, because master samples already exists in the target system with a different logical fhir id.
 *
 * Criteria:
 * 1. Filter: only master samples, derived samples and aliquot groups are allowed
 * 2. Filter: Only master samples and their aliquots for which a NAPKON-ID mapping exists
 * 3. Filter: Only master samples with existing storage location (not pseudo storage)
 * 4. Filter: only samples that are still existing and have not been retrieved yet (no sampleAbstaction)
 * 5. Filter: only samples with existing first reposition date
 * 5a. Filter: only samples that were received after a specific date
 * 6. Filter: Only samples of the OrgUnits napPOP and napSUP
 * 7. Cross Mapping IDs: NAPKONSMP (ibdw) to SAMPLEID (NUM) and SAMPLEID (ibdw) to EXTSAMPLEID (NUM)
 * 8. Mapping OrgUnit: napPOP (ibdw) to "NUM_Wuerzburg_POP" (NUM) and napSUP (ibdw) to "NUM_Wuerzburg_SUEP" (NUM)
 * 9. Mapping: ibdw sampleType.code, receptable.code to NUM sampleType
 */

specimen {
    // 0. Filter patients
//    final def patientList = ["lims_911069062"] //["lims_643282707", "lims_745748710"]
//    final def currentPatientId = context.source[sample().patientContainer().idContainer()]?.find {
//        "NAPKON" == it[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
//    }
//    final boolean belongsToIncludedPatient = patientList.contains(currentPatientId[IdContainer.PSN])
//
//    if (!belongsToIncludedPatient) {
//		System.out.println("Ignored patient: " + currentPatientId[IdContainer.PSN])
//        return
//    }

    // 1. Filter sample category
    final SampleCategory category = context.source[sample().sampleCategory()] as SampleCategory
    final boolean containsCategory = [SampleCategory.DERIVED, SampleCategory.MASTER, SampleCategory.ALIQUOTGROUP].contains(category)

    if (!containsCategory) {
        return
    }

    // 2. Filter NAPKON-ID Mapping exists
    String napkonMappingExists = ""
    if (category == SampleCategory.MASTER) {
        napkonMappingExists = context.source[sample().idContainer()]?.find { final def entry ->
            "NAPKONSMP" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
    } else if (category == SampleCategory.ALIQUOTGROUP) {
        napkonMappingExists = context.source[sample().parent().idContainer()]?.find { final def entry ->
            "NAPKONSMP" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
    } else if (category == SampleCategory.DERIVED) {
        napkonMappingExists = context.source[sample().parent().parent().idContainer()]?.find { final def entry ->
            "NAPKONSMP" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
        }
    }
    if (!napkonMappingExists) {
        return
    }

    // 3. Filter Existing Storage Location assigned
//    if ("swisslabProben" == context.source[sample().sampleLocation().locationId()]) {
//        return
//    }
    Boolean sampleExists = ("swisslabProben" != context.source[sample().sampleLocation().locationId()])

    // 4. Filter samples that are still existing and have not been retrieved (no sampleAbstraction)
    // TODO: Modify to check for existing sampleAbstraction as soon as available in a future CXX release
    final String sampleType = context.source[sample().sampleType().code()]
    final restAmount = context.source[sample().restAmount().amount()]

    //ALIQUOT
    if (sampleType == "URINE"           && category == SampleCategory.DERIVED && restAmount == 0) { return }            //Urin
    else if (sampleType == "BLDCELLS"   && category == SampleCategory.DERIVED && restAmount == 0) { return }            //CPT Heparin
    else if (sampleType == "EDTAPLASMA" && category == SampleCategory.DERIVED && restAmount == 0) { return }            //EDTA
    else if (sampleType == "SERUM"      && category == SampleCategory.DERIVED && restAmount == 0) { return }            //Serum
    else if (sampleType == "CITRATE"    && category == SampleCategory.DERIVED && restAmount == 0) { return }            //Citrat
    else if (sampleType == "URINESED"   && category == SampleCategory.DERIVED && restAmount == 0) { return }            //Urin-Sediment
    else if (sampleType == "BFFYCOAT"   && category == SampleCategory.DERIVED && restAmount == 0) { return }            //Buffy Coat
    //MASTER
    else if (sampleType == "PAXGEN"     && category == SampleCategory.MASTER && restAmount == 0) { return }             //PAX-Gene
    else if (sampleType == "SALIVA"     && category == SampleCategory.MASTER && restAmount == 0) { return }             //Speichel
    else if (sampleType == "NASLSWAB"   && category == SampleCategory.MASTER && restAmount == 0) { return }             //Oropharynx Abstrich
    else if (sampleType == "THRTSWAB"   && category == SampleCategory.MASTER && restAmount == 0) { return }             //Rachen Abstrich

    // 5. Filter First Reposition Date assigned for derived samples
    if (category == SampleCategory.DERIVED && context.source[sample().firstRepositionDate()] == null) {
        return
    }

    // 5a. Filter samples older than specified date
//    String receptionDate = ""
//    if (category == SampleCategory.MASTER) {
//        receptionDate = context.source[sample().receiptDate().date()]
//    } else if (category == SampleCategory.ALIQUOTGROUP) {
//        receptionDate = context.source[sample().parent().receiptDate().date()]
//    } else if (category == SampleCategory.DERIVED) {
//        receptionDate = context.source[sample().parent().parent().receiptDate().date()]
//    }
//    
//	//System.out.println("receptionDate for " + category + " sample " + context.source[sample().id()] + ": " + receptionDate)
//
//    if (receptionDate == null || receptionDate < "2023-01-01T00:00:00.000+01:00") {
//        //System.out.println("sample ignored")
//        return
//    }

    // 6. Filter OrgUnit
    String orgUnit = ""
    if ("napSUP" == context.source[sample().organisationUnit().code()] || "napSUP" == context.source[sample().parent().organisationUnit().code()]) {
        id = "Specimen/" + context.source[sample().id()]
        orgUnit = "NUM_W_SUEP"
    } else if ("napPOP" == context.source[sample().organisationUnit().code()] || "napPOP" == context.source[sample().parent().organisationUnit().code()]) {
        id = "Specimen/" + context.source[sample().id()]
        orgUnit = "NUM_W_POP"
    } else {
        return
    }

    // Update resource - ignore missing elements
    extension {
        url = FhirUrls.Extension.UPDATE_WITH_OVERWRITE
        valueBoolean = false
    }

    final def idContainerCodeMap = ["SAMPLEID": "EXTSAMPLEID", "NAPKONSMP": "SAMPLEID"]
    final Map<String, Object> idContainersMap = idContainerCodeMap.collectEntries { final String idContainerCode, final String _ ->
        return [
                (idContainerCode): context.source[sample().idContainer()]?.find { final def entry ->
                    idContainerCode == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
                }
        ]
    } as Map<String, Object>

    // 7: cross-mapping of the ids of MASTER samples. The sample id of the ibdw is provided as external sampled id to NUM.
    // The external sample id of ibdw is provided as sample id to NUM.
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
        // Set BASISSETID
//    final def extSampleIdParent = context.source[sample().idContainer()]?.find { final def entry ->
//      "NAPKONSMP" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
//    }
//    identifier {
//      type {
//        coding {
//          system = "urn:centraxx"
//          code = "BASISSETID"
//        }
//      }
//      final def psn = extSampleIdParent[SampleIdContainer.PSN] as String
//      value = psn.substring(0,6)
//    }
    } else { // Providing ibdw sample id as sample id to NUM.
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
            // Storage of PAXgene samples is done in three steps: RT -> -20°C freezer -> -80°C freezer
            // repositionDate of storage in -20°C can only be calculated to 3h after receiptDate
            final long paxGeneRTStorageTime = 3 * 3600 * 1000
            // valueDateTime = context.source[sample().repositionDate().date()]
            switch (context.source[sample().sampleType().code()]) {
                case "PAXGEN":
                  final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
                  final String receiptDate = context.source[sample().receiptDate().date()]
                    if (receiptDate) {
                        final Date tmpDate = dateFormat.parse(context.source[sample().receiptDate().date()] as String)
                        valueDateTime = dateFormat.format(new Date(tmpDate.getTime() + paxGeneRTStorageTime))
                    } else {
                        valueDateTime = context.source[sample().repositionDate().date()]
                    }
                    break
                default:
                    valueDateTime = context.source[sample().repositionDate().date()]
            }
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

    // 8: Mapped organization unit attached to sample
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

    status = (sampleExists && context.source[sample().restAmount().amount()] > 0) ? "available" : "unavailable"

    // 9: sample type mapping
    type {
        coding {
            system = "urn:centraxx"
            code = toNumType(
                    context.source[sample().sampleType().code()] as String,
                    context.source[sample().sampleCategory()] as SampleCategory
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
                        code = "LIMSPSN"
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
        date = context.source[sample().receiptDate().date()]
    }

    collection {
        collectedDateTime {
            date = context.source[sample().samplingDate().date()]
            quantity {
                switch (context.source[sample().sampleType().code()]) {
                    case "PAXGEN":
                        value = sampleExists ? "2.50" : "0"
                        unit = "ML"
                        break
                    default:
                        value = sampleExists ? context.source[sample().initialAmount().amount()] as Number : "0"
                        unit = context.source[sample().initialAmount().unit()]
                        break
                }
                system = "urn:centraxx"
            }
        }
    }

    container {
        if (context.source[sample().receptable()]) {
            identifier {
                value = toNumContainer(
                        context.source[sample().sampleType().code()] as String,
                        context.source[sample().sampleCategory()] as SampleCategory
//                context.source[sample().receptable().code()] as String
                )
                system = "urn:centraxx"
            }

            capacity {
                switch (context.source[sample().sampleType().code()]) {
                    case "PAXGEN":
                        value = "2.50"
                        unit = "ML"
                        break
                    default:
                        value = context.source[sample().receptable().size()]
                        unit = context.source[sample().restAmount().unit()]
                        break
                }
                system = "urn:centraxx"
            }
        }

        specimenQuantity {
            switch (context.source[sample().sampleType().code()]) {
                case "PAXGEN":
                    value = sampleExists ? "2.50" : "0"
                    unit = "ML"
                    break
                default:
                    value = sampleExists ? context.source[sample().restAmount().amount()] as Number : "0"
                    unit = context.source[sample().restAmount().unit()]
                    break
            }
            system = "urn:centraxx"
        }
    }

    note {
        text = context.source[sample().note()]
    }

    extension {
        url = FhirUrls.Extension.SAMPLE_CATEGORY
        valueCoding {
            system = "urn:centraxx"
            code = context.source[sample().sampleCategory()]
        }
    }

    // if (context.source[sample().concentration()]) {
    if (context.source[sample().sampleType().code()] == "BLDCELLS" && context.source[sample().sampleCategory()] as SampleCategory == SampleCategory.DERIVED) {
        final def concentrationValue = context.source[sample().concentrationMeasurements()].find { it != null }
        if (concentrationValue) {
            extension {
                url = FhirUrls.Extension.Sample.CONCENTRATION
                valueQuantity {
                    value = concentrationValue["concentration"]["amount"]
                    unit = concentrationValue["concentration"]["unit"]
                }
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


static String toNumType(final String sampleType, final SampleCategory sampleCategory) {
    //MASTER
    if (sampleType == "URINE" && sampleCategory == SampleCategory.MASTER) return "URN"                                  //Urin
    else if (sampleType == "BLDCELLS" && sampleCategory == SampleCategory.MASTER) return "NUM_pbmc_cpt"                 //CPT Heparin
    else if (sampleType == "EDTAPLASMA" && sampleCategory == SampleCategory.MASTER) return "EDTAWB"                     //EDTA
    else if (sampleType == "PAXGEN") return "NUM_pax"                                                                   //PAX-Gene
    else if (sampleType == "SALIVA") return "NUM_speichel"                                                              //Speichel
    else if (sampleType == "NASLSWAB") return "NUM_nasen-rachenabstrich"                                                //Oropharynx Abstrich
    else if (sampleType == "THRTSWAB") return "NUM_rachenabstrich"                                                      //Rachen Abstrich

    //ALIQUOT
    else if (sampleType == "URINE" && sampleCategory == SampleCategory.DERIVED) return "NUM_urinf"                      //Urin-Überstand
    else if (sampleType == "URINESED" && sampleCategory == SampleCategory.DERIVED) return "NUM_urins"                   //Urin-Sediment
    else if (sampleType == "BLDCELLS" && sampleCategory == SampleCategory.DERIVED) return "NUM_PBMC_C"                  //PBMC Zellen
    else if (sampleType == "EDTAPLASMA" && sampleCategory == SampleCategory.DERIVED) return "EDTA"                      //EDTA-Plasma
    else if (sampleType == "BFFYCOAT" && sampleCategory == SampleCategory.DERIVED) return "EDTABUF"                     //Buffy Coat

    //MASTER and ALIQUOT and ALIQUOTGROUP
    else if (sampleType == "SERUM") return "SER"                                                                        //Serum
    else if (sampleType == "CITRATE") return "CIT"                                                                      //Citrat

    //ALIQUOTGROUP
    else if (sampleType == "URINE" && sampleCategory == SampleCategory.ALIQUOTGROUP) return "NUM_urinf"                 //Urin-Überstand
    else if (sampleType == "URINESED" && sampleCategory == SampleCategory.ALIQUOTGROUP) return "NUM_urins"              //Urin-Sediment
    else if (sampleType == "BLDCELLS" && sampleCategory == SampleCategory.ALIQUOTGROUP) return "NUM_PBMC_C"             //PBMC Zellen
    else if (sampleType == "EDTAPLASMA" && sampleCategory == SampleCategory.ALIQUOTGROUP) return "EDTA"                 //EDTA-Plasma
    else if (sampleType == "BFFYCOAT" && sampleCategory == SampleCategory.ALIQUOTGROUP) return "EDTABUF"                //Buffy Coat
    else return "Unbekannt (XXX)"
}

static String toNumProcessing(final String sourceProcessing) {

    if (sourceProcessing == "NUM_RT20min1650g") return "NUM_BEGINN_ZENT"
    else if (sourceProcessing == "NUM_RT15min300gBremse") return "NUM_RT15min300gBremse"
    else if (sourceProcessing == "RT5min400g") return "NUM_BEGINN_ZENT"
    else if (sourceProcessing == "RT15min2500g") return "Sprec-B"
    else return "Sprec-Z"
}

static String toNumContainer(final String sampleType, final SampleCategory sampleCategory) {
    //MASTER
    if (sampleType == "URINE" && sampleCategory == SampleCategory.MASTER) return "ORG"                                  //Urin
    else if (sampleType == "BLDCELLS" && sampleCategory == SampleCategory.MASTER) return "ORG"                          //CPT Heparin
    else if (sampleType == "EDTAPLASMA" && sampleCategory == SampleCategory.MASTER) return "ORG"                        //EDTA
    else if (sampleType == "SERUM" && sampleCategory == SampleCategory.MASTER) return "ORG"                             //Serum
    else if (sampleType == "CITRATE" && sampleCategory == SampleCategory.MASTER) return "ORG"                           //Citrat
    else if (sampleType == "PAXGEN") return "ORG"                                                                       //PAX-Gene
    else if (sampleType == "SALIVA") return "ORG"                                                                       //Speichel
    else if (sampleType == "NASLSWAB") return "ORG"                                                                     //Oropharynx Abstrich
    else if (sampleType == "THRTSWAB") return "ORG"                                                                     //Rachen Abstrich

    //ALIQUOT
    else if (sampleType == "URINE" && sampleCategory == SampleCategory.DERIVED) return "NUM_AliContainer"               //Urin-Überstand
    else if (sampleType == "URINESED" && sampleCategory == SampleCategory.DERIVED) return "NUM_AliContainer"            //Urin-Sediment
    else if (sampleType == "BLDCELLS" && sampleCategory == SampleCategory.DERIVED) return "NUM_AliContainer"            //PBMC Zellen
    else if (sampleType == "EDTAPLASMA" && sampleCategory == SampleCategory.DERIVED) return "NUM_AliContainer"          //EDTA-Plasma
    else if (sampleType == "BFFYCOAT" && sampleCategory == SampleCategory.DERIVED) return "NUM_AliContainer"            //Buffy Coat
    else if (sampleType == "SERUM" && sampleCategory == SampleCategory.DERIVED) return "NUM_AliContainer"               //Serum
    else if (sampleType == "CITRATE" && sampleCategory == SampleCategory.DERIVED) return "NUM_AliContainer"             //Citrat
    else return "Unbekannt (XXX)"
}

static String toNumStorage(final String sampleType) {
    switch (sampleType) {
        case "BLDCELLS":
            return "NUM --> Klinikum Würzburg --> N2 Tank POP -196°C"
            break
        default:
            return "NUM --> Klinikum Würzburg --> Ultra-Tiefkühlschrank POP -80°C"
    }
}
