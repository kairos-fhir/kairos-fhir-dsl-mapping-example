package projects.uscore

import de.kairos.centraxx.fhir.r4.utils.FhirUrls

import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

/**
 * Represented by a CXX OrganizationUnit
 * @author Mike Wähnert
 *
 * Hints
 * - example contains only CentraXX identifier. If your CentraXX contains NPI or CLIA, the identifier system for
 * NPI:http://hl7.org/fhir/sid/us-npi or CLIA:urn:oid:2.16.840.1.113883.4.7
 * - CXX OrgUnits have no telecom or address
 *
 * @since v.1.13.0, CXX.v.2022.1.0
 */
organization {

  id = "Organization/" + context.source[organizationUnit().id()]

  meta {
    profile "http://hl7.org/fhir/us/core/StructureDefinition/us-core-organization"
  }

  identifier {
    system = FhirUrls.System.ORGANIZATION_UNIT
    value = context.source[organizationUnit().code()]
  }

  active = true

  name = context.source[organizationUnit().nameMultilingualEntries()].find { final def me ->
    me[LANG] == "de"
  }?.getAt(VALUE) as String

}
