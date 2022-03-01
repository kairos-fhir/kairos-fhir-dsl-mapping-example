package projects.uscore


import static de.kairos.fhir.centraxx.metamodel.RootEntities.diagnosis
import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX Diagnosis
 * Specified: https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-condition.html
 * @author Niklas Biedka
 * @since ?
 */

condition {

    id = "Condition/" + context.source[diagnosis().diagnosisId()]

    meta {
        profile "https://www.hl7.org/fhir/us/core/StructureDefinition/us-core-condition"
    }

    verificationStatus {
        coding {
            system = " http://terminology.hl7.org/CodeSystem/condition-ver-status"
            code = matchVerificationStatusToDiagnosisCertainty(context.source[diagnosis().diagnosisCertainty()] as String)
        }
    }

    clinicalStatus {
        coding {
            system = "http://terminology.hl7.org/CodeSystem/condition-clinical"
            code = "NI"
        }
    }

    category {
    }

    code {
        coding {
            system = "http://snomed.info/sct"
            code = matchResponseToSNOMED(context.source[diagnosis().diagnosisCode()] as String)
        }
    }

    subject {
        reference = "Patient/" + context.source[diagnosis().patientContainer().id()]
    }

}

static String matchVerificationStatusToDiagnosisCertainty(final String resp) {
    switch (resp) {
        case ("V"):
            return "unconfirmed"
        case ("G"):
            return "confirmed"
        case ("A"):
            return "refuted"
        case ("Z"):
            return "differential"
        default:
            null
    }
}

static String matchResponseToSNOMED(final String resp) {
    switch (resp) {
        case ("COV_BLUTHOCHDRUCK"):
            return "38341003"
        case ("COV_ZUSTAND_N_HERZINFARKT"):
            return "22298006"
        case ("COV_HERZRHYTHMUSSTOERUNGEN"):
            return "698247007"
        case ("COV_HERZINSUFFIZIENZ"):
            return "84114007"
        case ("COV_PAVK"):
            return "399957001"
        case ("COV_REVASKULARISATION"):
            return "81266008"
        case ("COV_KHK"):
            return "53741008"
        case ("COV_CARO"):
            return "64586002"
        case ("COV_UNBEKANNT"):
            return "49601007" //generic cardiovascular disease
        case ("COV_NEIN"):
            return "49601007" //generic cardiovascular disease
        default: null
    }
}