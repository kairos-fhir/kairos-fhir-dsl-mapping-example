package projects.mii.modul.biobanking

import de.kairos.fhir.centraxx.metamodel.AbstractIdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.enums.SampleKind
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Specimen

import static de.kairos.fhir.centraxx.metamodel.Multilingual.LANGUAGE
import static de.kairos.fhir.centraxx.metamodel.Multilingual.NAME
import static de.kairos.fhir.centraxx.metamodel.RootEntities.abstractSample
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX SAMPLE
 * Codings are custumized in CXX. Therefore, the code system is unknown. If other codings are used in the local CXX system, the code systems must be adjusted.
 * In this example SPREC codes for the sample type, and container are translated by a static mapping based on the provide concept maps.
 * TODO: NOTE: The script was written while the corresponding FHIR profile on simplifier.net was still in draft state. Changes in the profile might require adjustments in the script.
 * @author Jonas Küttner
 * @since KAIROS-FHIR-DSL.v.1.32.0, CXX.v.2024.2.1
 */

specimen {

  id = "Specimen/" + context.source[sample().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ProfileSpecimenBioprobe"
  }

  if (context.source[abstractSample().diagnosis()]) {
    extension {
      url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ExtensionDiagnose"
      valueReference {
        reference = "Condition/" + context.source[sample().diagnosis().id()]
      }
    }
  }

  if (context.source[sample().organisationUnit()]) {
    extension {
      url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ExtensionVerwaltendeOrganisation"
      valueReference {
        reference = "Organization/" + context.source[sample().organisationUnit().id()]
      }
    }
  }


  context.source[sample().idContainer()].each { final def idObj ->
    identifier {
      type {
        coding {
          system = "urn:centraxx"
          code = idObj[AbstractIdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE] as String
          display = idObj[AbstractIdContainer.ID_CONTAINER_TYPE][IdContainerType.NAME] as String
        }
      }
      value = idObj[AbstractIdContainer.PSN]
    }
  }

  // Specimen status is customized in CXX. Exact meaning depends on implementation in CXX. Here, it is assumed that the codes of the codesystem
  // are implemented in CXX.
  status = mapSpecimenStatus(context.source[sample().sampleStatus().code()] as String)

  if (context.source[sample().sampleType()]) {
    type {
      final Map<String, String> sampleTypeMap = mapSampleType(context.source[sample().sampleType().sprecCode()] as String)
      if (sampleTypeMap) {
        coding {
          system = "http://snomed.info/sct"
          code = sampleTypeMap.code
          display = sampleTypeMap.display
        }
      } else {
        coding {
          system = "https://doi.org/10.1089/bio.2017.0109/type-of-sample"
          code = context.source[sample().sampleType().sprecCode()]
        }
      }
    }
  }
  subject {
    reference = "Patient/" + context.source[sample().patientContainer().id()]
  }

  receivedTime {
    date = context.source[sample().receiptDate()]?.getAt(PrecisionDate.DATE)
  }

  if (context.source[sample().parent()]) {
    parent {
      reference = "Specimen/" + context.source[sample().parent().id()]
    }
  }

  collection {
    collectedDateTime = context.source[sample().samplingDate().date()]
    if (context.source[sample().orgSample()]) {
      bodySite {
        //Organs are specified user-defined in CXX. sct coding only applies, when used for coding in CXX
        coding {
          system = "http://snomed.info/sct"
          code = context.source[sample().orgSample().code()]
          display = context.source[sample().orgSample().multilinguals()]?.find { it[LANGUAGE] == "de" }?.getAt(NAME)
        }
      }
    }
  }

  if (context.source[sample().sampleLocation()]) {
    processing {
      procedure {
        coding = [new Coding("https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/CodeSystem/Probenlagerung",
            "LAGERUNG",
            "Lagerung einer Probe")]
      }
    }
    timeDateTime = context.source[sample().repositionDate()]?.getAt(PrecisionDate.DATE)
  }

  if (context.source[sample().receptable()]) {
    container {
      type {
        final Map<String, String> sampleContainerMap = mapContainer(context.source[sample().receptable().sprecCode()] as String, context.source[sample().sampleKind()] as SampleKind)
        if (sampleContainerMap) {
          coding {
            system = "http://snomed.info/sct"
            code = sampleContainerMap.code
            display = sampleContainerMap.display
          }
        } else {
          coding {
            system = "https://doi.org/10.1089/bio.2017.0109/long-term-storage"
            code = context.source[sample().receptable().sprecCode()]
          }
        }
      }
      capacity {
        value = context.source[sample().receptable().size()]
        unit = context.source[sample().receptable().volume()]
      }
      specimenQuantity {
        value = context.source[sample().restAmount().amount()]
        unit = context.source[sample().restAmount().unit()]
      }
      additiveReference {
        if (context.source[sample().sprecPrimarySampleContainer()]) {
          additiveCodeableConcept {
            coding {
              system = "https://doi.org/10.1089/bio.2017.0109/type-of-primary-container"
              code = context.source[sample().sprecPrimarySampleContainer().sprecCode()]
            }
          }
        }
        if (context.source[sample().stockType()]) {
          additiveCodeableConcept {
            coding {
              system = "https://doi.org/10.1089/bio.2017.0109/type-of-primary-container"
              code = context.source[sample().stockType().sprecCode()]
            }
          }
        }
      }
      /*additiveReference {
        if (context.source[sample().sprecPrimarySampleContainer()]) {
          final TranslationResult sprecPrimaryContainerTranslationResult = translateConceptMap(context.source[sample().sprecPrimarySampleContainer().sprecCode()] as String, sprecPrimaryContainerConceptMapUrl)
          if (sprecPrimaryContainerTranslationResult.code) {
            additiveCodeableConcept {
              system = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/ValueSet/additive"
              code = sprecPrimaryContainerTranslationResult.code
              display = sprecPrimaryContainerTranslationResult.display
            }
          }
        }*/
      /*if (context.source[sample().stockType()]) {
        final TranslationResult sprecFixationTypeTranslationResult = translateConceptMap(context.source[sample().stockType().sprecCode()] as String, sprecFixationTypeConceptMapUrl)
        if (sprecFixationTypeTranslationResult.code) {
          additiveCodeableConcept {
            system = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/ValueSet/additive"
            code = sprecFixationTypeTranslationResult.code
            display = sprecFixationTypeTranslationResult.display
          }
        }
      }*/
    }
  }
  note {
    text = context.source[sample().note()] as String
  }
}

