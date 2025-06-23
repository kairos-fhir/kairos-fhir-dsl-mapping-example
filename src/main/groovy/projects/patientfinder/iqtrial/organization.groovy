package projects.patientfinder.iqtrial

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.LANG
import static de.kairos.fhir.centraxx.metamodel.MultilingualEntry.VALUE
import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

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

  name = context.source[organizationUnit().multilinguals()]
      ?.find { final def ml -> ml[Multilingual.LANGUAGE] == "en" && ml[Multilingual.SHORT_NAME] != null }
      ?.getAt(Multilingual.SHORT_NAME) as String

}



