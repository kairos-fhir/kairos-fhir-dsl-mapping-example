package projects.mii.modul.biobanking

import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import de.kairos.fhir.centraxx.metamodel.SampleLocation
import org.hl7.fhir.r4.model.Specimen

import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX SAMPLE
 * Codings are custumized in CXX. Therefore, the code system is unknown. In this example the usage of snomed-ct is assumed to be used.
 * If other codings are used in the local CXX system, the code systems must be adjusted.
 * TODO: NOTE: The script was written while the corresponding FHIR profile on simplifier.net was still in draft state. Changes in the profile might require adjustments in the script.
 * @author Jonas Küttner
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 */
specimen {
  id = "Sample/" + context.source[sample().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ProfileSpecimenBioprobe"
  }

  extension {
    url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ExtensionDiagnose"
    valueReference {
      reference = "Diagnosis/" + context.source[sample().diagnosis().id()]
    }
  }

  extension {
    url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ExtensionVerwaltendeOrganisation"
    valueReference {
      reference = "OrganisationUnit/" + context.source[sample().organisationUnit().id()]
    }
  }

  // Specimen status is customized in CXX. Exact meaning depends on implementation in CXX.
  if (context.source[sample().sampleStatus()]) {
    status = Specimen.SpecimenStatus.NULL
  }

  if (context.source[sample().sampleType()]) {
    type {
      // Types can be specified in CXX. This code system applies only if snomed-ct is used for coding in CXX.
      coding {
        system = "http://snomed.info/sct"
        code = context.source[sample().sampleType().code()]
        display = context.source[sample().sampleType().nameMultilingualEntries()].find { final def entry ->
          "de" == entry[MultilingualEntry.LANG]
        }[MultilingualEntry.VALUE]
      }
      // SPREC is implemented in CXX.
      coding {
        system = "https://www.isber.org/page/SPREC"
        code = context.source[sample().sampleType().sprecCode()]
      }
    }
  }
  subject {
    reference = "Patient/" + context.source[sample().patientContainer().id()]
  }

  receivedTime {
    date = context.source[sample().receiptDate().date()]
  }

  if (context.source[sample().parent()]) {
    parent {
      reference = "Sample/" + context.source[sample().parent().id()]
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
          display = context.source[sample().orgSample().nameMultilingualEntries()].find { final def entry ->
            "de" == entry[MultilingualEntry.LANG]
          }[MultilingualEntry.VALUE]
        }
      }
    }
  }


  processing {
    final def temperature = context.source[sample().sampleLocation()]?.getAt(SampleLocation.TEMPERATURE) as String
    if (temperature) {
      //Temperature at the sample location is given as one value in CXX.
      extension {
        url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ExtensionTemperaturbedingungen"

        valueQuantity {
          value = temperature
          unit = "C"
          system = "http://unitsofmeasure.org"
          code = "Cel"
        }
      }
    }

    timeDateTime = context.source[sample().repositionDate().date()]
  }

  if (context.source[sample().receptable()]) {
    container {
      type {
        coding {
          system = "https://www.isber.org/page/SPREC"
          code = context.source[sample().receptable().sprecCode()]
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
    }

    note {
      text = context.source[sample().note()] as String
    }

  }
}


