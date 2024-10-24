package projects.cxx.ctcue

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.enums.CrfFieldType
import de.kairos.fhir.centraxx.metamodel.enums.CrfSectionType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.codesystems.PublicationStatus

questionnaire {

  id = "Questionnaire/1"

  meta {
    tag {
      system = FhirUrls.System.CXX_ENTITY
      code = "LaborMethod"
    }
  }

  identifier {
    system = FhirUrls.System.LaborMethod.BASE_URL
    value = "Thomas"
  }

  status = PublicationStatus.UNKNOWN

  // using first section only
  item {
    setLinkId("1")
    code {
      system = "system/code"
      value = "code1"
    }

    item {

      extension {
        url = FhirUrls.Extension.CrfTemplate.Section.TYPE
        valueCoding {
          system = FhirUrls.System.CrfTemplate.Section.Type.BASE_URL
          setCode(CrfSectionType.PLAIN.toString())
        }
      }

      setType(Questionnaire.QuestionnaireItemType.GROUP)


      setLinkId("5")

      ["a", "b", "c"].each { final def s ->
        item {

          extension {
            url = FhirUrls.Extension.CrfTemplate.Section.Field.CRFFIELDTYPE
            valueString = CrfFieldType.LABORVALUE.toString()
          }
          extension {
            url = FhirUrls.Extension.LaborValue.LABORVALUETYPE
            valueString = LaborValueDType.STRING.toString()
          }

          code {
            system = FhirUrls.System.LaborValue.BASE_URL
            value = "a"
          }

        }
      }
    }
  }
}

