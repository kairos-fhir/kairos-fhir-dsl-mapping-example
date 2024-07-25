package customimport.ctcue.customimport

import de.kairos.centraxx.fhir.r4.utils.FhirUrls
import de.kairos.fhir.centraxx.metamodel.enums.LaborMappingType
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueType
import org.hl7.fhir.r4.model.AllergyIntolerance
import org.hl7.fhir.r4.model.Extension
import org.hl7.fhir.r4.model.IntegerType
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.StringType

// TODO: performer, location
// TODO: needs to be tested with example data
bundle {

  context.bundles.each { final def bundle ->
    bundle.getEntry().findAll { it.getResource().getResourceType() == ResourceType.AllergyIntolerance }.each {

      final AllergyIntolerance sourceAI = it.getResource() as AllergyIntolerance
      entry {
        fullUrl = "Observation/unknown"

        request {
          method = "POST"
          url = "Observation/unknown" // unknown is possible, because the matching is done by identifier.
        }

        resource {
          observation {
            id = "unknown"

            meta {
              tag {
                system = FhirUrls.System.CXX_ENTITY
                code = "LaborMapping"
              }
            }

            // Trying to create the master data here from
            extension {
              url = FhirUrls.Extension.LaborMapping.CREATE_PROFILE
              valueBoolean = true
            }
            extension {
              url = FhirUrls.Extension.LaborMapping.INCREMENT_LABORMETHOD_VERSION
              valueBoolean = false
            }
            extension {
              url = FhirUrls.Extension.LABOR_MAPPING
              extension {
                url = FhirUrls.Extension.LaborMapping.LABOR_MAPPING_TYPE
                valueString = LaborMappingType.PATIENTLABORMAPPING.toString()
              }
              extension {
                url = FhirUrls.Extension.LaborMapping.PATIENT
                value = sourceAI.getPatient()
              }
              extension {
                url = FhirUrls.Extension.LaborMapping.RELATED_REFERENCE
                value = sourceAI.getPatient()
              }
            }
            status = Observation.ObservationStatus.FINAL

            identifier {
              system = FhirUrls.System.Finding.LABOR_FINDING_ID
              value = "AllergyIntolerance_" + sourceAI.getIdentifierFirstRep().getValue()
            }

            category {
              coding {
                system = FhirUrls.System.LaborMethod.Category.BASE_URL
                code = "GENERAL"
              }
            }

            code {
              coding {
                system = FhirUrls.System.Finding.LABOR_FINDING_SHORTNAME
                code = "AllergyIntolerance_" + sourceAI.getIdentifierFirstRep().getValue()
              }
            }

            subject = sourceAI.getPatient()

            method {
              coding {
                system = FhirUrls.System.LaborMethod.BASE_URL
                code = "AllergyIntolerance"
                display = "AllergyIntolerance"
              }
            }

            if (sourceAI.hasCode() && sourceAI.getCode().hasCoding()) {
              component {
                extension = createLaborValueExtension(LaborValueType.STRING)

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "AllergyIntolerance.code.coding.code"
                    display = "AllergyIntolerance.code.coding.code"
                  }
                }

                valueString = sourceAI.getCode().getCodingFirstRep().getCode()
              }

              component {
                extension = createLaborValueExtension(LaborValueType.STRING)

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "AllergyIntolerance.code.coding.system"
                    display = "AllergyIntolerance.code.coding.system"
                  }
                }

                valueString = sourceAI.getCode().getCodingFirstRep().getSystem()
              }

              component {
                extension = createLaborValueExtension(LaborValueType.STRING)

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "AllergyIntolerance.code.coding.display"
                    display = "AllergyIntolerance.code.coding.display"
                  }
                }

                valueString = sourceAI.getCode().getCodingFirstRep().getDisplay()
              }
            }

            if (sourceAI.hasClinicalStatus() && sourceAI.getClinicalStatus().hasCoding()) {
              component {
                extension = createLaborValueExtension(LaborValueType.STRING)

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "AllergyIntolerance.clinicalStatus.coding.code"
                    display = "AllergyIntolerance.clinicalStatus.coding.code"
                  }
                }

                valueString = sourceAI.getClinicalStatus().getCodingFirstRep().getCode()
              }

              component {
                extension = createLaborValueExtension(LaborValueType.STRING)

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "AllergyIntolerance.clinicalStatus.coding.display"
                    display = "AllergyIntolerance.clinicalStatus.coding.display"
                  }
                }

                valueString = sourceAI.getClinicalStatus().getCodingFirstRep().getDisplay()
              }
            }

            // category is a list of codes -> only one can be mapped right now.
            if (sourceAI.hasCategory()) {
              component {
                extension = createLaborValueExtension(LaborValueType.STRING)

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "AllergyIntolerance.category"
                    display = "AllergyIntolerance.category"
                  }
                }

                valueString = sourceAI.getCategory().iterator().next().getValue().toCode()
              }
            }

            if (sourceAI.hasOnsetPeriod()) {
              component {
                extension = createLaborValueExtension(LaborValueType.DATE)

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "AllergyIntolerance.onsetPeriod.start"
                    display = "AllergyIntolerance.onsetPeriod.start"
                  }
                }

                valueDateTime = sourceAI.getOnsetPeriod().getStart()
              }

              component {
                extension = createLaborValueExtension(LaborValueType.DATE)

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "AllergyIntolerance.onsetPeriod.end"
                    display = "AllergyIntolerance.onsetPeriod.end"
                  }
                }

                valueDateTime = sourceAI.getOnsetPeriod().getEnd()
              }
            }

            if (sourceAI.hasOnsetDateTimeType()) {
              component {
                extension = createLaborValueExtension(LaborValueType.DATE)

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "AllergyIntolerance.onsetPeriod.start"
                    display = "AllergyIntolerance.onsetPeriod.start"
                  }
                }

                valueDateTime = sourceAI.getOnsetDateTimeType()
              }
            }

            if (sourceAI.hasRecordedDate()) {
              component {
                extension = createLaborValueExtension(LaborValueType.DATE)

                code {
                  coding {
                    system = FhirUrls.System.LaborValue.BASE_URL
                    code = "AllergyIntolerance.recordedDate"
                    display = "AllergyIntolerance.recordedDate"
                  }
                }

                valueDateTime = sourceAI.getRecordedDate()
              }
            }

            // could have more than one reaction
            if (sourceAI.hasReaction()) {
              if (sourceAI.getReactionFirstRep().hasSubstance()) {
                component {
                  extension = createLaborValueExtension(LaborValueType.STRING)

                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = "AllergyIntolerance.reaction.substance.coding.code"
                      display = "AllergyIntolerance.reaction.substance.coding.code"
                    }
                  }

                  valueString = sourceAI.getReactionFirstRep().getSubstance().getCodingFirstRep().getCode()
                }

                component {
                  extension = createLaborValueExtension(LaborValueType.STRING)

                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = "AllergyIntolerance.reaction.substance.coding.system"
                      display = "AllergyIntolerance.reaction.substance.coding.system"
                    }
                  }

                  valueString = sourceAI.getReactionFirstRep().getSubstance().getCodingFirstRep().getSystem()
                }

                component {
                  extension = createLaborValueExtension(LaborValueType.STRING)
                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = "AllergyIntolerance.reaction.substance.coding.display"
                      display = "AllergyIntolerance.reaction.substance.coding.display"
                    }
                  }

                  valueString = sourceAI.getReactionFirstRep().getSubstance().getCodingFirstRep().getDisplay()
                }
              }

              // could habe multiple manifestations
              if (sourceAI.getReactionFirstRep().hasManifestation()) {
                component {
                  extension = createLaborValueExtension(LaborValueType.STRING)

                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = "AllergyIntolerance.reaction.manifestation.coding.code"
                      display = "AllergyIntolerance.reaction.manifestation.coding.code"
                    }
                  }

                  valueString = sourceAI.getReactionFirstRep().getManifestationFirstRep().getCodingFirstRep().getCode()
                }

                component {
                  extension = createLaborValueExtension(LaborValueType.STRING)

                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = "AllergyIntolerance.reaction.manifestation.coding.system"
                      display = "AllergyIntolerance.reaction.manifestation.coding.system"
                    }
                  }

                  valueString = sourceAI.getReactionFirstRep().getManifestationFirstRep().getCodingFirstRep().getSystem()
                }

                component {
                  extension = createLaborValueExtension(LaborValueType.STRING)
                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = "AllergyIntolerance.reaction.manifestation.coding.display"
                      display = "AllergyIntolerance.reaction.manifestation.coding.display"
                    }
                  }

                  valueString = sourceAI.getReactionFirstRep().getManifestationFirstRep().getCodingFirstRep().getDisplay()
                }
              }

              if (sourceAI.getReactionFirstRep().hasDescription()) {
                component {
                  extension = createLaborValueExtension(LaborValueType.STRING)

                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = "AllergyIntolerance.reaction.description"
                      display = "AllergyIntolerance.reaction.description"
                    }
                  }

                  valueString = sourceAI.getReactionFirstRep().getDescription()
                }
              }

              if (sourceAI.getReactionFirstRep().hasSeverity()) {
                component {
                  extension = createLaborValueExtension(LaborValueType.STRING)

                  code {
                    coding {
                      system = FhirUrls.System.LaborValue.BASE_URL
                      code = "AllergyIntolerance.reaction.severity"
                      display = "AllergyIntolerance.reaction.severity"
                    }
                  }

                  valueString = sourceAI.getReactionFirstRep().getSeverity().toCode()
                }
              }
            }
          }
        }
      }
    }
  }
}

static List<Extension> createLaborValueExtension(final LaborValueType laborValueType) {
  return [
      new Extension(
          FhirUrls.Extension.LaborValue.LABORVALUETYPE,
          new StringType(laborValueType.toString())
      ),
      new Extension(
          FhirUrls.Extension.LaborValue.VALUE_INDEX,
          new IntegerType(0)
      )
  ]
}
