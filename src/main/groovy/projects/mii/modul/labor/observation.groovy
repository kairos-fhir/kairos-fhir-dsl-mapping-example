package projects.mii.modul.labor

import de.kairos.fhir.centraxx.metamodel.LaborMapping
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.PatientContainer
import de.kairos.fhir.centraxx.metamodel.Unity
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborFindingLaborValue

/**
 * Represented by CXX LaborFindingLaborValue
 * specified by https://simplifier.net/medizininformatikinitiative-modullabor/observationlab
 * @author Mike Wähnert
 * @since v.1.8.0, CXX.v.3.18.1
 */
observation {
  id = "Observation/" + context.source[laborFindingLaborValue().id()]

  meta {
    profile("https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/ObservationLab")
  }

  def patient = context.source[laborFindingLaborValue().laborFinding().laborMappings()].find { it[LaborMapping.RELATED_PATIENT] != null }
  if (patient) {
    subject {
      reference = "Patient/" + patient[PatientContainer.ID]
    }
  }

  LaborValueDType dType = context.source[laborFindingLaborValue().laborValue().dType()] as LaborValueDType
  if (isNumeric(dType)) {
    valueQuantity {
      value = context.source[laborFindingLaborValue().numericValue()]
      unit = context.source[laborFindingLaborValue().laborValue()]?.getAt(LaborValueNumeric.UNIT)?.getAt(Unity.CODE) as String
    }
  }
}

private static boolean isNumeric(LaborValueDType dType) {
  [LaborValueDType.INTEGER, LaborValueDType.DECIMAL, LaborValueDType.SLIDER].contains(dType)
}

