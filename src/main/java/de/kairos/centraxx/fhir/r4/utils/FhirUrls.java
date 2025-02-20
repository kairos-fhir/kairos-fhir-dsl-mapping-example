package de.kairos.centraxx.fhir.r4.utils;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * Registry class for FHIR URI definitions
 * @author Mike Wähnert
 */
public final class FhirUrls {
  /**
   * Base URL for identifying FHIR resources of a CXX system
   */
  private static final String CXX_BASE_URL = "https://fhir.centraxx.de";

  public static final class Extension {
    /**
     * Base URL for identifying FHIR extensions of a CXX system in accordance with
     * <a href="https://hl7.org/FHIR/extensibility-definitions.html#Extension.url">Extension.url</a>
     */
    private static final String BASE_URL = CXX_BASE_URL + "/extension";
    public static final String SAMPLE_LOCATION = Extension.BASE_URL + "/sampleLocation";
    public static final String SPREC = BASE_URL + "/sprec";
    public static final String SAMPLE_CATEGORY = BASE_URL + "/sampleCategory";
    public static final String LABOR_MAPPING = BASE_URL + "/laborMapping";
    public static final String CREATE_MASTER_DATA = BASE_URL + "/createMasterData";

    /**
     * General URL for resource-wide extension to set the update mode - true means "update with replace/overwrite"
     */
    public static final String UPDATE_WITH_OVERWRITE = BASE_URL + "/updateWithOverwrite";

    private static final String NAME = "/name";

    private Extension() {/* hide constructor */}

    public static List<String> getAllDomains() {
      final List<String> domains = new ArrayList<>();
      domains.add(SAMPLE_LOCATION);
      domains.add(SPREC);
      domains.add(SAMPLE_CATEGORY);
      domains.add(LABOR_MAPPING);
      domains.add(UPDATE_WITH_OVERWRITE);
      domains.add(CREATE_MASTER_DATA);
      domains.addAll(Patient.getAllDomains());
      domains.addAll(Sprec.getAllDomains());
      domains.addAll(Study.getAllDomains());
      domains.addAll(Survey.getAllDomains());
      domains.addAll(CrfTemplate.getAllDomains());
      domains.addAll(LaborValue.getAllDomains());
      domains.addAll(Hotline.getAllDomains());
      domains.addAll(Sample.getAllDomains());
      domains.addAll(Medication.getAllDomains());
      domains.addAll(Consent.getAllDomains());
      domains.addAll(Consent.Revocation.getAllDomains());
      domains.addAll(Calendar.getAllDomains());
      domains.addAll(Task.getAllDomains());
      domains.addAll(Crf.getAllDomains());
      domains.addAll(StudyVisitItem.getAllDomains());
      domains.addAll(Document.getAllDomains());
      domains.addAll(ServiceRequest.getAllDomains());
      domains.addAll(LaborMapping.getAllDomains());
      domains.addAll(SampleLocation.getAllDomains());
      domains.addAll(Translation.getAllDomains());
      domains.addAll(FlexiFlagItem.getAllDomains());
      domains.addAll(Tumor.getAllDomains());
      domains.addAll(Histology.getAllDomains());
      domains.addAll(Catalogs.getAllDomains());
      domains.addAll(ContactAddress.getAllDomains());
      domains.addAll(RadiationTarget.getAllDomains());
      domains.addAll(PatientTransfer.getAllDomains());
      domains.addAll(FollowDisease.getAllDomains());
      domains.addAll(GtdsTherapy.getAllDomains());
      domains.addAll(GtdsTherapy.Surgery.getAllDomains());
      domains.addAll(GtdsTherapy.RadiationTherapy.getAllDomains());
      domains.addAll(GtdsTherapy.SystemTherapy.getAllDomains());
      domains.addAll(RadiationComponent.getAllDomains());
      domains.addAll(SurgeryComponent.getAllDomains());
      domains.addAll(PreexistingIllness.getAllDomains());
      domains.addAll(DeathCause.getAllDomains());
      domains.addAll(AdverseEffects.getAllDomains());
      domains.addAll(StudyMember.getAllDomains());
      domains.addAll(FhirDefaults.getAllDomains());
      domains.addAll(PatientInsurance.getAllDomains());
      domains.addAll(MedProcedure.getAllDomains());
      domains.addAll(Diagnosis.getAllDomains());
      domains.addAll(LaborMethod.getAllDomains());
      domains.addAll(MeasurementSeries.getAllDomains());
      return domains;
    }

    @Nonnull
    private static Map<String, String> getExtensionMap(@Nonnull final List<String> extensionUrls) {
      final Map<String, String> subExtensionsMap = new HashMap<>();
      extensionUrls.forEach(domain -> subExtensionsMap.put(getLastSplit(domain.split("/")), domain));
      return subExtensionsMap;
    }

    @Nonnull
    private static String getLastSplit(@Nonnull final String[] array) {
      return array[array.length - 1];
    }

    public static final class MeasurementSeries {
      private static final String BASE_URL = Extension.BASE_URL + "/measurementSeries";

      public static final String DEFINITION = BASE_URL + "/definition";

      public static List<String> getAllDomains() {
        final ArrayList<String> domains = new ArrayList<>();
        domains.addAll(MeasurementSeriesDefinition.getAllDomains());
        domains.addAll(singletonList(DEFINITION));

        return domains;
      }

      private MeasurementSeries() {/* hide constructor */}

      public static final class MeasurementSeriesDefinition {

        public static final String LABOR_VALUE = MeasurementSeries.DEFINITION + "/laborValue";

        private MeasurementSeriesDefinition() {/* hide constructor */}

        @Nonnull
        public static List<String> getAllDomains() {
          return singletonList(LABOR_VALUE);
        }
      }
    }

    public static final class Sample {

      private static final String SAMPLE_BASE_URL = Extension.BASE_URL + "/sample";
      public static final String DERIVAL_DATE = SAMPLE_BASE_URL + "/derivalDate";
      public static final String REPOSITION_DATE = SAMPLE_BASE_URL + "/repositionDate";
      public static final String SAMPLE_LOCATION = SAMPLE_BASE_URL + "/sampleLocation";
      public static final String SAMPLE_LOCATION_PATH = SAMPLE_BASE_URL + "/sampleLocationPath";
      public static final String X_POSITION = SAMPLE_BASE_URL + "/xPosition";
      public static final String Y_POSITION = SAMPLE_BASE_URL + "/yPosition";
      public static final String ORGANIZATION_UNIT = SAMPLE_BASE_URL + "/organizationUnit";
      public static final String CONCENTRATION = SAMPLE_BASE_URL + "/concentration";
      public static final String EPISODE = SAMPLE_BASE_URL + "/episode";
      public static final String PROJECT = SAMPLE_BASE_URL + "/project";

