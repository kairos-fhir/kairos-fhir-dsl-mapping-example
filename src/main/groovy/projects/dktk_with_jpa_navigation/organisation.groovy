package projects.dktk_with_jpa_navigation


import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

/**
 * Represented by a CXX OrganizationUnit
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.4
 */
organization {
  id = "Organization/" + context.source[organizationUnit().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Organization-organisation"
  }

  identifier {
    system = "urn:centraxx:org"
    value = context.source[organizationUnit().code()]
  }

  active = true

  type {
    coding {
      system = "https://www.hl7.org/fhir/valueset-organization-type.html"
      code = "dept"
      display = "Hospital Department"
    }
  }

  name = context.source[organizationUnit().nameMultilingualEntries()]?.find { it[LANG] == "en" }?.getAt(VALUE)

}
