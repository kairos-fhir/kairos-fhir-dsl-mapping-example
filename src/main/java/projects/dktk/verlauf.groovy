package projects.dktk

import org.hl7.fhir.r4.model.ClinicalImpression

/**
 * Represented by a CXX Progress
 * Specified by https://simplifier.net/oncology/verlauf
 *
 * hints:
 * A CXX progress has all tumor state (Lokal, Gesamt, lymphknoten, Metastasen) always the same time. All fields are optional.
 * The clinical expression is also created, if no reference exists.
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.4
 */
clinicalImpression {

  id = "ClinicalImpression/" + context.source["id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-ClinicalImpression-Verlauf"
  }

  status = ClinicalImpression.ClinicalImpressionStatus.COMPLETED

  subject {
    reference = "Patient/" + context.source["patientcontainer.id"]
  }

  if (context.source["episode"]) {
    encounter {
      reference = "Encounter/" + context.source["episode.id"]
    }
  }

  effectiveDateTime {
    date = normalizeDate(context.source["examinationDate"] as String)
  }

  problem {
    reference = "Condition/" + context.source["tumour.centraXXDiagnosis.id"]
  }

  // Reference GesamtbeurteilungTumorstatus
  if (context.source["fullAssessmentDict"]) {
    finding {
      itemReference {
        reference = "Observation/TumorstatusGesamt-" + context.source["id"]
      }
    }
  }

  // Reference LokalerTumorstatus
  if (context.source["assessmentPrimaryDict"]) {
    finding {
      itemReference {
        reference = "Observation/TumorstatusLokal-" + context.source["id"]
      }
    }
  }

  // Reference TumorstatusLymphknoten
  if (context.source["assessmentLymphDict"]) {
    finding {
      itemReference {
        reference = "Observation/TumorstatusLymph-" + context.source["id"]
      }
    }
  }

  // Reference TumorstatusFernmetastasen
  if (context.source["assessmentMetaDict"]) {
    finding {
      itemReference {
        reference = "Observation/TumorstatusMetas-" + context.source["id"]
      }
    }
  }

  // Reference Vitalstatus
  if (context.source["assessmentMetaDict"]) {
    finding {
      itemReference {
        reference = "Observation/Vitalstatus-" + context.source["patientcontainer.id"]
      }
    }
  }

  //Reference Fernmetastasen
  context.source["metastases"]?.each { final def m ->
    finding {
      itemReference {
        reference = "Observation/Metastasis-" + m["id"]
      }
    }
  }

  context.source["histologies"]?.each { final def h ->
    finding {
      itemReference {
        reference = "Observation/Histology-" + h["id"]
      }
    }
  }

  //TODO: | Specimen| TNMp| TNMc)
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}
