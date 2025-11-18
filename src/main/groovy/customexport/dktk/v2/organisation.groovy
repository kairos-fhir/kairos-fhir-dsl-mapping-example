package customexport.dktk.v2

import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

/**
 * Represented by a HDRP OrganizationUnit
 * Specified by https://simplifier.net/oncology/organisation
 * @author Mike Wähnert
 * @since v.1.52.0
 * @since HDRP.v.2025.3.0
 */
organization {
  id = "Organization/" + context.source[organizationUnit().id()]

  meta {
    profile "http://dktk.dkfz.de/fhir/StructureDefinition/onco-core-Organization-Organisation"
  }

  identifier {
    system = "urn:centraxx:org"
    value = context.source[organizationUnit().code()]
  }

  active = true

  type {
    coding {
      system = "http://terminology.hl7.org/CodeSystem/organization-type"
      code = "dept"
      display = "Hospital Department"
    }
  }

  name = context.source[organizationUnit().multilinguals()]?.find { final def ml ->
    ml[Multilingual.LANGUAGE] == "en" && ml[Multilingual.SHORT_NAME] != null
  }?.getAt(Multilingual.SHORT_NAME) as String

}