static Specimen.SpecimenStatus mapSpecimenStatus(final String specimenStatus) {
  switch (specimenStatus) {
    case "available": return Specimen.SpecimenStatus.AVAILABLE
    case "unavailable": return Specimen.SpecimenStatus.UNAVAILABLE
    case "unsatisfactory": return Specimen.SpecimenStatus.UNSATISFACTORY
    case "entered-in-error": return Specimen.SpecimenStatus.ENTEREDINERROR
    default: return Specimen.SpecimenStatus.NULL
  }
}

static Map<String, String> mapSampleType(final String sprecCode) {
  switch (sprecCode) {
    case "ASC": return ["code"   : "309201001",
                        "display": "Ascitic fluid sample (specimen)"]
    case "AMN": return ["code"   : "119373006",
                        "display": "Amniotic fluid specimen (specimen)"]
    case "BAL": return ["code"   : "258607008",
                        "display": "Bronchoalveolar lavage fluid sample (specimen)"]
    case "BLD": return ["code"   : "420135007",
                        "display": "Whole blood (substance)"]
    case "BMA": return ["code"   : "396997002",
                        "display": "Specimen from bone marrow obtained by aspiration (specimen)"]
    case "BMK": return ["code"   : "446676001",
                        "display": "Expressed breast milk specimen (specimen)"]
    case "BUF": return ["code"   : "258587000",
                        "display": "Buffy coat (specimen)"]
    case "BFF": return ["code"   : "258587000",
                        "display": "Buffy coat (specimen)"]
    case "CEL": return ["code"   : "404798000",
                        "display": "Peripheral blood mononuclear cell (cell)"]
    case "BON": return ["code"   : "430268003",
                        "display": "Specimen from bone (specimen)"]
    case "CRD": return ["code"   : "122556008",
                        "display": "Cord blood specimen (specimen)"]
    case "HAR": return ["display"    : "Hair specimen (specimen)",
                        "equivalence": "equivalent"]
    case "NAL": return ["code"   : "119327009",
                        "display": "Nail specimen (specimen)"]
    case "NAS": return ["code"   : "258467004",
                        "display": "Nasopharyngeal washings (specimen)"]
    case "PLC": return ["code"   : "119403008",
                        "display": "Specimen from placenta (specimen)"]
    case "PFL": return ["code"   : "418564007",
                        "display": "Pleural fluid specimen (specimen)"]
    case "RBC": return ["code"   : "119351004",
                        "display": "Erythrocyte specimen (specimen)"]
    case "SEM": return ["code"   : "119347001",
                        "display": "Seminal fluid specimen (specimen)"]
    case "SPT": return ["code"   : "119334006",
                        "display": "Sputum specimen (specimen)"]
    case "SYN": return ["code"   : "119332005",
                        "display": "Synovial fluid specimen (specimen)"]
    case "TER": return ["code"   : "122594008",
                        "display": "Tears specimen (specimen)"]
    case "TTH": return ["code"   : "430319000",
                        "display": "Specimen from tooth (specimen)"]
    case "SER": return ["code"       : "119364003",
                        "display"    : "Serum specimen (specimen)",
                        "equivalence": "equivalent"]
    case "CSF": return ["code"   : "258450006",
                        "display": "Cerebrospinal fluid sample (specimen)"]
    case "SAL": return ["code"   : "119342007",
                        "display": "Saliva specimen (specimen)"]
    case "STL": return ["code"   : "119339001",
                        "display": "Stool specimen (specimen)"]
    case "U24": return ["code"   : "276833005",
                        "display": "24 hour urine sample (specimen)"]
    case "URN": return ["code"   : "278020009",
                        "display": "Spot urine sample (specimen)"]
    case "URM": return ["code"   : "122575003",
                        "display": "Urine specimen (specimen)"]
    case "URT": return ["code"   : "409821005",
                        "display": " Timed urine specimen (specimen)"]
    case "DWB": return ["code"   : "119294007",
                        "display": "Dried blood specimen (specimen)"]
    case "PL1": return ["code"   : "119361006",
                        "display": "Plasma specimen (specimen)"]
    case "PL2": return ["code"   : "119361006",
                        "display": "Plasma specimen (specimen)"]
    default: return null
  }
}

static mapContainer(final String sprecCode, final SampleKind sampleKind)  {
  switch (sprecCode) {
    case ["A", "B", "V", "J", "K", "S", "T", "W"]: return ["code"   : "34234003:840560000=256633009",
                      "display": "Plastic tube , device (physical object): Has compositional material=Polypropylene (substance)"]
    case ["C", "D", "E", "N"] : return ["code"   : "83059008",
                                                     "display": "Tube, device (physical object)"]
    case ["F", "G", "H", "I", "O"] && sampleKind == SampleKind.TISSUE: return ["code": "464601003",
                                                           "display": "Tissue storage straw (physical object)"]
    case ["L", "M"]: return [ "code": "434822004",
                              "display": "Specimen well (physical object)"]
    case ["Q"] : return ["code": "463490008",
                         "display": "Medical bag (physical object)"]
    case ["Z"]: return ["code": "706437002",
                        "display": "Container (physical object)"]
    default: null
  }
}





