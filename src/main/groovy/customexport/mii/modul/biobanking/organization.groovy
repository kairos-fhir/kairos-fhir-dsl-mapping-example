package customexport.mii.modul.biobanking

import de.kairos.fhir.centraxx.metamodel.Multilingual

import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

/**
 * Represented by a HDRP OrganizationUnit
 * @author Jonas Küttner
 * @since KAIROS-FHIR-DSL.v.1.52.0, HDRP.v.2025.3.0
 */
organization {

  id = "Organization/" + context.source[organizationUnit().id()]

  meta {
    profile("https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ProfileOrganizationSammlungBiobank")
  }
  // This only applies, if you use the bbmri-eric as the orgUnit code in HDRP.
  identifier {
    system = "http://www.bbmri-eric.eu/"
    value = context.source[organizationUnit().code()]
  }

  name = context.source[organizationUnit().multilinguals()].find { final def ml ->
    ml[Multilingual.LANGUAGE] == "en" && ml[Multilingual.SHORT_NAME] != null
  }?.getAt(Multilingual.SHORT_NAME) as String

  if (context.source[organizationUnit().parent()]) {
    partOf {
      reference = "Organization/" + context.source[organizationUnit().parent().id()]
    }
  }

}

