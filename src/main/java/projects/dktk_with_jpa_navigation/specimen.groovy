package projects.dktk_with_jpa_navigation


import static de.kairos.fhir.centraxx.metamodel.AbstractCode.CODE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.ID_CONTAINER_TYPE
import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
import static de.kairos.fhir.centraxx.metamodel.RootEntities.abstractSample

/**
 * Represented by a CXX AbstractSample
 *
 * Specified by https://simplifier.net/bbmri.de/specimen
 *
 * hints:
 * The DKTK oncology profiles does not contain a separate specimen, instead of the BBMRI specimen should be used. Unfortunately,
 * the BBMRI specifies another Organization (https://simplifier.net/bbmri.de/collection) than the DKTK oncology, which is much different.
 * To avoid conflicts between both organization profiles, the specimen collection extension has been removed.
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.2
 */
specimen {

  id = "Specimen/" + context.source[abstractSample().id()]

  meta {
    profile "https://fhir.bbmri.de/StructureDefinition/Specimen"
  }

  context.source[abstractSample().idContainer()]?.each { final idc ->
    identifier {
      value = idc[PSN]
      type {
        coding {
          system = "urn:centraxx"
          code = idc[ID_CONTAINER_TYPE]?.getAt(CODE)
        }
      }
      system = "urn:centraxx"
    }
  }

  status = context.source[abstractSample().restAmount().amount()] > 0 ? "available" : "unavailable"

  type {
    coding {
      system = "urn:centraxx"
      code = context.source[abstractSample().sampleType().code()]
    }
    if (context.source[abstractSample().sampleType().sprecCode()]) {
      coding += context.translateBuiltinConcept("sprec3_bbmri_sampletype", context.source[abstractSample().sampleType().sprecCode()])
      coding {
        system = "https://doi.org/10.1089/bio.2017.0109"
        code = context.source[abstractSample().sampleType().sprecCode()]
      }
    } else {
      coding += context.translateBuiltinConcept("centraxx_bbmri_samplekind", context.source[abstractSample().sampleType().kind()] ?: "")
    }
  }

  subject {
    reference = "Patient/" + context.source[abstractSample().patientContainer().id()]
  }

  if (context.source[abstractSample().episode()]) {
    encounter {
      reference = "Encounter/" + context.source[abstractSample().episode().id()]
    }
  }

  receivedTime {
    date = context.source[abstractSample().samplingDate().date()]
  }

  final def ucum = context.conceptMaps.builtin("centraxx_ucum")
  collection {
    collectedDateTime {
      date = context.source[abstractSample().samplingDate().date()]
      quantity {
        value = context.source[abstractSample().initialAmount().amount()] as Number
        unit = ucum.translate(context.source[abstractSample().initialAmount().unit()] as String)?.code
        system = "http://unitsofmeasure.org"
      }
    }
  }

  container {
    if (context.source[abstractSample().receptable()]) {
      identifier {
        value = context.source[abstractSample().receptable().code()]
        system = "urn:centraxx"
      }

      capacity {
        value = context.source[abstractSample().receptable().size()]
        unit = ucum.translate(context.source[abstractSample().restAmount().unit()] as String)?.code
        system = "http://unitsofmeasure.org"
      }
    }

    specimenQuantity {
      value = context.source[abstractSample().restAmount().amount()] as Number
      unit = ucum.translate(context.source[abstractSample().restAmount().unit()] as String)?.code
      system = "http://unitsofmeasure.org"
    }
  }

//  if (context.source["organisationUnit"]) {
//    extension {
//      url = "https://fhir.bbmri.de/StructureDefinition/Custodian"
//      valueReference {
//        reference = "Organization/" + context.source["organisationUnit.id"]
//      }
//    }
//  }

  final def temperature = toTemperature(context)
  if (temperature) {
    extension {
      url = "https://fhir.bbmri.de/StructureDefinition/StorageTemperature"
      valueCodeableConcept {
        coding {
          system = "https://fhir.bbmri.de/CodeSystem/StorageTemperature"
          code = temperature
        }
      }
    }
  }

}

static def toTemperature(final ctx) {
  final def temp = ctx.source[abstractSample().sampleLocation().temperature()]

  if (null != temp) {
    switch (temp) {
      case { it >= 2.0 && it <= 10 }:
        return "temperature2to10"
      case { it <= -18.0 && it >= -35.0 }:
        return "temperature-18to-35"
      case { it <= -60.0 && it >= -85.0 }:
        return "temperature-60to-85"
    }
  }

  final def sprec = ctx.source[abstractSample().receptable().sprecCode()]
  if (null != sprec) {
    switch (sprec) {
      case ['C', 'F', 'O', 'Q']:
        return "temperatureLN"
      case ['A', 'D', 'J', 'L', 'N', 'O', 'S']:
        return "temperature-60to-85"
      case ['B', 'H', 'K', 'M', 'T']:
        return "temperature-18to-35"
      default:
        return "temperatureOther"
    }
  }

  return null
}