      private Sample() {/* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        final List<String> all = new ArrayList<>();

        all.addAll(asList(DERIVAL_DATE, REPOSITION_DATE, SAMPLE_LOCATION, SAMPLE_LOCATION_PATH,
                          X_POSITION, Y_POSITION, ORGANIZATION_UNIT, CONCENTRATION, EPISODE, PROJECT));

        all.addAll(Project.getAllDomains());
        return all;
      }

      public static final class Project {
        public static final String NAME = PROJECT + "/name";
        public static final String ACCREDITATION_DATE = PROJECT + "/accreditationDate";
        public static final String PROJECT_MANAGER = PROJECT + "/projectManager";
        public static final String SYNOPSIS = PROJECT + "/synopsis";
        public static final String START_DATE = PROJECT + "/startDate";
        public static final String END_DATE = PROJECT + "/endDate";
        public static final String STATE = PROJECT + "/state";
        public static final String CODE = PROJECT + "/code";
        public static final String IS_SPECIAL_SAMPLEHANDLING = PROJECT + "/isSpecialSampleHandling";

        private Project() {}

        @Nonnull
        public static List<String> getAllDomains() {
          return asList(NAME,
                        ACCREDITATION_DATE,
                        PROJECT_MANAGER,
                        SYNOPSIS,
                        START_DATE,
                        END_DATE,
                        STATE,
                        CODE,
                        IS_SPECIAL_SAMPLEHANDLING);
        }
      }
    }

    public static final class LaborMethod {
      private static final String BASE_URL = Extension.BASE_URL + "/laborMethod";
      public static final String PARENT = BASE_URL + "/parent";

      private LaborMethod() {
      }

      @Nonnull
      public static Collection<String> getAllDomains() {
        return asList(PARENT);
      }
    }

    public static final class MedProcedure {
      private static final String BASE_URL = Extension.BASE_URL + "/medProcedure";
      public static final String PROCEDURE_STATUS_PLANNED = BASE_URL + "/status/planned";

      private MedProcedure() {
      }

      @Nonnull
      public static Collection<String> getAllDomains() {
        return singletonList(PROCEDURE_STATUS_PLANNED);
      }
    }

    public static final class Diagnosis {
      private static final String BASE_URL = Extension.BASE_URL + "/diagnosis";

      public static final String ATTESTATION_DATE = Extension.BASE_URL + "/attestationDate";

      private Diagnosis() {}

      @Nonnull
      public static Collection<String> getAllDomains() {
        return singletonList(ATTESTATION_DATE);
      }
    }

    public static final class ContactAddress {
      private static final String BASE_URL = Extension.BASE_URL + "/address";
      public static final String TITLE = BASE_URL + "/title";
      public static final String INSTITUTE = BASE_URL + "/institute";
      public static final String DEPARTMENT = BASE_URL + "/department";
      public static final String POSITION = BASE_URL + "/position";
      public static final String CXX_CONTACT_ID = BASE_URL + "/cxxContactId";


      private ContactAddress() {
      }

      @Nonnull
      public static Collection<String> getAllDomains() {
        return asList(TITLE, INSTITUTE, DEPARTMENT, POSITION, CXX_CONTACT_ID);
      }
    }

    public static final class PatientInsurance {
      private static final String BASE_URL = Extension.BASE_URL + "/patientInsurance";
      public static final String POLICE_NUMBER = BASE_URL + "/policeNumber";
      public static final String GROUP_NUMBER = BASE_URL + "/groupNumber";
      public static final String INSURED = BASE_URL + "/insured";
      public static final String RANK = BASE_URL + "/rank";

      private PatientInsurance() {
      }

      @Nonnull
      public static Collection<String> getAllDomains() {
        return asList(POLICE_NUMBER, GROUP_NUMBER, INSURED, RANK);
      }
    }

    public static final class FlexiFlagItem {
      private static final String BASE_URL = Extension.BASE_URL + "/flexiFlagItem";
      public static final String COMMENTS = BASE_URL + "/comments";
      public static final String FLAG_PRIVATE = BASE_URL + "/flagPrivate";

      private FlexiFlagItem() {
      }

      @Nonnull
      public static Collection<String> getAllDomains() {
        return asList(COMMENTS, FLAG_PRIVATE);
      }
    }

    public static final class RadiationTarget {
      private static final String BASE_URL = Extension.BASE_URL + "/radiationTarget";
      public static final String RADIATION_COMPONENT = BASE_URL + "/radiationComponent";

      private RadiationTarget() {
      }

      @Nonnull
      public static List<String> getAllDomains() {
        return singletonList(RADIATION_COMPONENT);
      }
    }

    public static final class AdverseEffects {
      private static final String BASE_URL = Extension.BASE_URL + "/adverseEffects";
      public static final String RELATED = BASE_URL + "/related";
      public static final String KIND = BASE_URL + "/kind";
      public static final String COMMENTS = BASE_URL + "/comments";
      public static final String GRADE = BASE_URL + "/grade";
      public static final String VERSION = BASE_URL + "/version";

      private AdverseEffects() {
      }

      @Nonnull
      public static List<String> getAllDomains() {
        return asList(RELATED, KIND, COMMENTS, GRADE, VERSION);
      }
    }

    public static final class Histology {
      private static final String BASE_URL = Extension.BASE_URL + "/histology";
      public static final String SOURCEDICT = BASE_URL + "/sourceDict";

      public static Collection<String> getAllDomains() {
        return singletonList(SOURCEDICT);
      }

      private Histology() {
      }
    }

    public static final class Tumor {
      private static final String BASE_URL = Extension.BASE_URL + "/tumor";
      public static final String SOURCEDICT = BASE_URL + "/sourceDict";
      public static final String CAPTURECAUSEDICT = BASE_URL + "/captureCauseDict";
      public static final String PATIENTENLIGHTENSTATUS_DICT = BASE_URL + "/patientEnlightenStatusDict";
      public static final String ENLIGHTEN_DATETIME = BASE_URL + "/enlightenDateTime";
      public static final String THERAPYCAUSE_DICT = BASE_URL + "/therapyCauseDict";
      public static final String POSTCARE_AGREEMENT = BASE_URL + "/postCareAgreement";
      public static final String POSTCARE_AGREEMENT_DATETIME = BASE_URL + "/postCareAgreementDateTime";
      public static final String POSTCARE_STARTDATETIME = BASE_URL + "/postCareStartDateTime";
      public static final String POSTCARE_SCHEMA = BASE_URL + "/postCareSchema";
      public static final String VISITCAUSE_DICT = BASE_URL + "/visitCauseDict";

      private Tumor() {
      }

      @Nonnull
      public static Collection<String> getAllDomains() {
        return asList(SOURCEDICT,
                      CAPTURECAUSEDICT,
                      PATIENTENLIGHTENSTATUS_DICT,
                      ENLIGHTEN_DATETIME,
                      THERAPYCAUSE_DICT,
                      POSTCARE_AGREEMENT,
                      POSTCARE_AGREEMENT_DATETIME,
                      POSTCARE_STARTDATETIME,
                      POSTCARE_SCHEMA,
                      VISITCAUSE_DICT
        );
      }
    }

    public static final class FollowDisease {
      private static final String BASE_URL = Extension.BASE_URL + "/followDisease";
      public static final String VERSION = BASE_URL + "/version";
      public static final String GRADE = BASE_URL + "/grade";
      public static final String FOLLOWDISEASEKINDDICT = BASE_URL + "/followDiseaseKindDict";

      private FollowDisease() {
      }

      @Nonnull
      public static Collection<String> getAllDomains() {
        return asList(
          VERSION,
          GRADE,
          FOLLOWDISEASEKINDDICT
        );
      }
    }

    public static final class GtdsTherapy {
      private static final String BASE_URL = Extension.BASE_URL + "/gtdsTherapy";
      public static final String TUMOR = BASE_URL + "/tumor";

      @Nonnull
      public static Collection<String> getAllDomains() {
        return singletonList(
          TUMOR
        );
      }

      private GtdsTherapy() {
      }

      public static final class Surgery {
        private static final String BASE_URL = GtdsTherapy.BASE_URL + "/surgery";
        public static final String INTENTIONDICT = BASE_URL + "/intentionDict";
        public static final String CONCEPTID = BASE_URL + "/conceptId";
        public static final String THERAPYSTEP = BASE_URL + "/therapyStep";
        public static final String DATEACCURACYDICT = BASE_URL + "/dateAccuracyDict";
        public static final String ACCOMPLISHEDBYTEXT = BASE_URL + "/accomplishedByText";
        public static final String CAPTUREFINISHSTATEDICT = BASE_URL + "/captureFinishStateDict";
        public static final String OPDESCRIPTION = BASE_URL + "/opDescription";
        public static final String OPTEXT = BASE_URL + "/opText";
        public static final String RCLASSIFICATIONDICT = BASE_URL + "/rClassificationDict";
        public static final String LYMPHNODESEXAMINATED = BASE_URL + "/lymphNodesExaminated";
        public static final String LYMPHNODESINFESTED = BASE_URL + "/lymphNodesInfested";
        public static final String LYMPHNODE_1_EXAMINATED = BASE_URL + "/lymphNode_1_Examinated";
        public static final String LYMPHNODE_1_INFESTED = BASE_URL + "/lymphNode_1_Infested";
        public static final String LYMPHNODE_2_EXAMINATED = BASE_URL + "/lymphNode_2_Examinated";
        public static final String LYMPHNODE_2_INFESTED = BASE_URL + "/lymphNode_2_Infested";
        public static final String LYMPHNODE_3_EXAMINATED = BASE_URL + "/lymphNode_3_Examinated";
        public static final String LYMPHNODE_3_INFESTED = BASE_URL + "/lymphNode_3_Infested";
        public static final String LYMPHNODE_4_EXAMINATED = BASE_URL + "/lymphNode_4_Examinated";
        public static final String LYMPHNODE_4_INFESTED = BASE_URL + "/lymphNode_4_Infested";
        public static final String SENTINELSEXAMINATED = BASE_URL + "/sentinelsExaminated";
        public static final String SENTINELSINFESTED = BASE_URL + "/sentinelsInfested";
        public static final String BUILDINGDATE = BASE_URL + "/buildingDate";
        public static final String COMPLICATIONSDICT = BASE_URL + "/complicationsDict";
        public static final String TARGETPRIMARYTUMOURDICT = BASE_URL + "/targetPrimaryTumourDict";
        public static final String TARGETLYMPHNODEDICT = BASE_URL + "/targetLymphnodeDict";
        public static final String TARGETMETASTASISDICT = BASE_URL + "/targetMetastasisDict";
        public static final String RESECTIONDICT = BASE_URL + "/resectionDict";
        public static final String TARGETCOMPLICATIONDICT = BASE_URL + "/targetComplicationDict";
        public static final String TARGETOTHERDICT = BASE_URL + "/targetOtherDict";
        public static final String RESIDUALLOCALISATIONDICT = BASE_URL + "/residualLocalisationDict";
        public static final String RCLASSIFICATIONSUFFIX = BASE_URL + "/rClassificationSuffix";
        public static final String RCLASSIFICATIONLOCALDICT = BASE_URL + "/rClassificationLocalDict";
        public static final String URGENCYDICT = BASE_URL + "/urgencyDict";
        public static final String SUCCESSDICT = BASE_URL + "/successDict";
        public static final String LARGESTDIAMETER = BASE_URL + "/largestDiameter";
        public static final String DISTANCERESECTION = BASE_URL + "/distanceResection";
        public static final String OPERATEUR1TEXT = BASE_URL + "/operateur1Text";
        public static final String OPERATEUR2TEXT = BASE_URL + "/operateur2Text";
        public static final String ASASCORE = BASE_URL + "/asaScore";
        public static final String SURGICALACCESS = BASE_URL + "/surgicalAccess";

        private Surgery() {
        }

        @Nonnull
        public static Collection<String> getAllDomains() {
          return asList(
            INTENTIONDICT,
            CONCEPTID,
            THERAPYSTEP,
            DATEACCURACYDICT,
            ACCOMPLISHEDBYTEXT,
            CAPTUREFINISHSTATEDICT,
            OPDESCRIPTION,
            OPTEXT,
            RCLASSIFICATIONDICT,
            LYMPHNODESEXAMINATED,
            LYMPHNODESINFESTED,
            LYMPHNODE_1_EXAMINATED,
            LYMPHNODE_1_INFESTED,
            LYMPHNODE_2_EXAMINATED,
            LYMPHNODE_2_INFESTED,
            LYMPHNODE_3_EXAMINATED,
            LYMPHNODE_3_INFESTED,
            LYMPHNODE_4_EXAMINATED,
            LYMPHNODE_4_INFESTED,
            SENTINELSEXAMINATED,
            SENTINELSINFESTED,
            BUILDINGDATE,
            COMPLICATIONSDICT,
            TARGETPRIMARYTUMOURDICT,
            TARGETLYMPHNODEDICT,
            TARGETMETASTASISDICT,
            RESECTIONDICT,
            TARGETCOMPLICATIONDICT,
            TARGETOTHERDICT,
            RESIDUALLOCALISATIONDICT,
            RCLASSIFICATIONSUFFIX,
            RCLASSIFICATIONLOCALDICT,
            URGENCYDICT,
            SUCCESSDICT,
            LARGESTDIAMETER,
            DISTANCERESECTION,
            OPERATEUR1TEXT,
            OPERATEUR2TEXT,
            ASASCORE,
            SURGICALACCESS
          );
        }
      }

      public static final class RadiationTherapy {
        private static final String BASE_URL = GtdsTherapy.BASE_URL + "/radiationTherapy";
        public static final String INTENTIONDICT = BASE_URL + "/intentionDict";
        public static final String CONCEPTID = BASE_URL + "/conceptId";
        public static final String THERAPYSTEP = BASE_URL + "/therapyStep";
        public static final String DATEACCURACYDICT = BASE_URL + "/dateAccuracyDict";
        public static final String ACCOMPLISHEDBYTEXT = BASE_URL + "/accomplishedByText";
        public static final String CAPTUREFINISHSTATEDICT = BASE_URL + "/captureFinishStateDict";
        public static final String COMMENTS = BASE_URL + "/comments";
        public static final String FINALSTATEDICT = BASE_URL + "/finalStateDict";
        public static final String ASSESSMENT = BASE_URL + "/assessment";
        public static final String ADVERSEEFFECTSDICT = BASE_URL + "/adverseEffectsDict";
        public static final String RADIATIONPATTERNID = BASE_URL + "/radiationPatternId";
        public static final String THERAPYKINDDICT = BASE_URL + "/therapyKindDict";
        public static final String RADIOCHEMODICT = BASE_URL + "/radioChemoDict";
        public static final String TARGETPRIMARYTUMOURDICT = BASE_URL + "/targetPrimaryTumourDict";
        public static final String TARGETLYMPHNODEDICT = BASE_URL + "/targetLymphnodeDict";
        public static final String TARGETMETASTASISDICT = BASE_URL + "/targetMetastasisDict";
        public static final String TARGETOTHERDICT = BASE_URL + "/targetOtherDict";

        private RadiationTherapy() {
        }

        @Nonnull
        public static Collection<String> getAllDomains() {
          return asList(
            INTENTIONDICT,
            CONCEPTID,
            THERAPYSTEP,
            DATEACCURACYDICT,
            ACCOMPLISHEDBYTEXT,
            CAPTUREFINISHSTATEDICT,
            COMMENTS,
            FINALSTATEDICT,
            ASSESSMENT,
            ADVERSEEFFECTSDICT,
            RADIATIONPATTERNID,
            THERAPYKINDDICT,
            RADIOCHEMODICT,
            TARGETPRIMARYTUMOURDICT,
            TARGETLYMPHNODEDICT,
            TARGETMETASTASISDICT,
            TARGETOTHERDICT
          );
        }
      }

      public static final class SystemTherapy {
        private static final String BASE_URL = GtdsTherapy.BASE_URL + "/systemTherapy";
        public static final String INTENTIONDICT = BASE_URL + "/intentionDict";
        public static final String CONCEPTID = BASE_URL + "/conceptId";
        public static final String THERAPYSTEP = BASE_URL + "/therapyStep";
        public static final String DATEACCURACYDICT = BASE_URL + "/dateAccuracyDict";
        public static final String ACCOMPLISHEDBYTEXT = BASE_URL + "/accomplishedByText";
        public static final String CAPTUREFINISHSTATEDICT = BASE_URL + "/captureFinishStateDict";
        public static final String ASSESSMENT = BASE_URL + "/assessment";
        public static final String PROTOCOLTYPEDICT = BASE_URL + "/protocolTypeDict";
        public static final String PROTOCOLID = BASE_URL + "/protocolId";
        public static final String FINALSTATEDICT = BASE_URL + "/finalStateDict";
        public static final String PLANNEDDURATION = BASE_URL + "/plannedDuration";
        public static final String THERAPYKINDDICT = BASE_URL + "/therapyKindDict";
        public static final String THERAPYTYPEDICT = BASE_URL + "/therapyTypeDict";
        public static final String ADVERSEEFFECTSDICT = BASE_URL + "/adverseEffectsDict";
        public static final String RADIOCHEMODICT = BASE_URL + "/radioChemoDict";
        public static final String CYCLECOUNT = BASE_URL + "/cycleCount";
        public static final String COMMENTS = BASE_URL + "/comments";
        public static final String DESCRIPTION = BASE_URL + "/description";

        private SystemTherapy() {
        }

        @Nonnull
        public static Collection<String> getAllDomains() {
          return asList(
            INTENTIONDICT,
            CONCEPTID,
            THERAPYSTEP,
            DATEACCURACYDICT,
            ACCOMPLISHEDBYTEXT,
            CAPTUREFINISHSTATEDICT,
            ASSESSMENT,
            PROTOCOLTYPEDICT,
            PROTOCOLID,
            FINALSTATEDICT,
            PLANNEDDURATION,
            THERAPYKINDDICT,
            THERAPYTYPEDICT,
            ADVERSEEFFECTSDICT,
            RADIOCHEMODICT,
            CYCLECOUNT,
            COMMENTS,
            DESCRIPTION
          );
        }
      }
    }

    public static final class RadiationComponent {
      private static final String BASE_URL = Extension.BASE_URL + "/radiationComponent";
      public static final String DATEACCURACYDICT = BASE_URL + "/dateAccuracyDict";
      public static final String COMMENTS = BASE_URL + "/comments";
      public static final String ASSESSMENT = BASE_URL + "/assessment";
      public static final String IRRADIATIONDAYS = BASE_URL + "/irradiationDays";
      public static final String FRACTIONS = BASE_URL + "/fractions";
      public static final String APPLICATIONKINDDICT = BASE_URL + "/applicationKindDict";
      public static final String APPLICATIONTECH = BASE_URL + "/applicationTech";
      public static final String APPLICATIONTECHTEXT = BASE_URL + "/applicationTechText";
      public static final String SINGLEDOSE = BASE_URL + "/singleDose";
      public static final String COMPLETEDOSE = BASE_URL + "/completeDose";
      public static final String UNITDICT = BASE_URL + "/unitDict";
      public static final String ICRUREFDICT = BASE_URL + "/icruRefDict";
      public static final String VOLTAGE = BASE_URL + "/voltage";
      public static final String VOLTAGEDIMENSION = BASE_URL + "/voltageDimension";
      public static final String VOLTAGETEXT = BASE_URL + "/voltageText";
      public static final String REFERENCE = BASE_URL + "/reference";
      public static final String REFERENCEDIMENSION = BASE_URL + "/referenceDimension";
      public static final String REFERENCETEXT = BASE_URL + "/referenceText";
      public static final String MODIFICATIONDATE = BASE_URL + "/modificationDate";
      public static final String MODIFICATIONREASON = BASE_URL + "/modificationReason";
      public static final String INTERRUPTDICT = BASE_URL + "/interruptDict";
      public static final String INTERRUPTREASONDICT = BASE_URL + "/interruptReasonDict";
      public static final String INTERRUPTDURATION = BASE_URL + "/interruptDuration";
      public static final String FINALSTATEDICT = BASE_URL + "/finalStateDict";
      public static final String RADIATIONKINDDICT = BASE_URL + "/radiationKindDict";

      private RadiationComponent() {
      }

      @Nonnull
      public static Collection<String> getAllDomains() {
        return asList(
          DATEACCURACYDICT,
          COMMENTS,
          ASSESSMENT,
          IRRADIATIONDAYS,
          FRACTIONS,
          APPLICATIONKINDDICT,
          APPLICATIONTECH,
          APPLICATIONTECHTEXT,
          SINGLEDOSE,
          COMPLETEDOSE,
          UNITDICT,
          ICRUREFDICT,
          VOLTAGE,
          VOLTAGEDIMENSION,
          VOLTAGETEXT,
          REFERENCE,
          REFERENCEDIMENSION,
          REFERENCETEXT,
          MODIFICATIONDATE,
          MODIFICATIONREASON,
          INTERRUPTDICT,
          INTERRUPTREASONDICT,
          INTERRUPTDURATION,
          FINALSTATEDICT,
          RADIATIONKINDDICT
        );
      }
    }

    public static final class SurgeryComponent {
      private static final String BASE_URL = Extension.BASE_URL + "/surgeryComponent";
      public static final String COMPLICATIONS = BASE_URL + "/complications";

      private SurgeryComponent() {
      }

      @Nonnull
      public static Collection<String> getAllDomains() {
        return singletonList(
          COMPLICATIONS
        );
      }
    }

    public static final class PreexistingIllness {
      private static final String BASE_URL = Extension.BASE_URL + "/preexistingIllness";
      public static final String VERSION = BASE_URL + "/version";

      private PreexistingIllness() {
      }

      @Nonnull
      public static Collection<String> getAllDomains() {
        return singletonList(VERSION);
      }
    }

    public static final class DeathCause {
      private static final String BASE_URL = Extension.BASE_URL + "/deathCause";
      public static final String QUALIFICATOR = BASE_URL + "/qualificator";

      private DeathCause() {
      }

      @Nonnull
      public static Collection<String> getAllDomains() {
        return singletonList(QUALIFICATOR);
      }
    }

    public static final class Patient {
      private static final String PATIENT_BASE_URL = Extension.BASE_URL + "/patient";
      public static final String ETHNICITIES = PATIENT_BASE_URL + "/ethnicities";

      private Patient() {/* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        final List<String> domains = new ArrayList<>();
        domains.add(ETHNICITIES);
        domains.addAll(Ethnicities.getAllDomains());
        domains.addAll(Name.getAllDomains());
        return domains;
      }

      public static final class Ethnicities {
        public static final String ETHNICITY = ETHNICITIES + "/ethnicity";
        public static final String ETHNICITY_OVERRIDE = ETHNICITIES + "/override";

        private Ethnicities() {
        }

        @Nonnull
        public static List<String> getAllDomains() {
          return asList(ETHNICITY, ETHNICITY_OVERRIDE);
        }

        @Nonnull
        public static Map<String, String> getSubExtensions() {
          return getExtensionMap(getAllDomains());
        }
      }

      public static final class Name {
        private static final String NAME = PATIENT_BASE_URL + "/name";
        public static final String TITLE = NAME + "/title";
        public static final String AFFIX = NAME + "/affix";

        private Name() {
        }

        @Nonnull
        public static List<String> getAllDomains() {
          return asList(AFFIX, TITLE);
        }
      }
    }

    public static final class PatientTransfer {
      private static final String PATIENT_TRANSFER_BASE_URL = Extension.BASE_URL + "/patientTransfer";

      public static final String EPISODE = PATIENT_TRANSFER_BASE_URL + "/episode";
      public static final String TRANSFER_DATE = PATIENT_TRANSFER_BASE_URL + "/transferDate";
      public static final String ATTENDING_DOCTOR = PATIENT_TRANSFER_BASE_URL + "/attendingDoctor";

      public static final String CURRENT_LOCATION = PATIENT_TRANSFER_BASE_URL + "/currentLocation";
      public static final String PRIOR_LOCATION = PATIENT_TRANSFER_BASE_URL + "/priorLocation";

      private PatientTransfer() {/* hide constructor */}

