package projects.nhs

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.tnm

/**
 * Represented by a CXX TNM
 * @author Mike Wähnert
 * @since CXX.v.3.18.1.21, CXX.v.3.18.2, kairos-fhir-dsl-1.13.0
 */
observation {

  id = "Observation/Tnm-" + context.source[tnm().id()]

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      code = "TNM"
    }
  }

  subject {
    reference = "Patient/" + context.source[tnm().patientContainer().id()]
  }

  if (context.source[tnm().episode()] && !["SACT", "COSD"].contains(context.source[tnm().episode().entitySource()])) {
    encounter {
      reference = "Encounter/" + context.source[tnm().episode().id()]
    }
  }

  if (context.source[tnm().date()]) {
    effectiveDateTime {
      date = context.source[tnm().date()]
      precision = TemporalPrecisionEnum.DAY.toString()
    }
  }

  final StringBuilder sb = new StringBuilder();
  if (context.source[tnm().ySymbol()]) {
    sb.append(context.source[tnm().ySymbol()])
  }

  if (context.source[tnm().recidivClassification()]) { // rSymbol
    sb.append(context.source[tnm().recidivClassification()])
  }

  if (context.source[tnm().praefixTDict()]) {
    sb.append(context.source[tnm().praefixTDict().code()])
  }

  if (context.source[tnm().t()]) {
    sb.append("T")
    sb.append(context.source[tnm().t()])
  }

  if (context.source[tnm().certaintyT()]) {
    sb.append("C")
    sb.append(context.source[tnm().certaintyT()])
  }

  if (context.source[tnm().multiple()]) {
    sb.append("(")
    sb.append(context.source[tnm().multiple()])
    sb.append(")")
  }

  sb.append(" ")

  if (context.source[tnm().praefixNDict()]) {
    sb.append(context.source[tnm().praefixNDict().code()])
  }

  if (context.source[tnm().n()]) {
    sb.append("N")
    sb.append(context.source[tnm().n()])
  }

  if (sb.append(context.source[tnm().certaintyN()])) {
    sb.append("C")
    sb.append(context.source[tnm().certaintyN()])
  }
  sb.append(" ")

  if (context.source[tnm().praefixMDict()]) {
    sb.append(context.source[tnm().praefixMDict().code()])
  }

  if (context.source[tnm().m()]) {
    sb.append("M")
    sb.append(context.source[tnm().m()])
  }
  if (context.source[tnm().certaintyM()]) {
    sb.append("C")
    sb.append(context.source[tnm().certaintyM()])
  }

  sb.append(" ")

  if (context.source[tnm().l()]) {
    sb.append("L")
    sb.append(context.source[tnm().l()])
  }
  if (context.source[tnm().v()]) {
    sb.append("V")
    sb.append(context.source[tnm().v()])
  }
  if (context.source[tnm().pni()]) {
    sb.append("Pn")
    sb.append(context.source[tnm().pni()])
  }
  if (context.source[tnm().s()]) {
    sb.append("S")
    sb.append(context.source[tnm().s()])
  }
  if (context.source[tnm().grading()]) {
    sb.append("G")
    sb.append(context.source[tnm().grading()])
  }

  if (context.source[tnm().stadium()]) {
    valueCodeableConcept {
      coding {
        system = "https://fhir.centraxx.de/system/tnm/simple"
        code = sb.toString()
        version = context.source[tnm().version()]
      }
    }
  }

  if (context.source[tnm().tumour()]) {
    focus {
      reference = "Condition/" + context.source[tnm().tumour().centraxxDiagnosis().id()]
    }
  }
}
