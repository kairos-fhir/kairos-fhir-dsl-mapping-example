package customexport.mii.bielefeld


import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis

/**
 * Represented by a HDRP DIAGNOSIS
 * @author Jonas Küttner, Mike Wähnert
 * @since v.1.40.0, HDRP.v.2024.4.2
 *
 */

condition {

    if (!context.source[diagnosis().icdEntry()] && !context.source[diagnosis().diagnosisCode()]) {
        return // only ICD 10 supported
    }


    id = "Condition/" + context.source[diagnosis().id()]

    meta {
        profile "https://www.medizininformatik-initiative.de/fhir/core/modul-diagnose/StructureDefinition/Diagnose"
        versionId = "2.0.0"
    }

    subject {
        reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
    }

    encounter {
        reference = "Encounter/" + context.source[diagnosis().episode().id()]
    }

    if (context.source[diagnosis().icdEntry()]) {
        code {
            coding {
                system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
                version = context.source[diagnosis().icdEntry().catalogue().catalogueVersion()] as String
                code = context.source[diagnosis().icdEntry().code()] as String
            }
            if(context.source[diagnosis().diagnosisText()]){
                text = context.source[diagnosis().diagnosisText()] as String
            }
        }
    }else if(context.source[diagnosis().diagnosisCode()]){
        code {
            coding {
                system = "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
                version = "2025"
                code = context.source[diagnosis().diagnosisCode()] as String
            }
            if(context.source[diagnosis().diagnosisText()]){
                text = context.source[diagnosis().diagnosisText()] as String
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