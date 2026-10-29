package customexport.mii.bielefeld

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.enums.DocumentMappingType
import org.hl7.fhir.r4.model.Enumerations

import static de.kairos.fhir.centraxx.metamodel.RootEntities.documentMapping

documentReference {

  if ((context.source[documentMapping().mappingType()] as DocumentMappingType) != DocumentMappingType.PATIENTMAPPING) {
    return
  }

  id = context.source[documentMapping().id()]


  if (context.source[documentMapping().finding().documentId()] != null) {
    identifier {
      system = FhirUrls.System.Document.DOCUMENT_ID
      value = context.source[documentMapping().finding().documentId()]
    }
  }

  status = Enumerations.DocumentReferenceStatus.CURRENT

  final String hdrpDocName = context.source[documentMapping().finding().name()] as String
  final String[] split = hdrpDocName.split("_")


  if (split.length > 0 && ["KDL", "LNC", "SCT", "XDS"].contains(split[0])) {
    final String docType = split[0]
    final String docName = hdrpDocName.substring(docType.length() + 1)

    identifier {
      system = FhirUrls.System.Document.DOCUMENT_NAME
      value = docName
    }

    final String docSystem = getSystem(docType);

    type {
      coding {
        system = docSystem
        code = context.source[documentMapping().category().code()]
      }
    }
  } else {
    identifier {
      system = FhirUrls.System.Document.DOCUMENT_NAME
      value = hdrpDocName
    }
  }

  subject {
    reference = "Patient/" + context.source[documentMapping().relatedPatient().id()]
  }

  content {
    attachment {
      url = "Internal_URL_redacted"
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

}

static String getSystem(final String docType) {
  switch (docType) {
    case "KDL" -> "http://dvmd.de/fhir/CodeSystem/kdl"
    case "LNC" -> "http://loinc.org"
    case "SCT" -> "http://snomed.info/sct"
    case "XDS" -> "http://ihe-d.de/CodeSystems/IHEXDStypeCode"
  }
}
