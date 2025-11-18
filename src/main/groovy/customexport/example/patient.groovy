package customexport.example

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import org.hl7.fhir.r4.model.HumanName

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

patient {
  id = "Patient/" + context.source[patient().patientContainer().id()]

  context.source[patient().patientContainer().idContainer()]
      .each { final def idc ->
        identifier {
          type {
            coding {
              url = FhirUrls.System.IdContainerType.BASE_URL
              value = idc[IdContainer.ID_CONTAINER_TYPE][IdContainerType.CODE]
            }
          }
          value = idc[IdContainer.PSN]
        }
      }

  humanName {
    use = HumanName.NameUse.OFFICIAL
    family = context.source[patient().lastName()]
    given(context.source[patient().firstName()] as String)

  }
}
