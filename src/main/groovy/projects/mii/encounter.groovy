import de.kairos.fhir.centraxx.metamodel.*
import static de.kairos.fhir.centraxx.metamodel.RootEntities.episode

/**
 * represented bz CCX Episode
 * @author Jonas Küttner
 * @since v.1.8.0, CXX.v.3.18.1
 */
//TODO: hospitalization, location

encounter {
  id = "Encounter/" + context.source[episode().id()]

  meta {
    profile "https://www.medizininformatik-initiative.de/fhir/core/modul-fall/StructureDefinition/KontaktGesundheitseinrichtung"
  }

  context.source[episode().idContainer()]?.each { final idc ->
    identifier {
      value = idc[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.ID)
      system = "urn:centraxx"
      type {
        coding {
          system = "urn:centraxx"
          code = idc[]
        }
      }
    }
  }

  def typeOfStay = context.source[episode().stayType()]?.getAt(StayType.CODE)
  if (typeOfStay) {
    class_ {
      def codeDisplay = getTypeOfStayCode(typeOfStay)
      system = "http://terminology.hl7.org/CodeSystem/v2-0004"
      code = codeDisplay[0]
      display = codeDisplay[1]
    }
  }

  def medDepartment = context.source[episode().medDepartment()]
  if (medDepartment) {
    serviceType {
      coding {
        system = "https://www.medizininformatik-initiative.de/fhir/core/modul-fall/CodeSystem/Fachabteilungsschluessel"
        code = medDepartment[MedDepartment.CODE]
        //TODO: Mapping of Fachabteilungsschlüssel
      }
    }
  }

  subject {
    reference = "Patient/" + context.source[episode().patientContainer().id()]
  }

  period {
    start {
      date = normalizeDate(context.source[episode().validFrom()] as String)
    }
    end {
      date = normalizeDate(context.source[episode().validUntil()] as String)
    }
  }

  if (context.source[episode().habitation()]) {
    serviceProvider {
      reference = "Organization/" + context.source[episode().habitation().id()]
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


}


static List getTypeOfStayCode(Object cxxTypeOfStay) {
  switch (cxxTypeOfStay) {
    case "AH-001":
      return ["O", "Outpatient"]
    case "AH-002":
      return ["I", "Inpatient"]
    default:
      return ["U", "Unknown"]
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