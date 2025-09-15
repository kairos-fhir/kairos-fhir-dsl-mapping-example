package customexport.patientfinder.hull

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit
import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * Represented by a HDRP OrganizationUnit
 * @author Mike Wähnert
 * @since v.1.51.0, HDRP.v.2025.1.7
 */
organization {

  if (context.source[organizationUnit().code()] == "FAKE"){
    return
  }

  id = "Organization/" + context.source[organizationUnit().id()]

  identifier {
    system = FhirUrls.System.ORGANIZATION_UNIT
    value = context.source[organizationUnit().code()]
  }

  active = true

  final String orgUnitName = context.source[organizationUnit().multilinguals()]
      ?.find { final def me -> me[Multilingual.LANGUAGE] == "en" && me[Multilingual.SHORT_NAME] != null }
      ?.getAt(Multilingual.SHORT_NAME) as String

  name = cleanName(orgUnitName)

}

static String cleanName(final String name) {
  if (isBlank(name)) {
    return null
  }
  final String prefix = "specialty:"

  if (name.startsWith(prefix)) {
    return name.substring(prefix.length()).trim()
  }
  return name
}


