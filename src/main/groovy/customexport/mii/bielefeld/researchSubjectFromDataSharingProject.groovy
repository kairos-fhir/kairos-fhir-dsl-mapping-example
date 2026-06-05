package customexport.mii.bielefeld


import org.hl7.fhir.r4.model.ResearchSubject

researchSubject {

    if (!context.source["patientcontainer"])
        return

    def patientcontainer = context.source["patientcontainer"]

    if (!context.source["consent"])
        return

    def consentVar = context.source["consent"]

    if (!context.source["consent"]["consentType"])
        return

    if (!context.source["consent"]["consentType"]["flexiStudy"])
        return

    def flexiStudy = context.source["consent"]["consentType"]["flexiStudy"]

    if (flexiStudy["status"] != "APPROVED")
        return

    if (flexiStudy["code"] == "OWL_DIZ_STUDY_BC.dummy")
        return

    final def patientOid = patientcontainer["id"]

    final def studyOid = flexiStudy["id"]

    meta {
        profile "https://www.uni-bielefeld.de/fhir/ResearchSubject/StructureDefinition/data-usage-project"
    }

    status = ResearchSubject.ResearchSubjectStatus.CANDIDATE

    id = "ResearchSubject/" + studyOid + "-" + patientOid

    identifier {
        type {
            coding {
                system = "http://terminology.hl7.org/CodeSystem/v2-0203"
                code = "ANON"
            }
        }
        system = "https://fhir.centraxx.de/system/flexiStudy/code"
        value = flexiStudy["code"]
    }

    individual {
        reference = "Patient/" + patientOid
    }

 //  verursacht Fehler: consent {
 //       reference = "Consent/" + consentVar["id"]
  //  }

    final def memberFrom = context.source["memberFrom"]

    if (memberFrom) {
        period {
            start {
                date = memberFrom as String
            }

        }
    }
}
