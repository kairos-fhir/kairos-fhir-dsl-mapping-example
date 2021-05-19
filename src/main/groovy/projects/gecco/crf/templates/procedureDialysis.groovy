package projects.gecco.crf

import de.kairos.fhir.centraxx.metamodel.enums.Localization
import org.hl7.fhir.r4.model.Procedure

import static de.kairos.fhir.centraxx.metamodel.RootEntities.medProcedure

/**
 * Represented by a CXX MedProcedure
 * Specified by https://simplifier.net/forschungsnetzcovid-19/dialysis
 * @author Mike Wähnert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 * Hints:
 *  Only OPS sub codes of 8-85 allowed as specified by https://simplifier.net/forschungsnetzcovid-19/dialysis-procedures-ops
 *  TODO: work in progress
 */
procedure {

  String opsCode = context.source[medProcedure().opsEntry().code()]
  if (opsCode == null || !opsCode.startsWith("8-85")) {
    return // no export
  }

  id = "Procedure/MedProcedure-" + context.source[medProcedure().id()]

  meta {
    profile "https://www.netzwerk-universitaetsmedizin.de/fhir/StructureDefinition/procedureDialysis"
  }

  status = Procedure.ProcedureStatus.UNKNOWN

  subject {
    reference = "Patient/" + context.source[medProcedure().patientContainer().id()]
  }

  encounter {
    reference = "Encounter/" + context.source[medProcedure().episode().id()]
  }

  code {
    coding {
      system = "http://fhir.de/CodeSystem/dimdi/ops"
      version = context.source[medProcedure().opsEntry().catalogue().catalogueVersion()]
      code = opsCode

      Localization localization = context.source[medProcedure().localisation()] as Localization
      if (localization != null) {
        extension {
          url = "http://fhir.de/StructureDefinition/seitenlokalisation"
          valueCoding {
            system = "https://fhir.kbv.de/CodeSystem/KBV_CS_SFHIR_ICD_SEITENLOKALISATION"
            code = mapLocalisation(localization)
          }
        }
      }
    }
  }
}

static String mapLocalisation(Localization cxxLocalization) {
  switch (cxxLocalization) {
    case Localization.LEFT: return "L"
    case Localization.RIGHT: return "R"
    case Localization.BOTH: return "B"
    default: return null
  }
}
