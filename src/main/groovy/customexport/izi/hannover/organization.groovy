package customexport.izi.hannover

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

/**
 * Represented by a HDRP OrganizationUnit
 * @author Mike Wähnert
 * @since v.1.52.0
 * @since HDRP.v.2025.3.0
 *
 */
organization {

  id = "Organization/" + context.source[organizationUnit().id()]

  identifier {
    system = FhirUrls.System.ORGANIZATION_UNIT
    value = context.source[organizationUnit().code()]
  }

  active = true

  name = context.source[organizationUnit().multilinguals()].find { final def ml ->
    ml[Multilingual.LANGUAGE] == "de" && ml[Multilingual.SHORT_NAME] != null
  }?.getAt(Multilingual.SHORT_NAME) as String

}
