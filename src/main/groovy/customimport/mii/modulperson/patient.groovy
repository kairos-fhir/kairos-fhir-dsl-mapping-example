package customimport.mii.modulperson

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import org.hl7.fhir.r4.model.Address
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.HumanName
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.codesystems.NameUse

bundle {

  // filter for patient entries
  context.bundles.each { final def bundle ->
    bundle.getEntry().findAll { it.getResource().getResourceType() == ResourceType.Patient }
        .each {
          final Patient sourcePatient = it.getResource() as Patient

          // get bundle request to set to entries. Could also transform it here.
          final Bundle.BundleEntryRequestComponent requestToSet = it.getRequest()


          entry {
            request = requestToSet
            resource {
              patient {
                id = sourcePatient.getId()

                final Identifier gkv = sourcePatient.identifier.find {
                  it.type?.codingFirstRep?.system == "http://fhir.de/CodeSystem/identifier-type-de-basis" && it.type?.codingFirstRep?.code == "GKV"
                }

                if (gkv) {
                  identifier {
                    type {
                      coding {
                        system = FhirUrls.System.IdContainerType.BASE_URL
                        code = "GKV"
                      }
                    }
                    value = gkv.value
                  }
                }

                final Identifier pid = sourcePatient.identifier.find {
                  it.type?.codingFirstRep?.system == "http://terminology.hl7.org/CodeSystem/v2-0203" && it.type?.codingFirstRep?.code == "MR"
                }

                if (pid) {
                  identifier {
                    type {
                      coding {
                        system = FhirUrls.System.IdContainerType.BASE_URL
                        code = "PATIENTID"
                      }
                    }
                    value = pid.value
                  }
                }

                final Identifier pkv = sourcePatient.identifier.find {
                  it.type?.codingFirstRep?.system == "http://fhir.de/CodeSystem/identifier-type-de-basis" && it.type?.codingFirstRep?.code == "PKV"
                }

                if (pkv) {
                  identifier {
                    type {
                      coding {
                        system = FhirUrls.System.IdContainerType.BASE_URL
                        code = "PKV"
                      }
                    }
                    value = pkv.value
                  }
                }

                /**
                 * For import the mandatory system Ids must be set.
                 * TODO: Figure something out here that makes sense.
                 */
                final Identifier notNull = [pkv, gkv, pid].find{it != null}

                identifier {
                  type {
                    coding {
                      system = FhirUrls.System.IdContainerType.BASE_URL
                      code = "SID"
                    }
                  }
                  value = notNull.value
                }

                /**
                 * Names
                 * Currently we only import given and last name
                 * CXX supports title, affix, birthname
                 */

                final HumanName firstSourceName = sourcePatient.name.find {
                  it.use.toCode() == NameUse.OFFICIAL.toCode()
                }

                if (firstSourceName) {
                  humanName {
                    firstSourceName.given.each {
                      given.add(it.getValue())
                    }

                    family = firstSourceName.family
                  }
                }

                final HumanName firstSourceBirthName = sourcePatient.name.find {
                  it.use.toCode() == NameUse.MAIDEN.toCode()
                }

                if (firstSourceBirthName) {
                  /**
                   * not yet implemented in CXX importer
                   */
                }

                /**
                 * CXX does only map the FHIR administrative Genders back to the CXX Genders
                 * MII uses and extension for the case UNKNOWN which specifies whether it is "unbestimmt" or "divers"
                 */
                gender = sourcePatient.gender

                if (sourcePatient.birthDate) {
                  final Extension dataAbsentReasonExtension = sourcePatient.birthDateElement
                      .getExtensionByUrl("http://hl7.org/fhir/StructureDefinition/data-absent-reason")

                  birthDate {
                    date = sourcePatient.birthDate

                    if(dataAbsentReasonExtension){
                      extension.add(dataAbsentReasonExtension)
                    }
                  }

                }

                if (sourcePatient.deceasedDateTimeType) {
                  deceasedDateTime {
                    date = sourcePatient.deceasedDateTimeType.value
                  }
                }

                final Address firstAddress = sourcePatient.addressFirstRep

                /**
                 * Importer currently considers first line item to be street and second to be HouseNo
                 *
                 * country, state, postfach is supported by CXX but not yet by the FHIR importer
                 *
                 */

                if (firstAddress) {
                  address {
                    city = firstAddress.city
                    postalCode = firstAddress.postalCode

                    if (firstAddress.line.size() == 1) {
                      line.add(firstAddress.line.get(0).getValue())
                    } else {
                      final String street = firstAddress.line.find {
                        it.hasExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName")
                      }?.getValue()

                      final String houseNo = firstAddress.line.find {
                        it.hasExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber")
                      }?.getValue()

                      /**
                       * not supported by importer yet
                       */
                      final String postFach = firstAddress.line.find {
                        it.hasExtension("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-postBox")
                      }?.getValue()

                      line.add(street)
                      line.add(houseNo)

                    }
                  }
                }
              }
            }
          }
        }
  }
}

