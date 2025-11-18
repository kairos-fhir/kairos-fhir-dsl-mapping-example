package customexport.uscore

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

/**
 * Represented by a HDRP OrganizationUnit
 * @author Mike Wähnert
 *
 * Hints
 * - example contains only CentraXX identifier. If your CentraXX contains NPI or CLIA, the identifier system for
 * NPI:http://hl7.org/fhir/sid/us-npi or CLIA:urn:oid:2.16.840.1.113883.4.7
 * - HDRP OrgUnits have no telecom or address
 *
 * @since v.1.52.0, HDRP.v.2025.3.0
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

  name = context.source[organizationUnit().multilinguals()].find { final def ml ->
    ml[Multilingual.LANGUAGE] == "de" && ml[Multilingual.SHORT_NAME] != null
  }?.getAt(Multilingual.SHORT_NAME) as String

}
