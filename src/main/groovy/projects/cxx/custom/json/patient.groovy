package projects.cxx.custom.json

import de.kairos.fhir.centraxx.metamodel.IdContainer
import de.kairos.fhir.centraxx.metamodel.IdContainerType
import de.kairos.fhir.centraxx.metamodel.OrganisationUnit
import groovy.json.JsonOutput

import static de.kairos.fhir.centraxx.metamodel.RootEntities.patient

/**
 * Represented by a CXX PatientMasterDataAnonymous
 * This experimental script should export a custom JSON structure without using HAPI FHIR
 * @author Mike Wähnert
 */
patient {

  final CustomJsonStructure jsonObject = new CustomJsonStructure()
  jsonObject.oid = context.source[patient().patientContainer().id()]
  jsonObject.birthdate = context.source[patient().birthdate().date()]
  jsonObject.gender = context.source[patient().genderType()]
  jsonObject.maritalStatus = context.source[patient().maritalStatus().code()]
  jsonObject.bloodGroup = context.source[patient().bloodGroup().code()]

  context.source[patient().patientContainer().idContainer()].each { final idc ->
    jsonObject.psns.put(idc[IdContainer.ID_CONTAINER_TYPE]?.getAt(IdContainerType.CODE) as String, idc[IdContainer.PSN] as String)
  }

  context.source[patient().patientContainer().organisationUnits()].each { final orgUnit ->
    jsonObject.organization.add(orgUnit[OrganisationUnit.CODE] as String)
  }

  final File file = new File('C:/centraxx-home/fhir-custom-export/experiment', 'Patient-' + jsonObject.oid + '.json')
  file.write(JsonOutput.prettyPrint(JsonOutput.toJson(jsonObject)), "UTF-8")
}

class CustomJsonStructure {
  String oid
  Map<String, String> psns = new HashMap<>()
  List<String> organization = []
  String gender
  String birthdate
  String maritalStatus
  String bloodGroup
}
