package projects.rwthcbmb


import static de.kairos.fhir.centraxx.metamodel.RootEntities.patientMasterDataAnonymous

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * Specified by https://simplifier.net/bbmri.de/patient
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.2
 */
patient {

  id = "Patient/" + context.source["patientcontainer.id"]

  meta {
    profile "https://fhir.bbmri.de/StructureDefinition/Patient"
  }

  final def idContainer = context.source[patientMasterDataAnonymous().patientContainer().idContainer()]?.find {
    "MPI" == it["idContainerType"]?.getAt("code")
  }

  if (idContainer) {
    identifier {
      value = idContainer["psn"]
      type {
        coding {
          system = "urn:centraxx"
          code = idContainer["idContainerType"]?.getAt("code")
        }
      }
    }
  }
}

