package projects.izi.hannover

import de.kairos.centraxx.fhir.r4.utils.FhirUrls

import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

/**
 * Represented by a CXX OrganizationUnit
 * @author Mike Wähnert
 * @since v.1.13.0, CXX.v.2022.1.0 exportable, needs at leased
 * @since CXX.v.3.18.3.16, CXX.v.3.18.4.0, CXX.v.2023.1.0 importable
 *
 */
organization {

  id = "Organization/" + context.source[organizationUnit().id()]

  identifier {
    system = FhirUrls.System.ORGANIZATION_UNIT
    value = context.source[organizationUnit().code()]
  }

  active = true

  name = context.source[organizationUnit().nameMultilingualEntries()].find { final def me ->
    me[LANG] == "de"
  }?.getAt(VALUE) as String

}
