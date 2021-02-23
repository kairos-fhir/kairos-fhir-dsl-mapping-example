package projects.dktk.v2


import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

/**
 * Represented by a CXX OrganizationUnit
 * @author Mike Wähnert
 * @since CXX.v.3.17.1.6, v.3.17.2
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
