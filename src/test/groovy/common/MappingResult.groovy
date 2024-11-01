package common

import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.Resource

class MappingResult<E extends Resource> {
  final Context context
  final E resource

  MappingResult(final context, final resource){
    this.context = context
    this.resource = resource
  }
}
