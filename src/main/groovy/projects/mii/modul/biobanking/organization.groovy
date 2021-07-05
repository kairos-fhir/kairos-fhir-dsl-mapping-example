package projects.mii.modul.biobanking

import de.kairos.fhir.centraxx.metamodel.MultilingualEntry

import static de.kairos.fhir.centraxx.metamodel.RootEntities.organizationUnit

/**
 * Represented by a CXX OrganizationUnit
 * @author Jonas Küttner
 * @since KAIROS-FHIR-DSL.v.1.8.0, CXX.v.3.18.1.1
 */
organization {

  id = "Organization/" + context.source[organizationUnit().id()]

  meta {
    profile("https://www.medizininformatik-initiative.de/fhir/ext/modul-biobank/StructureDefinition/ProfileOrganizationSammlungBiobank")
  }
  // This only applies, if you use the bbmri-eric as the orgUnit code in CXX.
  identifier {
    system = "http://www.bbmri-eric.eu/"
    value = context.source[organizationUnit().code()]
  }

  name = context.source[organizationUnit().nameMultilingualEntries()].find { final def entry ->
    "de" == entry[MultilingualEntry.LANG]
  }[MultilingualEntry.VALUE]

  if (context.source[organizationUnit().parent()]) {
    partOf {
      reference = "Organization/" + context.source[organizationUnit().parent().id()]
    }
  }

}

