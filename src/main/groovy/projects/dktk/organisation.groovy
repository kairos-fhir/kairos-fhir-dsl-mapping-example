package projects.dktk

/**
 * Represented by a CXX OrganizationUnit
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.4
 */
organization {
  id = "Organization/" + context.source["id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Organization-organisation"
  }

  identifier {
    system = "urn:centraxx:org"
    value = context.source["code"]
  }

  active = true

  type {
    coding {
      system = "https://www.hl7.org/fhir/valueset-organization-type.html"
      code = "dept"
      display = "Hospital Department"
    }
  }

  name = context.source["nameMultilingualEntries2"]?.find { it["lang"] == "en" }?.getAt("value")

}
