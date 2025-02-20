package projects.mii.greifswald

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.AbstractIdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.PrecisionDate
import de.kairos.fhir.centraxx.metamodel.SampleType
import de.kairos.fhir.centraxx.metamodel.enums.SampleCategory
import de.kairos.fhir.centraxx.metamodel.enums.SampleKind
import de.kairos.fhir.dsl.r4.context.BuiltinConcept
import javassist.tools.rmi.Sample
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Specimen

import static de.kairos.fhir.centraxx.metamodel.Multilingual.LANGUAGE
import static de.kairos.fhir.centraxx.metamodel.Multilingual.NAME
import static de.kairos.fhir.centraxx.metamodel.RootEntities.abstractSample
import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX SAMPLE

 * @since fhir-dsl-1.45.0, HDRPv.2025.5.6
 */
specimen {

  if (![SampleCategory.DERIVED, SampleCategory.MASTER].contains(context.source[sample().sampleCategory()] as SampleCategory)) {
    return
  }

  id = "Specimen/" + context.source[sample().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Specimen"
  }

  if (context.source[abstractSample().diagnosis()]) {
    extension {
      url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Diagnose"
      valueReference {
        reference = "Condition/" + context.source[sample().diagnosis().id()]
      }
    }
  }

  if (context.source[sample().organisationUnit()]) {
    extension {
      url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/VerwaltendeOrganisation"
      valueReference {
        reference = "Organization/" + context.source[sample().organisationUnit().id()]
      }
    }
  }


  context.source[sample().idContainer()].each { final def idObj ->
    identifier {
      type {
        coding {
          system = FhirUrls.System.IdContainerType.BASE_URL
          code = idObj[AbstractIdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE] as String
          display = idObj[AbstractIdContainer.ID_CONTAINER_TYPE][IdContainerType.NAME] as String
        }
      }
      value = idObj[AbstractIdContainer.PSN]
    }
  }

  // Mapping of Cxx SampleStatus is possible
  status = Specimen.SpecimenStatus.AVAILABLE

  if (context.source[sample().sampleType()]) {
    type {
      final List<Coding> translationResult = context.translateBuiltinConceptToAll(BuiltinConcept.MII_SPREC_SNOMED_SAMPLETYPE,
          context.source[sample().sampleType().sprecCode()])
      if (!translationResult.isEmpty()) {
        coding.addAll(translationResult)
      }
      else {
        coding {
          system = "https://doi.org/10.1089/bio.2017.0109/sample-type"
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
    if ((context.source[sample().parent().sampleCategory()] as SampleCategory) != SampleCategory.ALIQUOTGROUP) {
      parent {
        reference = "Specimen/" + context.source[sample().parent().id()]
      }
    } else if ((context.source[sample().parent().sampleCategory()] as SampleCategory) == SampleCategory.ALIQUOTGROUP) {
      parent {
        reference = "Specimen/" + context.source[sample().parent().parent().id()]
      }
    }
  }

  collection {
    collectedDateTime = context.source[sample().samplingDate().date()]

    if (context.source[sample().orgSample()]) {

      bodySite {
        //Organs are specified user-defined in CXX. sct coding only applies, when used for coding in CXX
        coding {
          system = FhirUrls.System.Organ.BASE_URL
          code = context.source[sample().orgSample().code()]
          display = context.source[sample().orgSample().multilinguals()]?.find { it[LANGUAGE] == "de" }?.getAt(NAME)
        }

        // Needs a mapping to snowmed ct or idc-o-3
        coding {
          system = "http://snomed.info/sct"
        }
      }
    }
  }

  if (context.source[sample().sampleLocation()] != null && !context.source[sample().sampleLocation().locationSchema().workspace()]) {
    processing {
      if (context.source[sample().sampleLocation().temperature()]) {
        extension {
          url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/Temperaturbedingungen"
          valueRange {
            low {
              value = context.source[sample().sampleLocation().temperature()]
              unit = "°C"
              system = "http://unitsofmeasure.org"
              code = "Cel"
            }
          }
        }
      }

      procedure {
        coding {
          system = "http://snomed.info/sct"
          code = "1186936003"
        }
      }

      if (context.source[sample().repositionDate()] && context.source[sample().repositionDate().date()]) {
        timePeriod {
          start = context.source[sample().repositionDate().date()]
        }
      }

    }
  }

  container {
    if (context.source[sample().receptable()]) {
      type {

        if (context.source[sample().receptable().sprecCode()]) {
          final List<Coding> translationResult = context.translateBuiltinConceptToAll(BuiltinConcept.MII_SPREC_SNOMED_LONGTERMSTORAGE,
              context.source[sample().receptable().sprecCode()])
          if (translationResult) {
            coding.addAll(translationResult)
          }
        }

        coding {
          system = "https://doi.org/10.1089/bio.2017.0109/long-term-storage"
          code = context.source[sample().receptable().sprecCode()]
        }
      }


      capacity {
        value = context.source[sample().receptable().size()]

        final Coding translationResult = context.translateBuiltinConcept(BuiltinConcept.CXX_UCUM,
            context.source[sample().receptable().volume()])

        if (translationResult) {
          system = "http://unitsofmeasure.org"
          code = translationResult.code
          unit = translationResult.display
        } else {
          system = FhirUrls.System.AmountUnit.BASE_URL
          unit = context.source[sample().receptable().volume()]
          code = context.source[sample().receptable().volume()]
        }
      }
    }

    specimenQuantity {
      value = context.source[sample().restAmount().amount()]
      final Coding translationResult = context.translateBuiltinConcept(BuiltinConcept.CXX_UCUM,
          context.source[sample().restAmount().unit()])

      if (translationResult) {
        system = "http://unitsofmeasure.org"
        code = translationResult.code
        unit = translationResult.display
      } else {
        system = FhirUrls.System.AmountUnit.BASE_URL
        unit = context.source[sample().restAmount().unit()]
        code = context.source[sample().restAmount().unit()]
      }
    }

    // primary container if liquid, stock type if solid
    additiveCodeableConcept {
      if (context.source[sample().sprecPrimarySampleContainer()]) {

        final List<Coding> translationResult = context.translateBuiltinConceptToAll(BuiltinConcept.MII_SPREC_SNOMED_PRIMARYCONTAINER,
            context.source[sample().sprecPrimarySampleContainer().sprecCode()])
        if (translationResult) {
          coding.addAll(translationResult)
        }
        coding {
          system = "https://doi.org/10.1089/bio.2017.0109/type-of-primary-container"
          code = context.source[sample().sprecPrimarySampleContainer().sprecCode()]
        }
      }

      if (context.source[sample().stockType()]) {

        final List<Coding> translationResult = context.translateBuiltinConceptToAll(BuiltinConcept.MII_SPREC_SNOMED_FIXATIONTYPE,
            context.source[sample().stockType().sprecCode()])
        if (translationResult) {
          coding.addAll(translationResult)
        }
        coding {
          system = "https://doi.org/10.1089/bio.2017.0109/type-of-primary-container"
          code = context.source[sample().stockType().sprecCode()]
        }
      }
    }

  }

  note {
    text = context.source[sample().note()] as String
  }
}

