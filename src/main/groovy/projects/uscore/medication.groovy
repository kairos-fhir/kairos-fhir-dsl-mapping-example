package projects.uscore

import static de.kairos.fhir.centraxx.metamodel.RootEntities.medication

/**
 * Represented by a CXX Medication
 * Specified: https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-medication.html
 * @author Niklas Biedka
 * @since v.1.13.0, CXX.v.2022.1.0
 */

medication {

    id = "Medication/" + context.source[medication().id()]

    meta {
        profile "https://www.hl7.org/fhir/us/core/StructureDefinition/us-core-medication"
    }

    code {
        coding {
            system = "https://vsac.nlm.nih.gov/valueset/2.16.840.1.113762.1.4.1010.4/expansion"
            code = context.source[medication().code()]
            // Due to the code-system not being dissolvable, the CentraXX code is used
        }
    }
}
