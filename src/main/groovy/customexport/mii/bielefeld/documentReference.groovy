package customexport.mii.bielefeld


import de.kairos.fhir.centraxx.metamodel.enums.DocumentMappingType
import org.hl7.fhir.r4.model.CodeType
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Extension

import static de.kairos.fhir.centraxx.metamodel.RootEntities.documentMapping

documentReference {

    if ((context.source[documentMapping().mappingType()] as DocumentMappingType) != DocumentMappingType.PATIENTMAPPING) {
        return
    }

    meta {
        profile "https://www.medizininformatik-initiative.de/fhir/ext/modul-dokument/StructureDefinition/mii-pr-dokument-dokument"
    }

    extension {
        url = "https://www.medizininformatik-initiative.de/fhir/ext/modul-dokument/StructureDefinition/mii-ex-dokument-nlp-processing-status"
        valueCodeableConcept {
            coding {
                system = "https://www.medizininformatik-initiative.de/fhir/ext/modul-dokument/CodeSystem/mii-cs-dokument-nlp-processing-status"
                code = "unprocessed"
            }
        }
    }
    id = context.source[documentMapping().id()]

    status = Enumerations.DocumentReferenceStatus.CURRENT

    final String hdrpDocName = context.source[documentMapping().finding().name()] as String
    final String categoryCode = context.source[documentMapping().category().code()] as String
    final String docuemtId = context.source[documentMapping().finding().documentId()] as String

    final String[] split = categoryCode.split("_")

    println("documentMapping().finding().name(): " + hdrpDocName)
    println("context.source[documentMapping().category().code()]: " + categoryCode)


    if (split.length > 0 && ["KDL", "LNC", "SCT", "XDS"].contains(split[0])) {
        final String docType = split[0]
        final String docName = hdrpDocName.substring(docType.length() + 1)
        final String docSystem = getSystem(docType)

        println("docName: " + docName)

        type {
            coding {
                system = docSystem
                code = categoryCode
            }
        }
    }

    identifier {
        use = "official"
        system = "https://evkb.de/sid/imedone-document-id"
        value = docuemtId
    }


    subject {
        reference = "Patient/" + context.source[documentMapping().relatedPatient().id()]
    }

    // Attachment ohne URL erzeugen
    content {
        attachment {
            contentType = "application/pdf"
        }
    }

    if (context.source[documentMapping().episode()] != null) {
        contextComponent {
            encounter {
                reference = "Encounter/" + context.source[documentMapping().episode().id()]
            }
        }
    }

    description = context.source[documentMapping().finding().description()]
}.with { resource ->

    resource.getContent().each { content ->
        content.getAttachment().getUrlElement().addExtension(
                new Extension(
                        "http://hl7.org/fhir/StructureDefinition/data-absent-reason",
                        new CodeType("masked")
                )
        )
    }

    // Letzter Ausdruck ist die DocumentReference
    resource
}


static String getSystem(final String docType) {
    switch (docType) {
        case "KDL" -> "http://dvmd.de/fhir/CodeSystem/kdl"
        case "LNC" -> "http://loinc.org"
        case "SCT" -> "http://snomed.info/sct"
        case "XDS" -> "http://ihe-d.de/CodeSystems/IHEXDStypeCode"
    }
}
