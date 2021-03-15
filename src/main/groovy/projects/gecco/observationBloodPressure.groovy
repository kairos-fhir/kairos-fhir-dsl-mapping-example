package projects.gecco

import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Lukas Reinert
 * @since v.1.7.0, CXX.v.3.17.2
 */
observation {

  if (context.source[laborMapping().laborFinding().laborMethod().code()] != "BLOODPRESSURE_PROFILE") {
    return // no export
  }

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/blood-pressure"
  }

  status = Observation.ObservationStatus.UNKNOWN

  category{
    coding {
      system = "http://terminology.hl7.org/CodeSystem/observation-category"
      code = "vital-signs"
    }
  }

  code {
    coding {
      system = "http://loinc.org"
      code = "85354-9"
    }
    coding{
      system = "http://snomed.info/sct"
      code = "75367002"
    }
  }

  subject{
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()] as String
  }
  encounter {
    reference = "Episode/" + context.source[laborMapping().episode().id()]
  }


  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }



  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->

    if (lflv[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE] == "BLOODPRESSURE_SYS"){
      component {
        code {
          coding {
            system = "http://loinc.org"
            code = "8480-6"
          }
          coding{
            system = "http://snomed.info/sct"
            code = "271649006"
          }
        }

        if (isNumeric(lflv)) {
          valueQuantity {
            value = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
          }
        }
      }
    }
    else if (lflv[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE] == "BLOODPRESSURE_DIA"){
      component {
        code {
          coding {
            system = "http://loinc.org"
            code = "8462-4"
          }
          coding{
            system = "http://snomed.info/sct"
            code = "271650006"
          }
        }

        if (isNumeric(lflv)) {
          valueQuantity {
            value = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
          }
        }
      }
    }


  }
}


private static boolean isDTypeOf(final Object lflv, final List<LaborValueDType> types) {
  return types.contains(lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as LaborValueDType)
}

static boolean isNumeric(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.INTEGER, LaborValueDType.DECIMAL, LaborValueDType.SLIDER])
}
