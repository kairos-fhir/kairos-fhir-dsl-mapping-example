package projects.mii_bielefeld.modul.diagnose

import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a CXX DIAGNOSIS
 * Specified by (stable release) https://www.medizininformatik-initiative.de/fhir/core/modul-diagnose/StructureDefinition/Diagnose
 * Working draft (future release) https://simplifier.net/guide/MedizininformatikInitiative-ModulDiagnosen-ImplementationGuide/Condition
 * @author Jonas Küttner, Mike Wähnert
 * @since v.1.8.0, CXX.v.3.18.1
 * hints:
 * bodysite might not depicted in CXX
 */

condition {

  if (!context.source[diagnosis().icdEntry()]) {
    return // only ICD 10 supported
  }

  id = "Condition/" + context.source[diagnosis().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-diagnose/StructureDefinition/Diagnose"
    versionId = "2.0.0"
  }

  if (context.source[diagnosis().icdEntry()]) {
    code {
      coding {
        system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
        version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()] as String
        code = context.source[diagnosis().icdEntry().code()] as String
      }
    }
  }

  // diagnosis date mandatory
  if (context.source[diagnosis().diagnosisDate()] && context.source[diagnosis().diagnosisDate().date()]) {
    onsetDateTime = context.source[diagnosis().diagnosisDate().date()]
  }

  // technical recording date
  recordedDate = context.source[diagnosis().creationDate()]

  // attestation date (optional
  if (context.source[diagnosis().attestationDate()] && context.source[diagnosis().attestationDate().date()]) {
    extension {
      url = "http://hl7.org/fhir/StructureDefinition/condition-assertedDate"
      valueDateTime = context.source[diagnosis().attestationDate().date()]
    }
  }


}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 10) : null // removes the time
}