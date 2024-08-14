package projects.patientfinder

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.Episode
import de.kairos.fhir.centraxx.metamodel.Tnm
import de.kairos.fhir.dsl.r4.execution.Fhir4Source
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.AbstractIdContainer.PSN
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

  if (!isFakeEpisode(context.source[tnm().episode()])) {
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

  final String tnmSystem = createTnmSystem(context.source)
  final String tnmString = createTnmString(context.source)

  valueCodeableConcept {
    coding {
      system = tnmSystem
      code = tnmString
      version = context.source[tnm().version()]
    }

    if (context.source[tnm().stadium()]) {
      coding {
        system = "https://fhir.centraxx.de/system/tnm/stadium"
        code = (context.source[tnm().stadium()] as String).trim()
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

static boolean isFakeEpisode(final def episode) {
  if (episode == null) {
    return true
  }

  if (["SACT", "COSD"].contains(episode[Episode.ENTITY_SOURCE])) {
    return true
  }

  final def fakeId = episode[Episode.ID_CONTAINER]?.find { (it[PSN] as String).toUpperCase().startsWith("FAKE") }
  return fakeId != null
}

static String createTnmSystem(final Fhir4Source source) {
  if (["SACT", "COSD"].contains(source[Tnm.ENTITY_SOURCE])) {
    if ("c".equalsIgnoreCase(source[tnm().praefixTDict().code()] as String)) {
      return "https://fhir.centraxx.de/system/tnm/finalpretreatment"
    } else if ("p".equalsIgnoreCase(source[tnm().praefixTDict().code()] as String)) {
      return "https://fhir.centraxx.de/system/tnm/integrated"
    }
    return "https://fhir.centraxx.de/system/tnm/simple"
  }
  return {
    return "https://fhir.centraxx.de/system/tnm/simple"
  }
}

static String createTnmString(final Fhir4Source source) {
  if (["SACT", "COSD"].contains(source[Tnm.ENTITY_SOURCE])) {
    return createCosdTnmString(source)
  }
  return {
    return createCombinedTnmString(source)
  }
}

/**
 * Creates a combined TNM string with all TNM parts
 */
static String createCombinedTnmString(final Fhir4Source source) {

  final StringBuilder sb = new StringBuilder()
  if (source[tnm().ySymbol()]) {
    sb.append(source[tnm().ySymbol()])
  }

  if (source[tnm().recidivClassification()]) { // rSymbol
    sb.append(source[tnm().recidivClassification()])
  }

  if (source[tnm().praefixTDict()]) {
    sb.append(source[tnm().praefixTDict().code()])
  }

  if (source[tnm().t()]) {
    sb.append("T")
    sb.append(source[tnm().t()])
  }

  if (source[tnm().certaintyT()]) {
    sb.append("C")
    sb.append(source[tnm().certaintyT()])
  }

  if (source[tnm().multiple()]) {
    sb.append("(")
    sb.append(source[tnm().multiple()])
    sb.append(")")
  }

  sb.append(" ")

  if (source[tnm().praefixNDict()]) {
    sb.append(source[tnm().praefixNDict().code()])
  }

  if (source[tnm().n()]) {
    sb.append("N")
    sb.append(source[tnm().n()])
  }

  if (source[tnm().certaintyN()]) {
    sb.append("C")
    sb.append(source[tnm().certaintyN()])
  }
  sb.append(" ")

  if (source[tnm().praefixMDict()]) {
    sb.append(source[tnm().praefixMDict().code()])
  }

  if (source[tnm().m()]) {
    sb.append("M")
    sb.append(source[tnm().m()])
  }
  if (source[tnm().certaintyM()]) {
    sb.append("C")
    sb.append(source[tnm().certaintyM()])
  }

  sb.append(" ")

  if (source[tnm().l()]) {
    sb.append("L")
    sb.append(source[tnm().l()])
  }
  if (source[tnm().v()]) {
    sb.append("V")
    sb.append(source[tnm().v()])
  }
  if (source[tnm().pni()]) {
    sb.append("Pn")
    sb.append(source[tnm().pni()])
  }
  if (source[tnm().s()]) {
    sb.append("S")
    sb.append(source[tnm().s()])
  }
  if (source[tnm().grading()]) {
    sb.append("G")
    sb.append(source[tnm().grading()])
  }

  return sb.toString().trim()
}

/**
 * Creates a combined TNM string without prefixes
 */
static String createCosdTnmString(final Fhir4Source source) {

  final StringBuilder sb = new StringBuilder()
  if (source[tnm().ySymbol()]) {
    sb.append(source[tnm().ySymbol()])
  }

  if (source[tnm().recidivClassification()]) { // rSymbol
    sb.append(source[tnm().recidivClassification()])
  }

  if (source[tnm().t()]) {
    sb.append("T")
    sb.append(source[tnm().t()])
  }

  if (source[tnm().certaintyT()]) {
    sb.append("C")
    sb.append(source[tnm().certaintyT()])
  }

  if (source[tnm().multiple()]) {
    sb.append("(")
    sb.append(source[tnm().multiple()])
    sb.append(")")
  }

  sb.append(" ")

  if (source[tnm().n()]) {
    sb.append("N")
    sb.append(source[tnm().n()])
  }

  if (source[tnm().certaintyN()]) {
    sb.append("C")
    sb.append(source[tnm().certaintyN()])
  }
  sb.append(" ")

  if (source[tnm().m()]) {
    sb.append("M")
    sb.append(source[tnm().m()])
  }
  if (source[tnm().certaintyM()]) {
    sb.append("C")
    sb.append(source[tnm().certaintyM()])
  }

  sb.append(" ")

  if (source[tnm().l()]) {
    sb.append("L")
    sb.append(source[tnm().l()])
  }
  if (source[tnm().v()]) {
    sb.append("V")
    sb.append(source[tnm().v()])
  }
  if (source[tnm().pni()]) {
    sb.append("Pn")
    sb.append(source[tnm().pni()])
  }
  if (source[tnm().s()]) {
    sb.append("S")
    sb.append(source[tnm().s()])
  }
  if (source[tnm().grading()]) {
    sb.append("G")
    sb.append(source[tnm().grading()])
  }

  return sb.toString().trim()
}

