package projects.uscore

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.BinaryFilePart
import de.kairos.fhir.centraxx.metamodel.DocumentCategory
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.MultilingualEntry
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.codesystems.DataAbsentReason

import static de.kairos.fhir.centraxx.metamodel.BinaryFile.CONTENT_PARTS
import static de.kairos.fhir.centraxx.metamodel.BinaryFile.CONTENT_TYPE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.documentMapping
/**
 * Represents a CXX DocumentMapping.
 * Specified by https://www.hl7.org/fhir/us/core/StructureDefinition-us-core-documentreference.html
 *
 * @author Mike Wähnert, Jonas Küttner
 * @since v.1.14.0, CXX.v.2022.1.0
 */

final def lang = "de"
documentReference {

  id = "DocumentReference/" + context.source[documentMapping().id()]

  meta {
    profile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-documentreference")
  }
  identifier {
    system = FhirUrls.System.Document.DOCUMENT_ID
    value = context.source[documentMapping().finding().documentId()]
  }

  status = Enumerations.DocumentReferenceStatus.CURRENT

  final def docCategory = context.source[documentMapping().category()]

  type {
    if (docCategory != null) {
      coding {
        system = FhirUrls.System.Document.Category.BASE_URL
        code = docCategory[DocumentCategory.CODE]
        display = docCategory[DocumentCategory.NAME_MULTILINGUAL_ENTRIES].find { final me ->
          me[MultilingualEntry.LANG] == lang
        }?.getAt(MultilingualEntry.VALUE)
      }
    } else {
      coding {
        system = DataAbsentReason.UNKNOWN.system
        code = DataAbsentReason.UNKNOWN
      }
    }
  }

  category {
    coding {
      system = FhirUrls.System.Document.MappingType.BASE_URL
      code = context.source[documentMapping().mappingType()]
    }
  }

  final def relPatient = context.source[documentMapping().relatedPatient()]
  if (relPatient) {
    subject {
      reference = "Patient/" + relPatient[PatientContainer.ID]
    }
  }

  date = context.source[documentMapping().finding().creationDate()]

  final def binaryFile = context.source[documentMapping().finding().binaryFile()]
  final String link = context.source[documentMapping().finding().url()]
  content {
    if (binaryFile) {
      attachment {
        contentType = binaryFile[CONTENT_TYPE]
        data = (binaryFile[CONTENT_PARTS] as List)[BinaryFilePart.CONTENT]
      }
    }
    else if (link) {
      attachment {
        contentType = "unknown"
        url = link
      }
    }
  }

  final def episode = context.source[documentMapping().episode()]
  if (episode != null) {
    contextComponent {
      encounter {
        reference = "Encounter/" + episode[Episode.ID]
      }
    }
  }
}