      public static List<String> getAllDomains() {
        final List<String> domains = new ArrayList<>();
        domains.addAll(asList(CURRENT_LOCATION, PRIOR_LOCATION, EPISODE, TRANSFER_DATE, ATTENDING_DOCTOR));
        domains.addAll(Location.getAllDomains());
        return domains;
      }

      public static final class Location {
        private static final String LOCATION_BASE_URL = PATIENT_TRANSFER_BASE_URL + "/location";

        public static final String HABITATION = LOCATION_BASE_URL + "/habitation";
        public static final String MED_DEPARTMENT = LOCATION_BASE_URL + "/medDepartment";
        public static final String ROOM = LOCATION_BASE_URL + "/room";
        public static final String BED = LOCATION_BASE_URL + "/bed";
        public static final String FLOOR = LOCATION_BASE_URL + "/floor";

        private Location() {/* hide constructor */}

        public static List<String> getAllDomains() {
          return asList(HABITATION, MED_DEPARTMENT, ROOM, BED, FLOOR);
        }
      }
    }

    public static final class Medication {
      private static final String MEDICATION_BASE_URL = Extension.BASE_URL + "/medication";
      public static final String DOSE_VALUE = MEDICATION_BASE_URL + "/doseValue";
      public static final String PRESCRIBER = MEDICATION_BASE_URL + "/prescribedBy";
      public static final String TRANSCRIPTIONIST = MEDICATION_BASE_URL + "/transcriptionist";
      public static final String TYPE = MEDICATION_BASE_URL + "/type";
      public static final String ORDINANCE_RELEASE_METHOD = MEDICATION_BASE_URL + "/ordinanceReleaseMethod";
      public static final String IS_PRESCRIPTION = MEDICATION_BASE_URL + "/isPrescription";
      public static final String FON = MEDICATION_BASE_URL + "/fillerOrderNumber";
      public static final String PON = MEDICATION_BASE_URL + "/placerOrderNumber";
      public static final String RESULTDATE = MEDICATION_BASE_URL + "/resultDate";
      private static final String IDENTIFIER = MEDICATION_BASE_URL + "/identifier";

      private Medication() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return asList(DOSE_VALUE, PRESCRIBER, TRANSCRIPTIONIST, TYPE, ORDINANCE_RELEASE_METHOD, IS_PRESCRIPTION, FON, PON, RESULTDATE,
                      Identifier.FON, Identifier.MEDICATION_CODE);
      }

      public static final class Identifier {
        public static final String FON = IDENTIFIER + "/fillerOrderNumber";
        public static final String MEDICATION_CODE = IDENTIFIER + "/code";
      }
    }

    public static final class Study {
      private static final String STUDY_BASE_URL = Extension.BASE_URL + "/study";
      public static final String PHASES = STUDY_BASE_URL + "/phases";
      public static final String VISITS = STUDY_BASE_URL + "/visits";
      public static final String STATUS = STUDY_BASE_URL + "/status";
      public static final String STUDY_REGISTER_STATUS = STUDY_BASE_URL + "/studyRegisterStatus";

      private Study() {/* hide constructor */}

      public static List<String> getAllDomains() {
        final List<String> domains = new ArrayList<>();
        domains.addAll(asList(PHASES, VISITS, STATUS, STUDY_REGISTER_STATUS));
        domains.addAll(Visits.getAllDomains());
        domains.addAll(Phases.getAllDomains());
        domains.addAll(Schedule.getAllDomains());
        return domains;
      }

      public static final class Visits {
        public static final String VISIT = VISITS + "/visit";

        private Visits() {
        }

        public static List<String> getAllDomains() {
          return asList(VISIT, Visit.VISIT_NAME, Visit.VISIT_COPYVISITS);
        }

        public static final class Visit {
          public static final String VISIT_NAME = VISIT + NAME;
          public static final String VISIT_COPYVISITS = VISIT + "/copyVisits";

          private Visit() {
          }
        }
      }

      public static final class Phases {
        public static final String PHASE = PHASES + "/phase";

        private Phases() {
        }

        public static List<String> getAllDomains() {
          return asList(PHASE, Phase.PHASE_NAME, Phase.PHASE_DESCRIPTION);
        }
      }

      public static final class Phase {
        public static final String PHASE_NAME = Phases.PHASE + NAME;
        public static final String PHASE_DESCRIPTION = Phases.PHASE + "/description";

        private Phase() {
        }
      }

      public static class Schedule {
        public static final String SCHEDULE_BASE_URL = STUDY_BASE_URL + "/schedule";
        public static final String MIN = SCHEDULE_BASE_URL + "/min";
        public static final String MAX = SCHEDULE_BASE_URL + "/max";
        public static final String UNDEFINED = SCHEDULE_BASE_URL + "/undefined";

        public static List<String> getAllDomains() {
          return asList(SCHEDULE_BASE_URL, MIN, MAX, UNDEFINED);
        }

        public static Map<String, String> getSubExtensions() {
          return getExtensionMap(getAllDomains());
        }

        private Schedule() {
        }
      }
    }

    public static final class Survey {
      private static final String SURVEY_BASE_URL = Extension.BASE_URL + "/survey";
      public static final String CYCLES = SURVEY_BASE_URL + "/cycles";
      public static final String CYCLE = CYCLES + "/cycle";
      public static final String CYCLE_NAME = CYCLE + NAME;
      public static final String CYCLE_DESCRIPTION = CYCLE + "/description";

      public static final String FORMS = SURVEY_BASE_URL + "/forms";
      public static final String FORM = FORMS + "/form";
      public static final String FORM_NAME = FORM + NAME;

      public static final String DUE_DATE = SURVEY_BASE_URL + "/dueDate";

