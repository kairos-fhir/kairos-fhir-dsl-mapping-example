package projects.dktk.v1

/**
 * Represented by a CXX OrganizationUnit
 * Specified by https://simplifier.net/oncology/organisation
 * @author Mike Wähnert
 * @since CXX.v.3.17.0.4
 */
organization {
  id = "Organization/" + context.source["id"]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Organization-Organisation"
  }

  identifier {
    system = "urn:centraxx:org"
    value = context.source["code"]
  }

  active = true

  type {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/organization-type"
      code = "dept"
      display = "Hospital Department"
    }
  }

  name = context.source["nameMultilingualEntries2"]?.find { it["lang"] == "en" }?.getAt("value")

}
