package projects.cxx.napkon.zebanc


import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.SampleIdContainer
import de.kairos.fhir.centraxx.metamodel.enums.SampleCategory

import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

/**
 * Represented by a CXX AbstractSample
 * @author Jonas Küttner, Mike Wähnert
 * @since v.1.8.0, CXX.v.3.8.1.1
 *
 * The mapping transforms pooled specimen (that are set to rest amount 0) from the BB Charité system to the DZHK Greifswald system.
 *
 */
specimen {

  // 1. Filter sample category
  final SampleCategory category = context.source[sample().sampleCategory()] as SampleCategory
  final boolean containsCategory = [SampleCategory.MASTER].contains(category)
  if (!containsCategory) {
    return
  }

  // 2. Filter OrgUnit (NAPKON(-HAP), NAPKON-POP)
  final String[] list = ["NAPKON", "NAPKON-POP"]
  final boolean containsOrgUnit = list.contains(context.source[sample().organisationUnit().code()] as String)
  if (!containsOrgUnit) {
    return
  }

  // 3. get pooled samples and set appropriate napkon sample id containing 20 numbers
  final def sampleIdContainer = context.source[sample().idContainer()]?.find { final def entry ->
    "NAPKONProbenID" == entry[SampleIdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE)
  }

  // 4. Filter mastersamples of HEPFIX
  if (!sampleIdContainer || (sampleIdContainer?.getAt(SampleIdContainer.PSN) as String).contains("_BB")) {
    return
  }

  // get Id
  final String napkonId = sampleIdContainer?.getAt(SampleIdContainer.PSN) as String
  if (napkonId && napkonId.length() == 20) {
    id = "Specimen/" + context.source[sample().id()] + "0" // to ref a diff sample than first part / sample before

    // ref sample id
    identifier {
      type {
        coding {
          system = "urn:centraxx"
          code = "SAMPLEID"
        }
      }
      value = napkonId.substring(10, 20) // the right part of the id
    }

    // change rest amount
    container {
      specimenQuantity {
        value = 0
        unit = "ML" // can be UNKNOWN but is no option here --context.source[sample().restAmount().unit()]
        system = "urn:centraxx"
      }
    }

    // change sample location -no! Cxx will do that for us?
    /*
    extension {
        url = FhirUrls.Extension.Sample.SAMPLE_LOCATION
        extension {
            url = FhirUrls.Extension.Sample.SAMPLE_LOCATION_PATH
            valueString = ""
        }
    }
    */
    // change availabitlity
    status = "unavailable"
  }
}
