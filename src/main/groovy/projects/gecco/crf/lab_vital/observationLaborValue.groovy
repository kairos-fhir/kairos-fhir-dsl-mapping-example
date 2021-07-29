package projects.gecco.crf

import ca.uhn.fhir.model.api.TemporalPrecisionEnum
import de.kairos.fhir.centraxx.metamodel.CrfItem
import de.kairos.fhir.centraxx.metamodel.CrfTemplateField
import de.kairos.fhir.centraxx.metamodel.FlexiStudy
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.StudyMember
import org.hl7.fhir.r4.model.Observation

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * Specified by https://simplifier.net/guide/GermanCoronaConsensusDataSet-ImplementationGuide/Laboratoryvalue
 * @author Lukas Reinert
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1
 *
 */
observation {

  //final def studyMember = context.source[laborMapping().relatedPatient().studyMembers()].find{
  //  it[StudyMember.STUDY][FlexiStudy.CODE] == "SARS-Cov-2"
  //}
  //if (!studyMember) {
  //  return //no export
  //}

  final def profileName = context.source[laborMapping().laborFinding().laborMethod().code()]
  if (profileName != "COV_GECOO_LABOR") {
    return //no export
  }
  final def numID = context.source[laborMapping().id()]
  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lFlV ->
    if (lFlV){
      final String labValCode = lFlV[LaborFindingLaborValue.LABOR_VALUE][LaborValue.CODE]
      if (isRelevant(labValCode)){
        id = "Observation/LaborValue-" + labValCode + "-" + numID

        meta {
          source = "https://fhir.centraxx.de"
          profile "https://www.medizininformatik-initiative.de/fhir/core/modul-labor/StructureDefinition/ObservationLab"
        }

        status = Observation.ObservationStatus.FINAL

        category {
          coding {
            system = "http://loinc.org"
            code = "26436-6"
          }
          coding {
            system = "http://terminology.hl7.org/CodeSystem/observation-category"
            code = "laboratory"
          }
        }


        code{
          coding{
            system = "http://loinc.org"
            code = mapCXXtoLoincCode(labValCode)
          }
        }

        //Iteration to remove "[]" from id in string
        context.source[laborMapping().relatedPatient().idContainer().id()].each { final id ->
          subject {
            reference = "Patient/Patient-" + id
          }
        }


        effectiveDateTime {
          date = normalizeDate(context.source[laborMapping().creationDate()] as String)
          precision = TemporalPrecisionEnum.DAY.toString()
        }

        lFlV[LaborFindingLaborValue.NUMERIC_VALUE]?.each { final numVal ->
          if (numVal){
            valueQuantity {
              value = numVal
              unit = mapCXXtoUnit(labValCode)
              system = "http://unitsofmeasure.org"
            }
          }
        }
      }

    }
  }
}

static Boolean isRelevant(final String labValCode){
  return ["COV_GECCO_CRP",
          "COV_GECCO_FERRITIN",
          "COV_GECCO_BILIRUBIN",
          "COV_GECCO_ANTITROMBIN",
          "COV_GECCO_DDIMER",
          "COV_GECCO_FIBRINOGEN",
          "COV_GECCO_GAMMA-GT",
          "COV_GECCO_GOT/AST",
          "COV_GECCO_HÄMOGLOBIN",
          "COV_GECCO_IL6",
          "COV_GECCO_INR",
          "COV_GECCO_KARDIALE_TROPONINE",
          "COV_GECCO_KREATININ",
          "COV_GECCO_LAKTAT",
          "COV_GECCO_LDH",
          "COV_GECCO_LEUKOZYTEN_ABS",
          "COV_GECCO_LYMPHOZYTEN_ABS",
          "COV_GECCO_THROMBOZYTEN_ABS",
          "COV_GECCO_NT-PRO-BP",
          "COV_GECCO_PTT",
          "COV_GECCO_THROMBOZYTEN"].contains(labValCode)
}

static String normalizeDate(final String dateTimeString) {
  return dateTimeString != null ? dateTimeString.substring(0, 19) : null
}

static String mapCXXtoLoincCode(final String lvCode) {
  switch (lvCode) {
    case("COV_GECCO_CRP"):
      return "76485-2"
    case("COV_GECCO_FERRITIN"):
        return "24373-3"
    case("COV_GECCO_BILIRUBIN"):
        return "42719-5"
    case("COV_GECCO_ANTITROMBIN"):
        return "3176-5"
    case("COV_GECCO_DDIMER"):
        return "48066-5"
    case("COV_GECCO_FIBRINOGEN"):
        return "16859-1"
    case("COV_GECCO_GAMMA-GT"):
        return "2324-2"
    case("COV_GECCO_GOT/AST"):
        return "1920-8"
    case("COV_GECCO_HÄMOGLOBIN"):
        return "718-7"
    case("COV_GECCO_IL6"):
        return "26881-3"
    case("COV_GECCO_INR"):
        return "34714-6"
    case("COV_GECCO_KARDIALE_TROPONINE"):
        return "42757-5"
    case("COV_GECCO_KREATININ"):
        return "38483-4"
    case("COV_GECCO_LAKTAT"):
        return "59032-3"
    case("COV_GECCO_LDH"):
        return "2532-0"
    case("COV_GECCO_LEUKOZYTEN_ABS"):
        return "26464-8"
    case("COV_GECCO_LYMPHOZYTEN_ABS"):
        return "26474-7"
    case("COV_GECCO_THROMBOZYTEN_ABS"):
        return "26515-7"
    case("COV_GECCO_NT-PRO-BP"):
        return "33762-6"
    case("COV_GECCO_PTT"):
        return "3173-2"
    case("COV_GECCO_THROMBOZYTEN"): //actually should be code "NEUTROPHILES"
        return "26499-4"
    default:
      return null
  }
}
static String mapCXXtoUnit(final String lvCode) {
  switch (lvCode) {
    case("COV_GECCO_CRP"):
      return "mg/L"
    case("COV_GECCO_FERRITIN"):
      return "ng/mL"
    case("COV_GECCO_BILIRUBIN"):
      return "ng/mL"
    case("COV_GECCO_ANTITROMBIN"):
      return "ng/mL"
    case("COV_GECCO_DDIMER"):
      return "ng/mL"
    case("COV_GECCO_FIBRINOGEN"):
      return "ng/mL"
    case("COV_GECCO_GAMMA-GT"):
      return "U/L"
    case("COV_GECCO_GOT/AST"):
      return "U/L"
    case("COV_GECCO_HÄMOGLOBIN"):
      return "g/dL"
    case("COV_GECCO_IL6"):
      return "ng/mL"
    case("COV_GECCO_INR"):
      return "INR"
    case("COV_GECCO_KARDIALE_TROPONINE"):
      return "ng/mL"
    case("COV_GECCO_KREATININ"):
      return "mg/dL"
    case("COV_GECCO_LAKTAT"):
      return "mg/dL"
    case("COV_GECCO_LDH"):
      return "U/L"
    case("COV_GECCO_LEUKOZYTEN_ABS"):
      return "10*3/µL"
    case("COV_GECCO_LYMPHOZYTEN_ABS"):
      return "10*3/µL"
    case("COV_GECCO_THROMBOZYTEN_ABS"):
      return "10*3/µL"
    case("COV_GECCO_NT-PRO-BP"):
      return "ng/mL"
    case("COV_GECCO_PTT"):
      return "s"
    case("COV_GECCO_THROMBOZYTEN"): //actually should be code "NEUTROPHILES"
      return "10*3/µL"
    default:
      return null
  }
}