      private Survey() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return asList(CYCLES, CYCLE, CYCLE_NAME, CYCLE_DESCRIPTION, FORMS, FORM, FORM_NAME, DUE_DATE);
      }
    }

    public static final class StudyVisitItem {
      private static final String SVI_BASE_URL = Extension.BASE_URL + "/studyVisitItem";
      public static final String APPROVAL_STATE = SVI_BASE_URL + "/approvalState";

      private StudyVisitItem() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return singletonList(APPROVAL_STATE);
      }
    }

    public static final class Sprec {
      public static final String USE_SPREC = SPREC + "/useSprec";
      public static final String SPREC_CODE = SPREC + "/sprecCode";
      public static final String SPREC_TISSUE_COLLECTION_TYPE = SPREC + "/tissueCollectionType";
      public static final String WARM_ISCH_TIME = SPREC + "/warmIschTime";
      public static final String WARM_ISCH_TIME_DATE = SPREC + "/warmIschTimeDate";
      public static final String COLD_ISCH_TIME = SPREC + "/coldIschTime";
      public static final String COLD_ISCH_TIME_DATE = SPREC + "/coldIschTimeDate";
      public static final String STOCK_TYPE = SPREC + "/stockType";
      public static final String SPREC_FIXATION_TIME = SPREC + "/fixationTime";
      public static final String SPREC_FIXATION_TIME_DATE = SPREC + "/fixationTimeDate";
      public static final String SPREC_PRIMARY_SAMPLE_CONTAINER = SPREC + "/primarySampleContainer";
      public static final String SPREC_PRE_CENTRIFUGATION_DELAY = SPREC + "/preCentrifugationDelay";
      public static final String SPREC_PRE_CENTRIFUGATION_DELAY_DATE = SPREC + "/preCentrifugationDelayDate";
      public static final String SPREC_POST_CENTRIFUGATION_DELAY = SPREC + "/postCentrifugationDelay";
      public static final String SPREC_POST_CENTRIFUGATION_DELAY_DATE = SPREC + "/postCentrifugationDelayDate";
      public static final String STOCK_PROCESSING = SPREC + "/stockProcessing";
      public static final String STOCK_PROCESSING_DATE = SPREC + "/stockProcessingDate";
      public static final String SECOND_PROCESSING = SPREC + "/secondProcessing";
      public static final String SECOND_PROCESSING_DATE = SPREC + "/secondProcessingDate";

      private Sprec() {/* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        return asList(USE_SPREC,
                      SPREC_CODE,
                      SPREC_TISSUE_COLLECTION_TYPE,
                      WARM_ISCH_TIME,
                      WARM_ISCH_TIME_DATE,
                      COLD_ISCH_TIME,
                      COLD_ISCH_TIME_DATE,
                      STOCK_TYPE,
                      SPREC_FIXATION_TIME,
                      SPREC_FIXATION_TIME_DATE,
                      SPREC_PRIMARY_SAMPLE_CONTAINER,
                      SPREC_PRE_CENTRIFUGATION_DELAY,
                      SPREC_PRE_CENTRIFUGATION_DELAY_DATE,
                      SPREC_POST_CENTRIFUGATION_DELAY,
                      SPREC_POST_CENTRIFUGATION_DELAY_DATE,
                      STOCK_PROCESSING,
                      STOCK_PROCESSING_DATE,
                      SECOND_PROCESSING,
                      SECOND_PROCESSING_DATE);
      }

      public static Map<String, String> getSubExtensions() {
        return getExtensionMap(getAllDomains());
      }
    }

    public static final class CrfTemplate {
      private static final String CRFTEMPLATE_BASE_URL = Extension.BASE_URL + "/crfTemplate";
      public static final String MULTIPLE_USE_URL = CRFTEMPLATE_BASE_URL + "/multipleUse";

      private CrfTemplate() {/* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        final List<String> domains = new ArrayList<>();
        domains.add(MULTIPLE_USE_URL);
        domains.addAll(Section.getAllDomains());
        return domains;
      }

      public static final class Section {
        private static final String SECTION_BASE_URL = CRFTEMPLATE_BASE_URL + "/section";
        public static final String INDEX = SECTION_BASE_URL + "/index";
        public static final String TYPE = SECTION_BASE_URL + "/type";
        public static final String ALIGNMENT = SECTION_BASE_URL + "/orientation";
        public static final String ROW = SECTION_BASE_URL + "/row";
        public static final String LOWER_COLUMN = SECTION_BASE_URL + "/lowerColumn";
        public static final String UPPER_COLUMN = SECTION_BASE_URL + "/upperColumn";

        private Section() {/* hide constructor */}

        @Nonnull
        public static List<String> getAllDomains() {
          final List<String> domains = new ArrayList<>();
          domains.addAll(asList(INDEX, TYPE, ALIGNMENT, ROW, LOWER_COLUMN, UPPER_COLUMN));
          domains.addAll(Field.getAllDomains());
          return domains;
        }

        public static final class Field {
          private static final String FIELD_BASE_URL = SECTION_BASE_URL + "/field";
          public static final String CRFFIELDTYPE = FIELD_BASE_URL + "/crfFieldType";
          public static final String TOOLTIP = FIELD_BASE_URL + "/toolTip";
          public static final String ROW = FIELD_BASE_URL + "/row";
          public static final String LOWER_COLUMN = FIELD_BASE_URL + "/lowerColumn";
          public static final String UPPER_COLUMN = FIELD_BASE_URL + "/upperColumn";
          public static final String DIRECTION = FIELD_BASE_URL + "/direction";
          public static final String LENGTH = FIELD_BASE_URL + "/length";
          public static final String GRADUATIONS = FIELD_BASE_URL + "/graduations";
          public static final String MIN_VALUE_DESC = FIELD_BASE_URL + "/minValueDescription";
          public static final String MAX_VALUE_DESC = FIELD_BASE_URL + "/maxValueDescription";

          private Field() {/* hide constructor */}

          @Nonnull
          public static List<String> getAllDomains() {
            return asList(CRFFIELDTYPE, TOOLTIP, ROW, LOWER_COLUMN, UPPER_COLUMN, DIRECTION, LENGTH, GRADUATIONS, MIN_VALUE_DESC, MAX_VALUE_DESC);
          }
        }
      }
    }

    public static final class Crf {
      private static final String CRF_BASE_URL = Extension.BASE_URL + "/crf";
      public static final String CREATION_DATE = CRF_BASE_URL + "/creationDate";

      private Crf() {/* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        final List<String> domains = new ArrayList<>(singletonList(CREATION_DATE));
        domains.addAll(CrfItem.getAllDomains());
        return domains;
      }

      public static final class CrfItem {
        private static final String CRF_ITEM_BASE_URL = CRF_BASE_URL + "/item";
        public static final String VALUE_INDEX = CRF_ITEM_BASE_URL + "/valueIndex";

        private CrfItem() {/* hide constructor */}

        @Nonnull
        public static List<String> getAllDomains() {
          return singletonList(VALUE_INDEX);
        }
      }
    }

    public static final class LaborValue {
      private static final String LABORVALUE_BASE_URL = Extension.BASE_URL + "/laborValue";
      public static final String LABORVALUETYPE = LABORVALUE_BASE_URL + "/laborValueType";
      public static final String MIN = LABORVALUE_BASE_URL + "/min";
      public static final String MAX = LABORVALUE_BASE_URL + "/max";
      public static final String DATE_PRECISION = LABORVALUE_BASE_URL + "/datePrecision";
      public static final String OBSERVATION_METHODS = LABORVALUE_BASE_URL + "/observationMethods";
      public static final String OBSERVATION_METHOD = OBSERVATION_METHODS + "/observationMethod";
      public static final String IS_DEVIANT_VALUE = LABORVALUE_BASE_URL + "/deviantValue";
      public static final String CHOICE_TYPE = LABORVALUE_BASE_URL + "/choiceType";
      public static final String UPPER_VALUE = LABORVALUE_BASE_URL + "/upperValue";
      public static final String LOWER_VALUE = LABORVALUE_BASE_URL + "/lowerValue";
      public static final String UPPER_PRECISION = LABORVALUE_BASE_URL + "/upperPrecision";
      public static final String LOWER_PRECISION = LABORVALUE_BASE_URL + "/lowerPrecision";
      public static final String UNIT = LABORVALUE_BASE_URL + "/unit";
      public static final String FILE_VALUE = LABORVALUE_BASE_URL + "/fileValue";
      public static final String VALUE_INDEX = LABORVALUE_BASE_URL + "/valueIndex";

      private LaborValue() {/* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        final List<String> domains = new ArrayList<>(
          asList(LABORVALUETYPE, MIN, MAX, DATE_PRECISION, OBSERVATION_METHOD, OBSERVATION_METHODS, IS_DEVIANT_VALUE, CHOICE_TYPE, UPPER_VALUE,
                 LOWER_VALUE, UPPER_PRECISION, LOWER_PRECISION, UNIT, FILE_VALUE, VALUE_INDEX));
        domains.addAll(Slider.getAllDomains());
        return domains;
      }

      public static final class Slider {
        private static final String SLIDER_ITEM_BASE_URL = LABORVALUE_BASE_URL + "/slider";
        public static final String ALIGNMENT = SLIDER_ITEM_BASE_URL + "/alignment";

        private Slider() {/* hide constructor */}

        @Nonnull
        public static List<String> getAllDomains() {
          return singletonList(ALIGNMENT);
        }
      }
    }

    public static final class Hotline {
      private static final String HOTLINE_BASE_URL = Extension.BASE_URL + "/hotline";
      public static final String IS_APP = HOTLINE_BASE_URL + "/app";

      private Hotline() {/* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        return singletonList(IS_APP);
      }
    }

    public static final class Consent {

      private static final String CONSENT_BASE_URL = Extension.BASE_URL + "/consent";
      public static final String USER_INFO_FILE = CONSENT_BASE_URL + "/userInfoFile";
      public static final String NOTES = CONSENT_BASE_URL + "/notes";
      public static final String FILE = CONSENT_BASE_URL + "/file";
      public static final String REVOCATION = CONSENT_BASE_URL + "/revocation";

      private Consent() {/* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        return asList(USER_INFO_FILE, NOTES, FILE, REVOCATION);
      }

      public static final class Revocation {
        public static final String REVOCATION_PARTLY = Consent.REVOCATION + "/partlyRevoked";
        public static final String REVOCATION_FILE = Consent.REVOCATION + "/file";
        public static final String REVOCATION_DATE = Consent.REVOCATION + "/date";
        public static final String REVOCATION_NOTES = Consent.REVOCATION + "/notes";

        private Revocation() {/* hide constructor */}

        @Nonnull
        public static List<String> getAllDomains() {
          return asList(REVOCATION_PARTLY, REVOCATION_FILE, REVOCATION_DATE, REVOCATION_NOTES);
        }
      }
    }

    public static final class Document {
      private static final String BASE_URL = Extension.BASE_URL + "/document";
      public static final String DESCRIPTION = BASE_URL + "/description";
      public static final String KEYWORDS = BASE_URL + "/keywords";
      public static final String PRODUCER_ORDER_NUMBER = BASE_URL + "/producerOrderNumber";
      public static final String STATUS = BASE_URL + "/status";

      public static final String PATIENT_VISIBILITY = BASE_URL + "/patientVisibility";

      private Document() {/* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        return asList(DESCRIPTION, KEYWORDS, PRODUCER_ORDER_NUMBER, STATUS, PATIENT_VISIBILITY);
      }
    }

    public static final class Calendar {
      private static final String CALENDAR_BASE_URL = Extension.BASE_URL + "/calendar";
      public static final String ATTACHMENT = CALENDAR_BASE_URL + "/attachment";
      public static final String ALL_DAY = CALENDAR_BASE_URL + "/allDay";
      public static final String DEPARTMENT = CALENDAR_BASE_URL + "/department";
      public static final String STAY_TYPE = CALENDAR_BASE_URL + "/stayType";
      public static final String DRG = CALENDAR_BASE_URL + "/drg";
      public static final String LOCATION = CALENDAR_BASE_URL + "/location";
      public static final String RESOURCE = CALENDAR_BASE_URL + "/resource";
      public static final String VISIBLE = CALENDAR_BASE_URL + "/visible";
      public static final String STUDY_CENTER = CALENDAR_BASE_URL + "/studyCenter";
      public static final String STUDY_VISIT_TEMPLATE = CALENDAR_BASE_URL + "/studyVisitTemplate";
      public static final String INVITATION_STATUS = CALENDAR_BASE_URL + "/invitationStatus";

      private Calendar() { /* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        final List<String> domains = new ArrayList<>();
        domains.addAll(asList(ATTACHMENT, ALL_DAY,
                              DEPARTMENT, DRG, STAY_TYPE, LOCATION, RESOURCE, VISIBLE,
                              STUDY_CENTER, STUDY_VISIT_TEMPLATE, INVITATION_STATUS));
        domains.addAll(Attachment.getAllDomains());
        domains.addAll(Recurrence.getAllDomains());
        return domains;
      }

      public static final class Attachment {
        public static final String ATTACHMENT_PATIENT = ATTACHMENT + "/patient";
        public static final String ATTACHMENT_CRF = ATTACHMENT + "/crf";
        public static final String ATTACHMENT_SAMPLE = ATTACHMENT + "/sample";
        public static final String ATTACHMENT_STUDY = ATTACHMENT + "/study";
        public static final String ATTACHMENT_STUDYMEMBER = ATTACHMENT + "/studyMember";

        private Attachment() { /* hide constructor */}

        @Nonnull
        public static List<String> getAllDomains() {
          return asList(ATTACHMENT_PATIENT, ATTACHMENT_CRF, ATTACHMENT_SAMPLE,
                        ATTACHMENT_STUDY, ATTACHMENT_STUDYMEMBER);
        }
      }

      public static final class Recurrence {
        public static final String BASE = CALENDAR_BASE_URL + "/recurrence";
        public static final String RECURRENCE_EXPRESSION = BASE + "/expression";
        public static final String RECURRENCE_ENDDATE = BASE + "/endDate";
        public static final String RECURRENCE_COUNT = BASE + "/count";

        private Recurrence() { /* hide constructor */}

        @Nonnull
        public static List<String> getAllDomains() {
          return asList(BASE, RECURRENCE_EXPRESSION, RECURRENCE_ENDDATE, RECURRENCE_COUNT);
        }
      }
    }

    public static final class Task {
      private static final String TASK_BASE_URL = Extension.BASE_URL + "/task";
      public static final String DESCRIPTION = TASK_BASE_URL + "/description";
      public static final String CAL_EVENT = TASK_BASE_URL + "/calendarEvent";
      public static final String NOTIFY_ON_RESOLVE = TASK_BASE_URL + "/notifyOnResolve";
      public static final String ASSIGNEE_GROUP = TASK_BASE_URL + "/assigneeGroup";

      private Task() { /* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        return asList(DESCRIPTION, CAL_EVENT, NOTIFY_ON_RESOLVE, ASSIGNEE_GROUP);
      }
    }

    public static final class ServiceRequest {
      private static final String SERVICE_REQUEST_BASE_URL = Extension.BASE_URL + "/serviceRequest";
      public static final String LABOR_MAPPINGS = SERVICE_REQUEST_BASE_URL + "/laborMappings";
      public static final String STATUS = SERVICE_REQUEST_BASE_URL + "/status";
      public static final String REQUESTER = SERVICE_REQUEST_BASE_URL + "/requester";

      private ServiceRequest() { /* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        return asList(LABOR_MAPPINGS, LaborMappings.LABOR_MAPPING, STATUS, Status.CURRENT_STATUS, Status.LAST_STATUS_TRANSITION, REQUESTER);
      }

      public static final class LaborMappings {
        public static final String LABOR_MAPPING = LABOR_MAPPINGS + "/laborMapping";

        private LaborMappings() {
        }

        @Nonnull
        public static Map<String, String> getSubExtensions() {
          return getExtensionMap(singletonList(LABOR_MAPPING));
        }
      }

      public static final class Status {
        public static final String CURRENT_STATUS = STATUS + "/currentStatus";
        public static final String LAST_STATUS_TRANSITION = STATUS + "/lastStatusTransition";

        private Status() {
        }

        @Nonnull
        public static Map<String, String> getSubExtensions() {
          return getExtensionMap(asList(CURRENT_STATUS, LAST_STATUS_TRANSITION));
        }
      }
    }

    public static final class LaborMapping {
      public static final String LABOR_MAPPING_TYPE = Extension.LABOR_MAPPING + "/type";
      public static final String RELATED_REFERENCE = Extension.LABOR_MAPPING + "/relatedReference";
      public static final String PATIENT = Extension.LABOR_MAPPING + "/patient";
      public static final String ENCOUNTER = Extension.LABOR_MAPPING + "/encounter";
      public static final String CREATE_PROFILE = Extension.LABOR_MAPPING + "/createProfile";
      public static final String INCREMENT_LABORMETHOD_VERSION = Extension.LABOR_MAPPING + "/incrementProfileVersion";

      private LaborMapping() { /* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        return asList(LABOR_MAPPING_TYPE, RELATED_REFERENCE, PATIENT, ENCOUNTER, CREATE_PROFILE, INCREMENT_LABORMETHOD_VERSION);
      }
    }

    public static final class SampleLocation {
      public static final String PATH = SAMPLE_LOCATION + "/path";
      public static final String SCHEMA = SAMPLE_LOCATION + "/schema";
      public static final String FILL_COUNT = SAMPLE_LOCATION + "/fillCount";
      public static final String TEMPERATURE = SAMPLE_LOCATION + "/temperature";
      public static final String RELOCATIONDATE = SAMPLE_LOCATION + "/relocationDate";

      private SampleLocation() { /* hide constructor */}

      @Nonnull
      public static List<String> getAllDomains() {
        final List<String> domains = new ArrayList<>();
        domains.add(PATH);
        domains.add(SCHEMA);
        domains.add(FILL_COUNT);
        domains.addAll(Schema.getAllDomains());
        domains.add(TEMPERATURE);
        domains.addAll(Temperature.getAllDomains());
        domains.add(RELOCATIONDATE);
        return domains;
      }

      public static final class Schema {
        public static final String MAX_SIZE = SCHEMA + "/maxSize";
        public static final String HEIGHT = SCHEMA + "/height";
        public static final String WIDTH = SCHEMA + "/width";
        public static final String UNLIMITED = SCHEMA + "/unlimited";
        public static final String STORABLE = SCHEMA + "/storable";

        private Schema() {
        }

        @Nonnull
        public static List<String> getAllDomains() {
          return asList(MAX_SIZE, HEIGHT, WIDTH, UNLIMITED, STORABLE);
        }

        @Nonnull
        public static Map<String, String> getSubExtensions() {
          return getExtensionMap(getAllDomains());
        }
      }

      public static final class Temperature {
        public static final String VALUE = TEMPERATURE + "/value";
        public static final String INHERIT_TEMPERATURE = TEMPERATURE + "/inheritToChildren";

        private Temperature() {
        }

        @Nonnull
        public static List<String> getAllDomains() {
          return asList(VALUE, INHERIT_TEMPERATURE);
        }
      }
    }

    /**
     * @see <a href="https://www.hl7.org/fhir/languages.html##ext">Translation Extension</a>
     */
    public static final class Translation {
      public static final String BASE_URL = "http://hl7.org/fhir/StructureDefinition/translation";
      public static final String LANG = "lang";
      public static final String CONTENT = "content";

      private Translation() {
      }

      @Nonnull
      public static List<String> getAllDomains() {
        return asList(BASE_URL, LANG, CONTENT);
      }
    }

    public static final class Catalogs {
      private static final String BASE_URL = Extension.BASE_URL + "/catalogs";
      public static final String CATALOG_USAGE = Catalogs.BASE_URL + "/usage";
      public static final String MASTER_DATA_CATALOG_TYPE = Catalogs.BASE_URL + "/masterDataCatalogType";

      private Catalogs() {
      }

      @Nonnull
      public static List<String> getAllDomains() {
        return asList(CATALOG_USAGE, MASTER_DATA_CATALOG_TYPE);
      }
    }

    public static final class StudyMember {
      private static final String BASE_URL = Extension.BASE_URL + "/studyMember";
      public static final String STUDY_CENTER = BASE_URL + "/studyCenter";

      private StudyMember() {
      }

      @Nonnull
      public static List<String> getAllDomains() {
        return singletonList(STUDY_CENTER);
      }
    }

    public static class FhirDefaults {

      /**
       * see <a href="https://www.hl7.org/fhir/R4/extension-data-absent-reason-definitions.html">Extension: Data Absent Reason</a>
       */
      public static final String DATA_ABSENT_REASON = "http://hl7.org/fhir/StructureDefinition/data-absent-reason";

      private FhirDefaults() {
      }

      @Nonnull
      public static List<String> getAllDomains() {
        return singletonList(DATA_ABSENT_REASON);
      }
    }
  }

  public static final class Catalog {
    public static final String BASE_URL = CXX_BASE_URL + "/catalog";
    public static final String ICD_CATALOG = FhirUrls.Catalog.BASE_URL + "/" + "IcdCatalog";
    public static final String CUSTOM_CATALOG = FhirUrls.Catalog.BASE_URL + "/" + "Catalog";
    public static final String OPS_CATALOG = FhirUrls.Catalog.BASE_URL + "/" + "OpsCatalog";
    public static final String VALUELIST = FhirUrls.Catalog.BASE_URL + "/" + "ValueList";
    public static final String MASTERDATACATALOG = FhirUrls.Catalog.BASE_URL + "/" + "MasterDataCatalog";

    private Catalog() {/* hide constructor */}
  }

  public static final class System {
    private static final String BASE_URL = CXX_BASE_URL + "/system";
    private static final String BASE_URL_VALUESET = CXX_BASE_URL + "/valueSet";

    public static final String CXX_ENTITY = BASE_URL + "/cxxEntity";

    public static final String STAY_TYPE = BASE_URL + "/stayType";
    public static final String LABOR_MAPPING = BASE_URL + "/laborMapping";
    public static final String LOCATION_TYPE = BASE_URL + "/locationType";
    public static final String ORGANIZATION_UNIT = BASE_URL + "/organizationUnit";
    public static final String INSURANCE_COMPANY = BASE_URL + "/insuranceCompany";

    // special master data catalog entry values
    public static final String STRING = CXX_BASE_URL + "/string";
    public static final String INTEGER = CXX_BASE_URL + "/integer";
    public static final String DECIMAL = CXX_BASE_URL + "/decimal";
    public static final String DATE = CXX_BASE_URL + "/date";
    public static final String BOOLEAN = CXX_BASE_URL + "/boolean";

    private System() {/* hide constructor */}

    public static class FhirDefaults {
      public static final String ICD10 = "http://hl7.org/fhir/sid/icd-10";
      public static final String DATA_ABSENT_REASON = "http://terminology.hl7.org/CodeSystem/data-absent-reason";

      private FhirDefaults() {
      }
    }

    public static class AmountUnit {
      public static final String BASE_URL = System.BASE_URL + "/amountUnit";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/amountUnit";

      private AmountUnit() {
      }
    }

    public static class InsuranceRelationship {
      public static final String BASE_URL = System.BASE_URL + "/insuranceRelationship";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/insuranceRelationship";

      private InsuranceRelationship() {
      }
    }

    public static class MeasurementSeries {
      private static final String BASE_URL = System.BASE_URL + "/measurementSeries";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/measurementSeries";

      public static final String SERIES_ID = BASE_URL + "/seriesId";

      private MeasurementSeries() {
      }

      public static class MeasurementSeriesDefinition {
        public static final String BASE_URL = MeasurementSeries.BASE_URL + "/measureSeriesDefinition";
        public static final String BASE_URL_VALUESET = MeasurementSeries.BASE_URL_VALUESET + "/measureSeriesDefinition";

        private MeasurementSeriesDefinition() {
        }
      }
    }

    public static class MedProcedure {
      public static final String PROCEDURE_ID = System.BASE_URL + "/medProcedure/procedureId";

      private MedProcedure() {
      }
    }

    public static class CountryState {
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/iso3166-2";

      private CountryState() {
      }
    }

    public static class MedDepartment {
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/medDepartment";
      public static final String BASE_URL = System.BASE_URL + "/medDepartment";

      private MedDepartment() {
      }
    }

    public static class Tumor {
      public static final String TUMOR_ID = System.BASE_URL + "/tumor/tumorId";
      public static final String XML_ID = System.BASE_URL + "/tumor/xmlId";
      public static final String CXX_TUMOR_ID = System.BASE_URL + "/tumor/cxxTumorId";

      private Tumor() {
      }
    }

    public static class Histology {
      public static final String HISTOLOGY_ID = System.BASE_URL + "/histology/histologyId";
      public static final String XML_ID = System.BASE_URL + "/histology/xmlId";
      public static final String CXX_HISTOLOGY_ID = System.BASE_URL + "/histology/cxxHistologyId";

      private Histology() {
      }
    }

    public static class Tnm {
      public static final String BASE_URL = System.BASE_URL + "/tnm";
      public static final String TNM_ID = BASE_URL + "/tnmId";
      public static final String XML_ID = BASE_URL + "/xmlId";
      public static final String CXX_TNM_ID = BASE_URL + "/cxxTnmId";

      private Tnm() {
      }
    }

    public static class AnnArbor {
      public static final String BASE_URL = System.BASE_URL + "/annArbor";
      public static final String ANNARBOR_ID = BASE_URL + "/annArborId";
      public static final String XML_ID = BASE_URL + "/xmlId";
      public static final String CXX_ANNARBOR_ID = BASE_URL + "/cxxAnnArborId";

      private AnnArbor() {
      }
    }

    public static class OtherClassification {
      public static final String BASE_URL = System.BASE_URL + "/otherClassification";
      public static final String CLASSIFICATION_ID = BASE_URL + "/classificationId";
      public static final String XML_ID = BASE_URL + "/xmlId";
      public static final String CXX_CLASSIFICATION_ID = BASE_URL + "/cxxClassificationId";
      public static final String OTHERCLASSIFICATION_ID = BASE_URL + "/otherClassificationId";
      public static final String CLASSIFICATION_NAME = BASE_URL + "/classificationName";

      private OtherClassification() {
      }
    }

    public static class Progress {
      public static final String BASE_URL = System.BASE_URL + "/progress";
      public static final String PROGRESS_ID = BASE_URL + "/progressId";
      public static final String XML_ID = BASE_URL + "/xmlId";
      public static final String CXX_PROGRESS_ID = BASE_URL + "/cxxProgressId";

      private Progress() {
      }
    }

    public static class Metastasis {
      public static final String BASE_URL = System.BASE_URL + "/metastasis";
      public static final String METASTASIS_ID = BASE_URL + "/metastasisId";
      public static final String XML_ID = BASE_URL + "/xmlId";
      public static final String CXX_METASTASIS_ID = BASE_URL + "/cxxMetastisId";

      private Metastasis() {
      }
    }

    public static class TumorMammaDiagnosis {
      public static final String BASE_URL = System.BASE_URL + "/tumorMammaDiagnosis";
      public static final String TUMORMAMMADIAGNOSIS_ID = BASE_URL + "/tumorMammaDiagnosisId";
      public static final String XML_ID = BASE_URL + "/xmlId";

      private TumorMammaDiagnosis() {
      }
    }

    public static class TumorLocalization {
      public static final String BASE_URL = System.BASE_URL + "/tumorLocalization";
      public static final String XML_ID = BASE_URL + "/xmlId";

      private TumorLocalization() {
      }
    }

    public static class CouncilPatient {
      public static final String BASE_URL = System.BASE_URL + "/councilPatient";
      public static final String COUNCIL_PATIENT_ID = BASE_URL + "/councilPatientId";

      private CouncilPatient() {
      }
    }

    public static class FollowDisease {
      public static final String BASE_URL = System.BASE_URL + "/followDisease";
      public static final String FOLLOWDISEASE_ID = BASE_URL + "/followDiseaseId";
      public static final String CXX_ID = BASE_URL + "/cxxId";

      private FollowDisease() {
      }
    }

    public static class Surgery {
      public static final String BASE_URL = System.BASE_URL + "/surgery";
      public static final String SURGERY_ID = BASE_URL + "/surgeryId";
      public static final String XML_ID = BASE_URL + "/xmlId";
      public static final String CXX_SURGERY_ID = BASE_URL + "/cxxSurgeryId";

      private Surgery() {
      }
    }

    public static class RadiationTherapy {
      public static final String BASE_URL = System.BASE_URL + "/radiationTherapy";
      public static final String RADIATIONTHERAPY_ID = BASE_URL + "/radiationTherapyId";
      public static final String XML_ID = BASE_URL + "/xmlId";
      public static final String CXX_RADIATIONTHERAPY_ID = BASE_URL + "/cxxRadiationTherapyId";

      private RadiationTherapy() {
      }
    }

    public static class SystemTherapy {
      public static final String BASE_URL = System.BASE_URL + "/systemTherapy";
      public static final String SYSTEMTHERAPY_ID = BASE_URL + "/systemTherapyId";
      public static final String XML_ID = BASE_URL + "/xmlId";
      public static final String CXX_SYSTEMTHERAPY_ID = BASE_URL + "/cxxSystemTherapyId";

      private SystemTherapy() {
      }
    }

    public static class RadiationComponent {
      public static final String BASE_URL = System.BASE_URL + "/radiationComponent";
      public static final String RADIATIONCOMPONENT_ID = BASE_URL + "/radiationComponentId";
      public static final String CXX_RADIATIONCOMPONENT_ID = BASE_URL + "/cxxRadiationComponentId";

      private RadiationComponent() {
      }
    }

    public static class SurgeryComponent {
      public static final String BASE_URL = System.BASE_URL + "/surgeryComponent";
      public static final String SURGERYCOMPONENT_ID = BASE_URL + "/surgeryComponentId";
      public static final String XML_ID = BASE_URL + "/xmlId";
      public static final String CXX_SURGERYCOMPONENT_ID = BASE_URL + "/cxxSurgeryComponentId";

      private SurgeryComponent() {
      }
    }

    public static class Complications {
      public static final String BASE_URL = System.BASE_URL + "/complications";
      public static final String XML_ID = BASE_URL + "/xmlId";

      private Complications() {
      }
    }

    public static class GtdsDict {
      private static final String BASE_URL = System.BASE_URL + "/gtds";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/gtds";

      public static class AnastomoticLeakageDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/anastomoticLeakageDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/anastomoticLeakageDictionary";

        private AnastomoticLeakageDictionary() {
        }
      }

      public static class AnnArborExtraDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/annArborExtraDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/annArborExtraDictionary";

        private AnnArborExtraDictionary() {
        }
      }

      public static class AnnArborGeneralDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/annArborGeneralDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/annArborGeneralDictionary";

        private AnnArborGeneralDictionary() {
        }
      }

      public static class AnnArborInfestationDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/annArborInfestationDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/annArborInfestationDictionary";

        private AnnArborInfestationDictionary() {
        }
      }

      public static class CaptureFinishStateDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/captureFinishStateDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/captureFinishStateDictionary";

        private CaptureFinishStateDictionary() {
        }
      }

      public static class ClosureReasonDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/closureReasonDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/closureReasonDictionary";

        private ClosureReasonDictionary() {
        }
      }

      public static class CodeTypeDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/codeTypeDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/codeTypeDictionary";

        private CodeTypeDictionary() {
        }
      }

      public static class ComplicationIntraPostDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/complicationIntraPostDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/complicationIntraPostDictionary";

        private ComplicationIntraPostDictionary() {
        }
      }

      public static class ComplicationKindDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/complicationKindDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/complicationKindDictionary";

        private ComplicationKindDictionary() {
        }
      }

      public static class CouncilStatusDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/councilStatusDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/councilStatusDictionary";

        private CouncilStatusDictionary() {
        }
      }

      public static class CouncilTypeDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/councilTypeDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/councilTypeDictionary";

        private CouncilTypeDictionary() {
        }
      }

      public static class DateAccuracyDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/dateAccuracyDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/dateAccuracyDictionary";

        private DateAccuracyDictionary() {
        }
      }

      public static class DiagnosisDoneDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/diagnosisDoneDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/diagnosisDoneDictionary";

        private DiagnosisDoneDictionary() {
        }
      }

      public static class EcogDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/ecogDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/ecogDictionary";

        private EcogDictionary() {
        }
      }

      public static class ExpansionGeneralDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/expansionGeneralDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/expansionGeneralDictionary";

        private ExpansionGeneralDictionary() {
        }
      }

      public static class FinalStateDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/finalStateDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/finalStateDictionary";

        private FinalStateDictionary() {
        }
      }

      public static class FishDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/fishDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/fishDictionary";

        private FishDictionary() {
        }
      }

      public static class FollowDiseaseKindDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/followDiseaseKindDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/followDiseaseKindDictionary";

        private FollowDiseaseKindDictionary() {
        }
      }

      public static class FullAssessmentDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/fullAssessmentDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/fullAssessmentDictionary";

        private FullAssessmentDictionary() {
        }
      }

      public static class GleasonScoreCauseDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/gleasonScoreCauseDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/gleasonScoreCauseDictionary";

        private GleasonScoreCauseDictionary() {
        }
      }

      public static class GradingDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/gradingDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/gradingDictionary";

        private GradingDictionary() {
        }
      }

      public static class Her2NeuScoreDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/her2NeuScoreDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/her2NeuScoreDictionary";

        private Her2NeuScoreDictionary() {
        }
      }

      public static class HighestInsuranceDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/highestInsuranceDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/highestInsuranceDictionary";

        private HighestInsuranceDictionary() {
        }
      }

      public static class IndicationDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/indicationDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/indicationDictionary";

        private IndicationDictionary() {
        }
      }

      public static class InsuranceDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/insuranceDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/insuranceDictionary";

        private InsuranceDictionary() {
        }
      }

      public static class InterventionKindDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/interventionKindDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/interventionKindDictionary";

        private InterventionKindDictionary() {
        }
      }

      public static class MarkerMethodDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/markerMethodDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/markerMethodDictionary";

        private MarkerMethodDictionary() {
        }
      }

      public static class MenoPauseStateDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/menoPauseStateDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/menoPauseStateDictionary";

        private MenoPauseStateDictionary() {
        }
      }

      public static class MetastasisCertaintyDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/metastasisCertaintyDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/metastasisCertaintyDictionary";

        private MetastasisCertaintyDictionary() {
        }
      }

      public static class MetastasisLocalisationDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/metastasisLocalisationDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/metastasisLocalisationDictionary";

        private MetastasisLocalisationDictionary() {
        }
      }

      public static class NewHistologyDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/newHistologyDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/newHistologyDictionary";

        private NewHistologyDictionary() {
        }
      }

      public static class PatientEnlightenStatusDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/patientEnlightenStatusDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/patientEnlightenStatusDictionary";

        private PatientEnlightenStatusDictionary() {
        }
      }

      public static class PostOpControlDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/postOpControlDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/postOpControlDictionary";

        private PostOpControlDictionary() {
        }
      }

      public static class PraeOpMarkerDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/praeOpMarkerDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/praeOpMarkerDictionary";

        private PraeOpMarkerDictionary() {
        }
      }

      public static class PreOpASADictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/preOpASADictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/preOpASADictionary";

        private PreOpASADictionary() {
        }
      }

      public static class ProcessingStateDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/processingStateDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/processingStateDictionary";

        private ProcessingStateDictionary() {
        }
      }

      public static class ProgressCaptureCauseDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/progressCaptureCauseDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/progressCaptureCauseDictionary";

        private ProgressCaptureCauseDictionary() {
        }
      }

      public static class ProgressTherapyIntentionDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/progressTherapyIntentionDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/progressTherapyIntentionDictionary";

        private ProgressTherapyIntentionDictionary() {
        }
      }

      public static class RASMutationDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/rASMutationDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/rASMutationDictionary";

        private RASMutationDictionary() {
        }
      }

      public static class RClassificationDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/rClassificationDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/rClassificationDictionary";

        private RClassificationDictionary() {
        }
      }

      public static class RadiationApplicationKindDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/radiationApplicationKindDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/radiationApplicationKindDictionary";

        private RadiationApplicationKindDictionary() {
        }
      }

      public static class RadiationInterruptReasonDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/radiationInterruptReasonDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/radiationInterruptReasonDictionary";

        private RadiationInterruptReasonDictionary() {
        }
      }

      public static class RadiationKindDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/radiationKindDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/radiationKindDictionary";

        private RadiationKindDictionary() {
        }
      }

      public static class RadiationTherapyKindDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/radiationTherapyKindDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/radiationTherapyKindDictionary";

        private RadiationTherapyKindDictionary() {
        }
      }

      public static class RadiationUnitDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/radiationUnitDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/radiationUnitDictionary";

        private RadiationUnitDictionary() {
        }
      }

      public static class ReceptorStateDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/receptorStateDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/receptorStateDictionary";

        private ReceptorStateDictionary() {
        }
      }

      public static class RelatedDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/relatedDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/relatedDictionary";

        private RelatedDictionary() {
        }
      }

      public static class ResidualLocalisationDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/residualLocalisationDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/residualLocalisationDictionary";

        private ResidualLocalisationDictionary() {
        }
      }

      public static class SelectionListDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/selectionListDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/selectionListDictionary";

        private SelectionListDictionary() {
        }
      }

      public static class SideDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/sideDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/sideDictionary";

        private SideDictionary() {
        }
      }

      public static class SourceDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/sourceDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/sourceDictionary";

        private SourceDictionary() {
        }
      }

      public static class SourceTypeDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/sourceTypeDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/sourceTypeDictionary";

        private SourceTypeDictionary() {
        }
      }

      public static class StateLymphNodeDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/stateLymphNodeDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/stateLymphNodeDictionary";

        private StateLymphNodeDictionary() {
        }
      }

      public static class StateMetastasisDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/stateMetastasisDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/stateMetastasisDictionary";

        private StateMetastasisDictionary() {
        }
      }

      public static class StatePrimaryDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/statePrimaryDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/statePrimaryDictionary";

        private StatePrimaryDictionary() {
        }
      }

      public static class StomapositionDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/stomapositionDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/stomapositionDictionary";

        private StomapositionDictionary() {
        }
      }

      public static class SurgerySuccessDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/surgerySuccessDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/surgerySuccessDictionary";

        private SurgerySuccessDictionary() {
        }
      }

      public static class SurgeryUrgencyDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/surgeryUrgencyDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/surgeryUrgencyDictionary";

        private SurgeryUrgencyDictionary() {
        }
      }

      public static class SurgicalAccessDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/surgicalAccessDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/surgicalAccessDictionary";

        private SurgicalAccessDictionary() {
        }
      }

      public static class SystemTherapyKindDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/systemTherapyKindDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/systemTherapyKindDictionary";

        private SystemTherapyKindDictionary() {
        }
      }

      public static class SystemTherapyProtocolTypeDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/systemTherapyProtocolTypeDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/systemTherapyProtocolTypeDictionary";

        private SystemTherapyProtocolTypeDictionary() {
        }
      }

      public static class SystemTherapyTypeDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/systemTherapyTypeDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/systemTherapyTypeDictionary";

        private SystemTherapyTypeDictionary() {
        }
      }

      public static class TMEDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/tMEDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/tMEDictionary";

        private TMEDictionary() {
        }
      }

      public static class TargetDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/targetDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/targetDictionary";

        private TargetDictionary() {
        }
      }

      public static class TherapyCauseDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/therapyCauseDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/therapyCauseDictionary";

        private TherapyCauseDictionary() {
        }
      }

      public static class TherapyDoneDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/therapyDoneDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/therapyDoneDictionary";

        private TherapyDoneDictionary() {
        }
      }

      public static class TherapyIntentionDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/therapyIntentionDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/therapyIntentionDictionary";

        private TherapyIntentionDictionary() {
        }
      }

      public static class TherapyStatusDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/therapyStatusDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/therapyStatusDictionary";

        private TherapyStatusDictionary() {
        }
      }

      public static class TherapyTargetLymphDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/therapyTargetLymphDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/therapyTargetLymphDictionary";

        private TherapyTargetLymphDictionary() {
        }
      }

      public static class TherapyTargetMetaDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/therapyTargetMetaDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/therapyTargetMetaDictionary";

        private TherapyTargetMetaDictionary() {
        }
      }

      public static class TherapyTargetPrimaryDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/therapyTargetPrimaryDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/therapyTargetPrimaryDictionary";

        private TherapyTargetPrimaryDictionary() {
        }
      }

      public static class TnmPDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/tnmPDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/tnmPDictionary";

        private TnmPDictionary() {
        }
      }

      public static class TumorCaptureCauseDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/tumorCaptureCauseDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/tumorCaptureCauseDictionary";

        private TumorCaptureCauseDictionary() {
        }
      }

      public static class TumorConferenceDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/tumorConferenceDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/tumorConferenceDictionary";

        private TumorConferenceDictionary() {
        }
      }

      public static class TumorDeathDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/tumorDeathDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/tumorDeathDictionary";

        private TumorDeathDictionary() {
        }
      }

      public static class VisitCauseDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/visitCauseDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/visitCauseDictionary";

        private VisitCauseDictionary() {
        }
      }

      public static class YesNoXDictionary {
        public static final String BASE_URL = GtdsDict.BASE_URL + "/yesNoXDictionary";
        public static final String BASE_URL_VALUESET = GtdsDict.BASE_URL_VALUESET + "/yesNoXDictionary";

        private YesNoXDictionary() {
        }
      }

      private GtdsDict() {
      }
    }

    public static class Episode {
      private static final String BASE_URL = System.BASE_URL + "/episode";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/episode";
      public static final String CXX_EPISODE_ID = BASE_URL + "/centraxxEpisodeId";

      private Episode() {
      }

      public static class StayType {
        private StayType() {
          /*hide constructor*/
        }

        public static final String BASE_URL = Episode.BASE_URL + "/stayType";
        public static final String BASE_URL_VALUESET = Episode.BASE_URL_VALUESET + "/stayType";
      }
    }

    public static class Crf {
      public static final String BASE_URL = System.BASE_URL + "/crf";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/crf";

      private Crf() {
      }
    }

    public static class SampleCategory {
      public static final String BASE_URL = System.BASE_URL + "/sampleCategory";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/sampleCategory";

      private SampleCategory() {
      }
    }

    public static class AbstractionReason {
      public static final String BASE_URL = System.BASE_URL + "/abstractionReason";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/abstractionReason";

      private AbstractionReason() {
      }
    }

    public static final class ContactAddress {
      public static final String BASE_URL = System.BASE_URL + "/contactAddress";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/contactAddress";

      public static final class CxxContactId {
        public static final String BASE_URL = ContactAddress.BASE_URL + "/cxxContactId";
        public static final String BASE_URL_VALUESET = ContactAddress.BASE_URL_VALUESET + "/cxxContactId";

        private CxxContactId() {
        }
      }

      private ContactAddress() {
      }
    }

    public static final class LaborMethod {
      public static final String BASE_URL = System.BASE_URL + "/laborMethod";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/laborMethod";

      private LaborMethod() {
      }

      public static final class Category {
        public static final String BASE_URL = LaborMethod.BASE_URL + "/category";
        public static final String BASE_URL_VALUESET = LaborMethod.BASE_URL_VALUESET + "/category";

        private Category() {
        }
      }

      public static final class LaborMethodType {
        public static final String BASE_URL = LaborMethod.BASE_URL + "/laborMethodType";
        public static final String BASE_URL_VALUESET = LaborMethod.BASE_URL_VALUESET + "/laborMethodType";

        private LaborMethodType() {
        }
      }
    }

    public static final class IdContainerType {
      public static final String BASE_URL = System.BASE_URL + "/idContainerType";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/idContainerType";

      private IdContainerType() {
      }
    }

    public static final class FlexiFlagItem {
      private static final String BASE_URL = System.BASE_URL + "/flexiFlagItem";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/flexiFlagItem";

      public static final class FlexiFlagDefEntry {
        public static final String BASE_URL = FlexiFlagItem.BASE_URL + "/flexiFlagDefEntry";
        public static final String BASE_URL_VALUESET = FlexiFlagItem.BASE_URL_VALUESET + "/flexiFlagDefEntry";
      }

      private FlexiFlagItem() {
      }
    }

    public static final class RadiationTarget {
      public static final String RADIATION_TARGET_ID = BASE_URL + "/radiationTargetId";
      public static final String CXX_RADIATION_TARGET_ID = BASE_URL + "/cXXRadiationTargetId";
      public static final String CODE = BASE_URL + "/code";

      private RadiationTarget() {
      }
    }

    public static final class AdverseEffects {
      public static final String ADVERSE_EFFECT_ID = BASE_URL + "/adverseEffectId";

      private AdverseEffects() {
      }
    }

    public static final class Patient {
      private static final String BASE_URL = System.BASE_URL + "/patient";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/patient";

      public static final class Ethnicity {
        public static final String BASE_URL = Patient.BASE_URL + "/ethnicity";
        public static final String BASE_URL_VALUESET = Patient.BASE_URL_VALUESET + "/ethnicity";

        private Ethnicity() {
        }
      }

      public static final class BloodGroup {
        public static final String BASE_URL = Patient.BASE_URL + "/bloodgroup";

        private BloodGroup() {
        }
      }

      public static final class Citizenship {
        public static final String BASE_URL = Patient.BASE_URL + "/citizenship";

        private Citizenship() {
        }
      }

      public static final class Denomination {
        public static final String BASE_URL = Patient.BASE_URL + "/denomination";

        private Denomination() {
        }
      }

      public static final class MaritalStatus {
        public static final String BASE_URL = Patient.BASE_URL + "/maritalStatus";

        private MaritalStatus() {
        }
      }

      public static final class Species {
        public static final String BASE_URL = Patient.BASE_URL + "/species";

        private Species() {
        }
      }

      public static final class Title {
        public static final String BASE_URL = Patient.BASE_URL + "/title";
        public static final String BASE_URL_VALUESET = Patient.BASE_URL_VALUESET + "/title";

        private Title() {
        }
      }

      private Patient() {/* hide constructor */}

      public static final class Gender {
        public static final String BASE_URL = Patient.BASE_URL + "/gender";

        private Gender() {
        }
      }

      public static final class PatientInsurance {
        private static final String BASE_URL = Patient.BASE_URL + "/patientInsurance";
        private static final String BASE_URL_VALUESET = Patient.BASE_URL_VALUESET + "/patientInsurance";

        public static final class InsuredRelationship {
          public static final String BASE_URL = PatientInsurance.BASE_URL + "/relationship";
          public static final String BASE_URL_VALUESET = PatientInsurance.BASE_URL_VALUESET + "/relationship";

          private InsuredRelationship() {}
        }

        public static final class CoverageType {
          public static final String BASE_URL = PatientInsurance.BASE_URL + "/coverageType";
          public static final String BASE_URL_VALUESET = PatientInsurance.BASE_URL_VALUESET + "/coverageType";

          private CoverageType() {}
        }

        private PatientInsurance() {
        }
      }
    }

    public static final class Study {
      public static final String BASE_URL = System.BASE_URL + "/study";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/study";

      private Study() {/* hide constructor */}

      public static class StudyStatus {

        public static final String BASE_URL = Study.BASE_URL + "/status";
        public static final String BASE_URL_VALUESET = Study.BASE_URL_VALUESET + "/status";

        private StudyStatus() {
        }
      }

      public static class StudyRegisterStatus {

        public static final String BASE_URL = Study.BASE_URL + "/studyRegisterStatus";
        public static final String BASE_URL_VALUESET = Study.BASE_URL_VALUESET + "/studyRegisterStatus";

        private StudyRegisterStatus() {
        }
      }

      public static class StudyVisitTemplate {

        public static final String BASE_URL = Study.BASE_URL + "/studyVisitTemplate";
        public static final String BASE_URL_VALUESET = Study.BASE_URL_VALUESET + "/studyVisitTemplate";

        private StudyVisitTemplate() {
        }
      }

      public static class StudyCenter {

        public static final String BASE_URL = Study.BASE_URL + "/studyCenter";
        public static final String BASE_URL_VALUESET = Study.BASE_URL_VALUESET + "/studyCenter";

        private StudyCenter() {
        }
      }

      public static class StudyArmType {

        public static final String BASE_URL = Study.BASE_URL + "/studyArmType";
        public static final String BASE_URL_VALUESET = Study.BASE_URL_VALUESET + "/studyArmType";

        private StudyArmType() {
        }
      }

      public static class Schedule {
        private static final String BASE_URL = System.Study.BASE_URL + "/schedule";
        public static final String MINUNIT = BASE_URL + "/minUnit";
        public static final String MAXUNIT = BASE_URL + "/maxUnit";
        public static final String REFPOINT = BASE_URL + "/refPoint";

        private Schedule() {
        }
      }
    }

    public static final class Sprec {
      private static final String BASE_URL = System.BASE_URL + "/sprec";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/sprec";

      private Sprec() {/* hide constructor */}

      public static final class FixationTime {
        public static final String BASE_URL = Sprec.BASE_URL + "/fixationTime";
        public static final String BASE_URL_VALUESET = Sprec.BASE_URL_VALUESET + "/fixationTime";

        private FixationTime() {
        }
      }

      public static final class StockType {
        public static final String BASE_URL = Sprec.BASE_URL + "/stockType";
        public static final String BASE_URL_VALUESET = Sprec.BASE_URL_VALUESET + "/stockType";

        private StockType() {
        }
      }

      public static final class ColdIschTime {
        public static final String BASE_URL = Sprec.BASE_URL + "/coldIschTime";
        public static final String BASE_URL_VALUESET = Sprec.BASE_URL_VALUESET + "/coldIschTime";

        private ColdIschTime() {
        }
      }

      public static final class WarmIschTime {
        public static final String BASE_URL = Sprec.BASE_URL + "/warmIschTime";
        public static final String BASE_URL_VALUESET = Sprec.BASE_URL_VALUESET + "/warmIschTime";

        private WarmIschTime() {
        }
      }

      public static final class TissueCollectionType {
        public static final String BASE_URL = Sprec.BASE_URL + "/tissueCollectionType";
        public static final String BASE_URL_VALUESET = Sprec.BASE_URL_VALUESET + "/tissueCollectionType";

        private TissueCollectionType() {
        }
      }

      public static final class SecondProcessing {
        public static final String BASE_URL = Sprec.BASE_URL + "/secondProcessing";
        public static final String BASE_URL_VALUESET = Sprec.BASE_URL_VALUESET + "/secondProcessing";

        private SecondProcessing() {
        }
      }

      public static final class StockProcessing {
        public static final String BASE_URL = Sprec.BASE_URL + "/stockProcessing";
        public static final String BASE_URL_VALUESET = Sprec.BASE_URL_VALUESET + "/stockProcessing";

        private StockProcessing() {
        }
      }

      public static final class PostCentrifugationDelay {
        public static final String BASE_URL = Sprec.BASE_URL + "/postCentrifugationDelay";
        public static final String BASE_URL_VALUESET = Sprec.BASE_URL_VALUESET + "/postCentrifugationDelay";

        private PostCentrifugationDelay() {
        }
      }

      public static final class PreCentrifugationDelay {
        public static final String BASE_URL = Sprec.BASE_URL + "/preCentrifugationDelay";
        public static final String BASE_URL_VALUESET = Sprec.BASE_URL_VALUESET + "/preCentrifugationDelay";

        private PreCentrifugationDelay() {
        }
      }

      public static final class PrimarySampleContainer {
        public static final String BASE_URL = Sprec.BASE_URL + "/primarySampleContainer";
        public static final String BASE_URL_VALUESET = Sprec.BASE_URL_VALUESET + "/primarySampleContainer";

        private PrimarySampleContainer() {
        }
      }
    }

    public static final class CrfTemplate {
      public static final String BASE_URL = System.BASE_URL + "/crfTemplate";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/crfTemplate";

      private CrfTemplate() {/* hide constructor */}

      public static final class Section {
        public static final String BASE_URL = CrfTemplate.BASE_URL + "/section";
        public static final String BASE_URL_VALUESET = CrfTemplate.BASE_URL_VALUESET + "/section";

        private Section() {/* hide constructor */}

        public static final class Field {
          public static final String BASE_URL = Section.BASE_URL + "/field";
          public static final String BASE_URL_VALUESET = Section.BASE_URL_VALUESET + "/field";

          private Field() {/* hide constructor */}

          public static final class CrfFieldType {
            public static final String BASE_URL = Field.BASE_URL + "/crfFieldType";
            public static final String BASE_URL_VALUESET = Field.BASE_URL_VALUESET + "/crfFieldType";

            private CrfFieldType() {/*hide constructor*/}
          }
        }

        public static final class Type {
          public static final String BASE_URL = Section.BASE_URL + "/type";
          public static final String BASE_URL_VALUESET = Section.BASE_URL_VALUESET + "/type";

          private Type() {/* hide constructor */}
        }

        public static final class Alignment {
          public static final String BASE_URL = Section.BASE_URL + "/alignment";
          public static final String BASE_URL_VALUESET = Section.BASE_URL_VALUESET + "/alignment";

          private Alignment() {/* hide constructor */}
        }
      }
    }

    public static final class StudyProfile {
      public static final String BASE_URL = System.BASE_URL + "/studyProfile";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/studyProfile";

      private StudyProfile() {/* hide constructor */}
    }

    public static final class Consent {
      public static final String BASE_URL = System.BASE_URL + "/consent";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/consent";

      private Consent() { /* hide constructor */}

      public static final class Type {
        public static final String BASE_URL = Consent.BASE_URL + "/type";
        public static final String BASE_URL_VALUESET = Consent.BASE_URL_VALUESET + "/type";

        private Type() {/* hide constructor */}
      }

      public static final class Action {
        public static final String BASE_URL = Consent.BASE_URL + "/action";
        public static final String BASE_URL_VALUESET = Consent.BASE_URL_VALUESET + "/action";

        private Action() {/* hide constructor */}
      }

      public static final class ConsentObject {
        public static final String BASE_URL = Consent.BASE_URL + "/object";
        public static final String BASE_URL_VALUESET = Consent.BASE_URL_VALUESET + "/object";

        private ConsentObject() {/* hide constructor */}
      }
    }

    public static final class Finding {
      public static final String BASE_URL = System.BASE_URL + "/finding";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/finding";
      public static final String LABOR_FINDING_ID = BASE_URL + "/laborFindingId";
      public static final String LABOR_FINDING_SHORTNAME = BASE_URL + "/shortname";

      private Finding() {/* hide constructor */}

      public static final class AbnormalFlag {
        public static final String BASE_URL = Finding.BASE_URL + "/abnormalFlag";
        public static final String BASE_URL_VALUESET = Finding.BASE_URL_VALUESET + "/abnormalFlag";

        private AbnormalFlag() {/* hide constructor */}
      }
    }

    public static final class Medication {
      public static final String BASE_URL = System.BASE_URL + "/medication";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/medication";
      public static final String AGENT = BASE_URL + "/agent";
      public static final String AGENT_GROUP = BASE_URL + "/agentGroup";
      public static final String DOSE_TYPE = BASE_URL + "/doseType";
      public static final String APPLICATION_METHOD = BASE_URL + "/applicationMethod";
      public static final String APPLICATION_MEDIUM = BASE_URL + "/applicationMedium";
      public static final String APPLICATION_FORM = BASE_URL + "/applicationForm";

      private Medication() {/* hide constructor */}

      public static final class ServiceType {
        public static final String BASE_URL = Medication.BASE_URL + "/serviceType";
        public static final String BASE_URL_VALUESET = Medication.BASE_URL_VALUESET + "/serviceType";

        private ServiceType() {
        }
      }
    }

    public static final class Calendar {
      public static final String BASE_URL = System.BASE_URL + "/calendar";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/calendar";
      public static final String PARTICIPANT_TYPE = BASE_URL + "/participantType";
      public static final String EVENT_ID = BASE_URL + "/eventId";

      private Calendar() {/* hide constructor */}

      public static final class Resource {
        public static final String BASE_URL = Calendar.BASE_URL + "/resource";
        public static final String BASE_URL_VALUESET = Calendar.BASE_URL_VALUESET + "/resource";

        private Resource() {/*hide constructor*/}
      }

      public static final class Type {
        public static final String BASE_URL = Calendar.BASE_URL + "/type";
        public static final String BASE_URL_VALUESET = Calendar.BASE_URL_VALUESET + "/type";

        private Type() {/* hide constructor*/}
      }

      public static final class StayType {
        public static final String BASE_URL = Calendar.BASE_URL + "/stayType";
        public static final String BASE_URL_VALUESET = Calendar.BASE_URL_VALUESET + "/stayType";

        private StayType() {/* hide constructor */}
      }

      public static final class AppointmentLocation {
        public static final String BASE_URL = Calendar.BASE_URL + "/appointmentLocation";
        public static final String BASE_URL_VALUESET = Calendar.BASE_URL_VALUESET + "/appointmentLocation";

        private AppointmentLocation() {/*hide constructor*/}
      }
    }

    public static final class Hotline {
      private static final String BASE_URL = System.BASE_URL + "/hotline";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/hotline";
      public static final String APPOINTMENT_LOCATION = BASE_URL + "/appointmentLocation";

      public static final class ContactEvent {
        public static final String BASE_URL = Hotline.BASE_URL + "/contactEvent";
        public static final String BASE_URL_VALUESET = Hotline.BASE_URL_VALUESET + "/contactEvent";

        private ContactEvent() {/* hide constructor */}
      }

      public static final class Reason {
        public static final String BASE_URL = Hotline.BASE_URL + "/reason";
        public static final String BASE_URL_VALUESET = Hotline.BASE_URL_VALUESET + "/reason";

        private Reason() {/* hide constructor */}
      }

      private Hotline() {/* hide constructor */}
    }

    public static final class Task {
      private static final String BASE_URL = System.BASE_URL + "/task";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/task";
      public static final String OTHER_DECLINE_REASON = BASE_URL + "/otherDeclineReason";
      public static final String ATTACHMENT_TYPE = BASE_URL + "/attachmentType";

      public static final class TaskType {
        public static final String BASE_URL = Task.BASE_URL + "/taskType";
        public static final String BASE_URL_VALUESET = Task.BASE_URL_VALUESET + "/taskType";

        private TaskType() {/* hide constructor */}
      }

      public static final class DeclineReason {
        public static final String BASE_URL = Task.BASE_URL + "/declineReason";
        public static final String BASE_URL_VALUESET = Task.BASE_URL_VALUESET + "/declineReason";

        private DeclineReason() {/* hide constructor */}
      }

      public static final class AssigneeGroup {
        public static final String BASE_URL = Task.BASE_URL + "/assigneeGroup";
        public static final String BASE_URL_VALUESET = Task.BASE_URL_VALUESET + "/assigneeGroup";

        private AssigneeGroup() {/* hide constructor */}
      }

      private Task() {/* hide constructor */}
    }

    public static final class LaborValue {
      public static final String BASE_URL = System.BASE_URL + "/laborValue";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/laborValue";

      private LaborValue() {/* hide constructor */}

      public static final class Unit {
        public static final String BASE_URL = LaborValue.BASE_URL + "/unit";
        public static final String BASE_URL_VALUESET = LaborValue.BASE_URL_VALUESET + "/unit";

        private Unit() {/* hide constructor */}
      }

      public static final class LaborValueType {
        public static final String BASE_URL = LaborValue.BASE_URL + "/laborValueType";
        public static final String BASE_URL_VALUESET = LaborValue.BASE_URL_VALUESET + "/laborValueType";

        private LaborValueType() {/*hide constructor*/}
      }

      public static final class ChoiceType {
        public static final String BASE_URL = LaborValue.BASE_URL + "/choiceType";
        public static final String BASE_URL_VALUESET = LaborValue.BASE_URL_VALUESET + "/choiceType";

        private ChoiceType() {/*hide constructor*/}
      }

      public static final class DatePrecision {
        public static final String BASE_URL = LaborValue.BASE_URL + "/datePrecision";
        public static final String BASE_URL_VALUESET = LaborValue.BASE_URL_VALUESET + "/datePrecision";

        private DatePrecision() {/*hide constructor*/}
      }

      public static final class BorderPrecision {
        public static final String BASE_URL = LaborValue.BASE_URL + "/borderPrecision";
        public static final String BASE_URL_VALUESET = LaborValue.BASE_URL_VALUESET + "/borderPrecision";

        private BorderPrecision() {/*hide constructor*/}
      }
    }

    public static final class Document {
      private static final String BASE_URL = System.BASE_URL + "/document";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/document";
      public static final String DOCUMENT_ID = BASE_URL + "/documentId";

      public static final class Kind {
        public static final String BASE_URL = Document.BASE_URL + "/kind";
        public static final String BASE_URL_VALUESET = Document.BASE_URL_VALUESET + "/kind";

        private Kind() {/* hide constructor */}
      }

      public static final class MappingType {
        public static final String BASE_URL = System.BASE_URL + "/mappingType";
        public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/mappingType";

        private MappingType() {/* hide constructor */}
      }

      public static final class Category {
        public static final String BASE_URL = Document.BASE_URL + "/category";
        public static final String BASE_URL_VALUESET = Document.BASE_URL_VALUESET + "/category";

        private Category() {/* hide constructor */}
      }

      public static final class Status {
        public static final String BASE_URL = Document.BASE_URL + "/status";
        public static final String BASE_URL_VALUESET = Document.BASE_URL_VALUESET + "/status";

        private Status() {/* hide constructor */}
      }

      private Document() {/* hide constructor */}
    }

    public static final class ServiceRequest {
      private static final String BASE_URL = System.BASE_URL + "/serviceRequest";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/serviceRequest";
      public static final String REQUEST_ID = BASE_URL + "/requestId";

      private ServiceRequest() {
      }

      public static final class Type {
        public static final String BASE_URL = ServiceRequest.BASE_URL + "/type";
        public static final String BASE_URL_VALUESET = ServiceRequest.BASE_URL_VALUESET + "/type";

        private Type() {
        }
      }
    }

    public static final class List {
      private static final String BASE_URL = System.BASE_URL + "/list";
      public static final String TYPE = BASE_URL + "/type";

      private List() {
      }
    }

    public static final class SampleLocationSchema {
      public static final String BASE_URL = System.BASE_URL + "/sampleLocationSchema";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/sampleLocationSchema";

      private SampleLocationSchema() {
      }
    }

    public static final class Sample {
      private static final String BASE_URL = System.BASE_URL + "/sample";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/sample";

      private Sample() {
      }

      public static final class SampleType {
        public static final String BASE_URL = Sample.BASE_URL + "/sampleType";
        public static final String BASE_URL_VALUESET = Sample.BASE_URL_VALUESET + "/sampleType";

        private SampleType() {
        }
      }

      public static final class Receptacle {
        public static final String BASE_URL = Sample.BASE_URL + "/sampleReceptacle";
        public static final String BASE_URL_VALUESET = Sample.BASE_URL_VALUESET + "/sampleReceptacle";

        private Receptacle() {
        }
      }

      public static final class SampleDonator {
        public static final String BASE_URL = Sample.BASE_URL + "/sampleDonator";

        private SampleDonator() {
        }
      }

      public static final class SamplePartner {
        public static final String BASE_URL = Sample.BASE_URL + "/samplePartner";

        private SamplePartner() {
        }
      }

      public static final class SamplingMoment {
        public static final String BASE_URL = Sample.BASE_URL + "/samplingMoment";

        private SamplingMoment() {
        }
      }

      public static final class Project {
        public static final String BASE_URL = Sample.BASE_URL + "/project";
        public static final String BASE_URL_VALUESET = Sample.BASE_URL_VALUESET + "/project";


        private Project() {
        }

        public static final class ProjectState {

          public static final String BASE_URL = Project.BASE_URL + "/projectState";
          public static final String BASE_URL_VALUESET = Project.BASE_URL_VALUESET + "/projectState";

          private ProjectState() {
          }
        }
      }

      public static final class SampleKind {
        public static final String BASE_URL = Sample.BASE_URL + "/sampleKind";

        private SampleKind() {
        }
      }

      public static final class SampleLocalisation {
        public static final String BASE_URL = Sample.BASE_URL + "/sampleLocalisation";

        private SampleLocalisation() {
        }
      }
    }

    public static final class StudyVisitItem {
      public static final String BASE_URL = System.BASE_URL + "/studyVisitItem";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/studyVisitItem";

      public static class ApprovalState {
        public static final String BASE_URL = StudyVisitItem.BASE_URL + "/approvalState";
        public static final String BASE_URL_VALUESET = StudyVisitItem.BASE_URL_VALUESET + "/approvalState";

        private ApprovalState() {/*hide constructor*/}
      }

      private StudyVisitItem() {/*hide constructor*/}
    }

    public static final class Country {

      public static final String BASE_URL = System.BASE_URL + "/country";

      private Country() {
      }
    }

    public static final class Catalogs {
      private static final String BASE_URL = System.BASE_URL + "/catalogs";
      private static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/catalogs";
      public static final String VALUE_LIST = Catalogs.BASE_URL + "/valueList";
      public static final String CUSTOM_CATALOG = Catalogs.BASE_URL + "/customCatalog";
      public static final String MASTER_DATA_CATALOG = Catalogs.BASE_URL + "/masterDataCatalog";
      public static final String USAGE_ENTRY = Catalogs.BASE_URL + "/usageEntry";

      private Catalogs() {
      }

      public static final class CatalogUsage {
        public static final String BASE_URL = Catalogs.BASE_URL + "/usage";
        public static final String BASE_URL_VALUESET = Catalogs.BASE_URL_VALUESET + "/usage";

        private CatalogUsage() {
        }
      }

      public static final class MasterDataCatalogType {
        public static final String BASE_URL = Catalogs.BASE_URL + "/masterDataCatalogType";
        public static final String BASE_URL_VALUESET = Catalogs.BASE_URL_VALUESET + "/masterDataCatalogType";

        private MasterDataCatalogType() {
        }
      }

      public static final class SearchCatalogItem {
        public static final String BASE_URL = Catalogs.BASE_URL + "/searchCatalogItem";

        private SearchCatalogItem() {
        }
      }

      //      public static final class UsageEntry {
      //        public static final String BASE_URL = Catalogs.BASE_URL + "/usageEntry";
      //
      //        private UsageEntry() {}
      //      }
      //
      //      public static final class IcdEntry {
      //        public static final String BASE_URL = Catalogs.BASE_URL + "/icdEntry";
      //
      //        private IcdEntry() {}
      //      }
      //
      //      public static final class OpsEntry {
      //        public static final String BASE_URL = Catalogs.BASE_URL + "/opsEntry";
      //
      //        private OpsEntry() {}
      //      }
      //
      //      public static final class CatalogEntry {
      //        public static final String BASE_URL = Catalogs.BASE_URL + "/catalogEntry";
      //
      //        private CatalogEntry() {}
      //      }
    }

    public static final class AttendingDoctor {
      public static final String BASE_URL = System.BASE_URL + "/attendingDoctor";

      private AttendingDoctor() {
      }
    }

    public static final class StudyMember {
      private static final String BASE_URL = System.BASE_URL + "/studyMember";

      public static final String INTERNAL_STUDYMEMBERID = BASE_URL + "/internalStudyMemberId";

      private StudyMember() {
      }
    }

    public static final class Organ {
      public static final String BASE_URL = System.BASE_URL + "/organ";
      public static final String BASE_URL_VALUESET = System.BASE_URL_VALUESET + "/organ";

      private Organ() {}
    }
  }

  private FhirUrls() {/* hide constructor */}
}
