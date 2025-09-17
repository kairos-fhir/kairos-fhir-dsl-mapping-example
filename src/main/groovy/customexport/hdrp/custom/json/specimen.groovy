import com.fasterxml.jackson.databind.ObjectMapper

import static de.kairos.fhir.centraxx.metamodel.RootEntities.sample

specimen {

  final ObjectMapper mapper = new ObjectMapper()
  final String jsonString = mapper.writeValueAsString(context.source)

  final File file = new File('C:/Users/u1089116/centraxx-home/fhir-custom-export/sact', 'Specimen-' + context.source[sample().id()] + '.json')
  file.write(jsonString, "UTF-8")
}


