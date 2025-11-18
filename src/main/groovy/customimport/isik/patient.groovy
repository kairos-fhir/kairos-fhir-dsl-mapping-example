package customimport.isik

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient

import javax.annotation.Nullable

// example mapping
final def idMap = [
    "https://some-uri/isik-id": "ISIK",
    "https://some-uri/test-id": "TEST-ID"
]

/**
 * Transforms a ISIK patient into and HDRP patient. Requires the definition of possible Identifier types as IdContainerTypes in HDRP.
 */
bundle {

  type = Bundle.BundleType.TRANSACTION

  final List<Patient> sourcePatients = getPatientsFromBundles(context.bundles)

  println(sourcePatients.size())

  if (sourcePatients.isEmpty()) {
    return
  }

  sourcePatients.each { final Patient sourcePatient ->

    println(sourcePatient.birthDate)
    final List<Identifier> sourceIdentifiers = getSourceIdentifier(sourcePatient.getIdentifier());
    final HumanName sourceOfficalName = getSourceOfficialName(sourcePatient.getName())
    final HumanName sourceMaidenName = getSourceMaidenName(sourcePatient.getName())

    final Address firstPhysicalAddress = getFirstPhysicalAddress(sourcePatient.getAddress())

    entry {
      resource {
        patient {
          id = sourcePatient.id

          for (final Identifier sourceIdentifier: sourceIdentifiers){
            final String idContainerTypeCode = mapSystem(sourceIdentifier.system, idMap)
            identifier {
              type {
                coding {
                  system = FhirUrls.System.IdContainerType.BASE_URL
                  code = idContainerTypeCode
                }
                value = sourceIdentifier.value
              }
            }
          }

          if (sourceOfficalName != null) {
            humanName {
              use = HumanName.NameUse.OFFICIAL
              family = sourceOfficalName.family
              given(sourceOfficalName.getGivenAsSingleString())
            }
          }

          if (sourceMaidenName != null) {
            humanName {
              use = HumanName.NameUse.MAIDEN
              family = sourceMaidenName.family
            }
          }

          // use first physical if given
          if (firstPhysicalAddress != null) {
            address {
              if (firstPhysicalAddress.line.size() == 1) {
                line(firstPhysicalAddress.getLine().get(0).getValue())
              } else {
                line(firstPhysicalAddress.line.collect { it.getValue() }.join(" "))
              }

              postalCode = firstPhysicalAddress.postalCode
              country = firstPhysicalAddress.country
            }
          }

          gender = sourcePatient.gender

          birthDate {
            date = sourcePatient.getBirthDate()
            if (sourcePatient.getBirthDateElement().hasExtension("http://hl7.org/fhir/StructureDefinition/data-absent-reason")) {
              extension.add(sourcePatient.getBirthDateElement().getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/data-absent-reason"))
            }
          }
        }
      }

      request {
        method = "POST"
        url = "Patient/unknown" // unknown is possible, because the matching is done by identifier.
      }
    }
  }
}

private static List<Patient> getPatientsFromBundles(final List<Bundle> bundles) {
  return bundles
      .collect { getPatientFromBundle(it) }
      .collectMany { it }
}

private static List<Patient> getPatientFromBundle(final Bundle sourceBundle) {
  return sourceBundle.getEntry()
      .findAll { it.hasResource() }
      .collect { it.getResource() }
      .findAll { it instanceof Patient }
      .collect { (Patient) it }
}

/**
 * The identifier is sliced by pattern, so we filter for "Patientennummer" slices here.
 * @param identifiers
 * @return
 */
private static List<Identifier> getSourceIdentifier(final List<Identifier> identifiers) {
  return identifiers
      .findAll { it.hasType() }
      .findAll {
        it.getType().hasCoding("http://terminology.hl7.org/CodeSystem/v2-0203", "MR")
      }
}

@Nullable
private static String mapSystem(final String system, final Map<String, String> idMap) {
  return idMap.get(system)
}

@Nullable
private static HumanName getSourceOfficialName(final List<HumanName> humanNames) {
  return humanNames.find { it.use == HumanName.NameUse.OFFICIAL }
}

@Nullable
private static HumanName getSourceMaidenName(final List<HumanName> humanNames) {
  return humanNames.find { it.use == HumanName.NameUse.MAIDEN }
}

@Nullable
private static Address getFirstPhysicalAddress(final List<Address> addresses) {
  return addresses.find { it.type == Address.AddressType.BOTH }
}
