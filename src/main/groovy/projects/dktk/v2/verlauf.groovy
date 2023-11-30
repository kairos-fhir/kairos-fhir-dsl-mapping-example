package projects.dktk.v2

import org.hl7.fhir.r4.model.ClinicalImpression

import static de.kairos.fhir.centraxx.metamodel.AbstractEntity.ID
import static de.kairos.fhir.centraxx.metamodel.RootEntities.progress

/**
 * Represented by a CXX Progress
 * Specified by https://simplifier.net/oncology/verlauf
 *
 * hints:
 * A CXX progress has all tumor state (Lokal, Gesamt, lymphknoten, Metastasen) always the same time. All fields are optional.
 * The clinical expression is also created, if no reference exists.
 *
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, missing References added since CXX.v.3.17.2
 */
clinicalImpression {

  id = "ClinicalImpression/" + context.source[progress().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-ClinicalImpression-Verlauf"
  }

  status = ClinicalImpression.ClinicalImpressionStatus.COMPLETED

  subject {
    reference = "Patient/" + context.source[progress().patientContainer().id()]
  }

  effectiveDateTime {
    date = normalizeDate(context.source[progress().examinationDate()] as String)
  }

  if (context.source[progress().tumour()] && hasRelevantCode(context.source[progress().tumour().centraxxDiagnosis().diagnosisCode()] as String)) {
    problem {
      reference = "Condition/" + context.source[progress().tumour().centraxxDiagnosis().id()]
    }
  }

  // Reference GesamtbeurteilungTumorstatus
  if (context.source[progress().fullAssessmentDict()]) {
    finding {
      itemReference {
        reference = "Observation/TumorstatusGesamt-" + context.source[progress().id()]
      }
    }
  }

  // Reference LokalerTumorstatus
  if (context.source[progress().assessmentPrimaryDict()]) {
    finding {
      itemReference {
        reference = "Observation/TumorstatusLokal-" + context.source[progress().id()]
      }
    }
  }

  // Reference TumorstatusLymphknoten
  if (context.source[progress().assessmentLymphDict()]) {
    finding {
      itemReference {
        reference = "Observation/TumorstatusLymph-" + context.source[progress().id()]
      }
    }
  }

  // Reference TumorstatusFernmetastasen
  if (context.source[progress().assessmentMetaDict()]) {
    finding {
      itemReference {
        reference = "Observation/TumorstatusMetas-" + context.source[progress().id()]
      }
    }
  }

  // Reference Vitalstatus
  finding {
    itemReference {
      reference = "Observation/Vitalstatus-" + context.source[progress().patientContainer().id()]
    }
  }

  //Reference Fernmetastasen
  context.source[progress().metastases()]?.each { final def m ->
    finding {
      itemReference {
        reference = "Observation/Metastasis-" + m[ID]
      }
    }
  }

  context.source[progress().histologies()]?.each { final def h ->
    finding {
      itemReference {
        reference = "Observation/Histology-" + h[ID]
      }
    }
  }

  context.source[progress().tumour().tnms()]?.each { final def tnm ->
    finding {
      itemReference {
        reference = "Observation/Tnm-" + tnm[ID]
      }
    }
  }

  // duplicated references, because of problem reference to condition and reference condition -> sample
  context.source[progress().tumour().centraxxDiagnosis().samples()]?.each { final def s ->
    finding {
      itemReference {
        reference = "Specimen/" + s[ID]
      }
    }
  }
}

/**
 * removes milli seconds and time zone.
 * @param dateTimeString the date time string
 * @return the result might be something like "1989-01-15T00:00:00"
 */
static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static boolean hasRelevantCode(final String icdCode) {
  return icdCode != null && (icdCode.toUpperCase().startsWith('C') || icdCode.toUpperCase().startsWith('D'))
}
