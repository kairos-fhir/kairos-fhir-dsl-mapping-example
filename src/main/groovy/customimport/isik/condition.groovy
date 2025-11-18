package customimport.isik

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.enums.ChoiceType
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Coding
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Extension

import javax.annotation.Nullable

// example mapping
final def idMap = [
    "https://some-uri/isik-id": "ISIK",
    "https://some-uri/test-id": "TEST-ID"
]

/**
 * Transforms an ISIK Conditions into a HDRP Conditions
 *
 * Clinical status is imported as LaborFinding linked to the Diagnosis. For this a Catalog with code
 * FHIR_CONDITION_CLINICAL_STATUS must be created in HDRP master data. Furthermore ICD-10-GM-DE is required
 * to import IcdEntries.
 * with code
 */
bundle {
  type = Bundle.BundleType.TRANSACTION

  final List<Condition> sourceConditions = getConditionsFromBundles(context.bundles)

  if (sourceConditions.isEmpty()) {
    return
  }

  sourceConditions.each { final Condition sourceCondition ->

    final Coding icdCoding = sourceCondition.getCode().getCoding().find { final Coding sourceCoding ->
      sourceCoding.hasSystem() && sourceCoding.hasCode() && sourceCoding.getSystem() == "http://fhir.de/CodeSystem/bfarm/icd-10-gm"
    }

    entry {
      resource {
        condition {
          id = sourceCondition.id

          // use an HDRP identifier to use natural identifiers
          identifier {
            value = "ISIK_Diagnosis_" + sourceCondition.id
          }

          if (icdCoding != null) {
            code {
              coding {
                system = FhirUrls.Catalog.ICD_CATALOG + "/#c.ICD-10-GM-DE#v." + icdCoding.version + "-GM-DE"
                code = icdCoding.code
                version = "code"
              }
            }
          }

          final String certainty = getDiagnosisCertainty(icdCoding)
          if (certainty != null) {
            extension {
              url = FhirUrls.Extension.Diagnosis.DIAGNOSIS_CERTAINTY
              valueCoding {
                system = FhirUrls.System.Diagnosis.DiagnosisCertainty.BASE_URL
                code = certainty
              }
            }
          }

          if (sourceCondition.subject.identifier != null) {
            final String idContainerTypeCode = mapSystem(sourceCondition.subject.identifier.system, idMap)
            subject {
              identifier {
                type {
                  coding {
                    system = FhirUrls.System.IdContainerType.BASE_URL
                    code = idContainerTypeCode
                  }
                  value = sourceCondition.subject.identifier.value
                }
              }
            }
          }

          recordedDate = sourceCondition.recordedDate

          // not must support in isik but would be required to updated an existing condition properly
          onsetDateTime {
            date = sourceCondition.getOnsetDateTimeType()?.getValue()
          }

          note = sourceCondition.note
        }
      }
      request {
        method = Bundle.HTTPVerb.POST
        url = "Condition/unknown" // unknown is possible, because the matching is done by identifier.
      }
    }

    if (sourceCondition.getClinicalStatus() != null) {
      entry {
        resource {
          observation {
            id = "Observation/" + sourceCondition.id

            extension {
              url = FhirUrls.Extension.LABOR_MAPPING
              extension {
                url = FhirUrls.Extension.LaborMapping.LABOR_MAPPING_TYPE
                valueString = LaborMappingType.DIAGNOSIS.toString()
              }
              extension {
                url = FhirUrls.Extension.LaborMapping.PATIENT
                final String idContainerTypeCode = mapSystem(sourceCondition.subject.identifier.system, idMap)
                valueReference {
                  identifier {
                    type {
                      coding {
                        system = FhirUrls.System.IdContainerType.BASE_URL
                        code = idContainerTypeCode
                      }
                      value = sourceCondition.subject.identifier.value
                    }
                  }
                }
              }
              extension {
                url = FhirUrls.Extension.LaborMapping.RELATED_REFERENCE
                valueReference {
                  identifier {
                    value = "ISIK_Diagnosis_" + sourceCondition.id
                  }
                }
              }
            }
            extension {
              url = FhirUrls.Extension.LaborMapping.CREATE_PROFILE
              valueBoolean = true
            }

            identifier {
              system = FhirUrls.System.Finding.LABOR_FINDING_ID
              value = "Additional Data Condition " + sourceCondition.id
            }

            code {
              coding {
                system = FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME
                code = "Additional Data Condition " + sourceCondition.id
              }
            }

            effectiveDateTime {
              date = sourceCondition.recordedDate
            }

            method {
              coding {
                system = FhirUrls.System.LaborMethod.BASE_URL
                code = "ISIK_ADDITIONAL_CONDITION_DATA"
              }
            }

            component {
              extension {
                url = FhirUrls.Extension.LaborValue.LABORVALUETYPE
                valueString = LaborValueDType.CATALOG.toString()
              }
              extension {
                url = FhirUrls.Extension.LaborValue.CHOICE_TYPE
                valueString = ChoiceType.SELECTONE.toString()
              }
              extension {
                url = FhirUrls.Extension.LaborValue.VALUE_INDEX
                valueInteger = 0
              }
              code {
                coding {
                  system = FhirUrls.System.LaborValue.BASE_URL
                  code = "Condition.clinicalStatus"
                }
              }
              valueCodeableConcept {
                coding {
                  system = "urn:centraxx:CodeSystem/Catalog#c.FHIR_CONDITION_CLINICAL_STATUS"
                  code = sourceCondition.getClinicalStatus().getCodingFirstRep().getCode()
                }
              }
            }
          }
        }

        request {
          method = Bundle.HTTPVerb.POST
          url = "Observation/unknown" // unknown is possible, because the matching is done by identifier.
        }
      }
    }
  }
}

private static List<Condition> getConditionsFromBundles(final List<Bundle> bundles) {
  return bundles
      .collect { getConditionFromBundle(it) }
      .collectMany { it }
}

private static List<Condition> getConditionFromBundle(final Bundle sourceBundle) {
  sourceBundle.getEntry()
      .findAll { it.hasResource() }
      .collect { it.getResource() }
      .findAll { it instanceof Condition }
      .collect { (Condition) it }
}

@Nullable
private static String getDiagnosisCertainty(final Coding coding) {
  final Extension ext = coding.getExtensionByUrl("http://fhir.de/StructureDefinition/icd-10-gm-diagnosesicherheit")
  if (ext == null) {
    return null
  }

  return ((Coding) ext.getValue())?.getCode()
}

@Nullable
private static String mapSystem(final String system, final Map<String, String> idMap) {
  return idMap.get(system)
}
