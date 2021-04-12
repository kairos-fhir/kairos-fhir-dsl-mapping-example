package projects.gecco

import de.kairos.centraxx.common.types.GenderType
import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueCatalog
import de.kairos.fhir.centraxx.metamodel.LaborValueEnumeration
import de.kairos.fhir.centraxx.metamodel.LaborValueInteger
import de.kairos.fhir.centraxx.metamodel.RootEntities
import de.kairos.fhir.centraxx.metamodel.UsageEntry
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.CanonicalType
import org.hl7.fhir.r4.model.Enumerations
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.SimpleQuantity

import javax.persistence.criteria.Root

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping
import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since v.1.7.0, CXX.v.3.17.2
 */
observation {

  if (context.source[patient().genderType()]) {

    id = "SexAssignedAtBirth/" + context.source[patient().patientContainer().id()]

    meta {
      profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/sex-assigned-at-birth"
    }

    status = Observation.ObservationStatus.UNKNOWN


    category {
      coding {
        system = "http://terminology.hl7.org/CodeSystem/observation-category"
        code = "social-history"

      }
    }

    code {
      coding {
        system = "http://loinc.org"
        code = "876689-9"
      }
    }

    subject{
      reference = "Patient/" + context.source[patient().patientContainer().id()]
    }

    valueCodeableConcept {
      coding{
        system = "AdministrativeGender"
        code = mapGender(context.source[patient().genderType()] as GenderType)
      }
    }
  }
}



static Enumerations.AdministrativeGender mapGender(final GenderType genderType) {
  switch (genderType) {
    case null:
      return null
    case GenderType.MALE:
      return Enumerations.AdministrativeGender.MALE
    case GenderType.FEMALE:
      return Enumerations.AdministrativeGender.FEMALE
    case GenderType.UNKNOWN:
      return Enumerations.AdministrativeGender.UNKNOWN
    default:
      return Enumerations.AdministrativeGender.OTHER
  }
}