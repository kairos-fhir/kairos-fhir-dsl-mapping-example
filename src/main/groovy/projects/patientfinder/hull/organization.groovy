package projects.patientfinder

import de.kairos.centraxx.fhir.r4.utils.FhirUrls

import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit
import static org.apache.commons.lang3.StringUtils.isBlank

/**
 * Represented by a CXX OrganizationUnit
 * @author Mike Wähnert
 * @since v.1.13.0, CXX.v.2022.1.0
 */
organization {

  id = "Organization/" + context.source[organizationUnit().id()]

  identifier {
    system = FhirUrls.System.ORGANIZATION_UNIT
    value = context.source[organizationUnit().code()]
  }

  active = true

  final String orgUnitName = context.source[organizationUnit().nameMultilingualEntries()]?.find { final def me -> me[LANG] == "en" }?.getAt(VALUE) as String

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


