package projects.databox

import de.kairos.fhir.centraxx.metamodel.Diagnosis
import de.kairos.fhir.centraxx.metamodel.EpisodeIdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.MedDepartment
import de.kairos.fhir.centraxx.metamodel.StayType
import org.hl7.fhir.r4.model.Encounter

import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode

/**
 * Represented by CXX Episode
 * @author Marvin Schmidtke
 * hints:
 * identifier visit number: the MII profile requires a "Aufnahmenummer" as identifier. Therefore a specific Episode ID
 * must be defined in CentraXX (with code "VISIT_NUMBER" in this example)
 * serviceType: The fhir-profile requires a "Fachabteilungsschluessel" as defined in the following value set
 * https://www.medizininformatik-initiative.de/fhir/core/modul-fall/CodeSystem/Fachabteilungsschluessel
 * For consistency, the med departments codes defined in CXX must match those defined in this value set.
 * type.kontaktebene, hospitalization are not depicted in CXX
 */

encounter {
  id = "Encounter/" + context.source[episode().id()]

  //Created Episode Id in CXX called visit number
  final def visit_number = context.source[episode().idContainer()]?.find { final idc ->
    "EPISODEID" == idc[EpisodeIdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE]
  }

  if (visit_number) {
    identifier {
      type {
        coding {
          system = "urn:centraxx"
          code = "episodeId"
        }
      }
      value = visit_number[EpisodeIdContainer.PSN]
      system = "urn:centraxx"
    }
  }

  final def typeOfStay = context.source[episode().stayType()]?.getAt(StayType.CODE)
  if (typeOfStay) {
    class_ {
      final def codeDisplay = getTypeOfStayCode(typeOfStay)
      system = "http://terminology.hl7.org/CodeSystem/v2-0004"
      code = codeDisplay[0]
      display = codeDisplay[1]
    }
  } else {
    class_ {
      system = "http://terminology.hl7.org/CodeSystem/v2-0004"
      code = "U"
      display = "Unknown"
    }
  }
  final def medDepartment = context.source[episode().medDepartment()]
  if (medDepartment) {
    serviceType {
      coding {
        system = "https://www.medizininformatik-initiative.de/fhir/core/modul-fall/CodeSystem/Fachabteilungsschluessel"
        code = medDepartment[MedDepartment.CODE]
      }
    }
  }

  subject {
    reference = "Patient/" + context.source[episode().patientContainer().id()]
  }

  period {
    start {
      date = context.source[episode().validFrom()] as String
    }
    end {
      date = context.source[episode().validUntil()] as String
    }
  }

  if (context.source[episode().habitation()]) {
    serviceProvider {
      identifier {
        system = "urn:centraxx"
        value = context.source[episode().habitation().code()]
      }
    }
  }

  context.source[episode().diagnoses()]?.each { final def d ->
    diagnosis {
      condition {
        reference = "Condition/" + d[Diagnosis.ID]
        identifier {
          system = "urn:centraxx"
          value = d[Diagnosis.DIAGNOSIS_ID]
        }
      }
    }
  }

  status = Encounter.EncounterStatus.UNKNOWN

}


static List getTypeOfStayCode(final Object cxxTypeOfStay) {
  switch (cxxTypeOfStay) {
    case "AH-001":
      return ["O", "Outpatient"]
    case "AH-002":
      return ["I", "Inpatient"]
    default:
      return ["U", "Unknown"]
  }
}