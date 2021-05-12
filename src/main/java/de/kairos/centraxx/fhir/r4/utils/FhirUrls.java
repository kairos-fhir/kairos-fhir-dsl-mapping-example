package de.kairos.centraxx.fhir.r4.utils;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.List;

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
     * Base URL for identifying FHIR extensions of a CXX system in accordance with https://hl7.org/FHIR/extensibility-definitions.html#Extension.url
     */
    private static final String BASE_URL = CXX_BASE_URL + "/extension";
    public static final String SPREC = BASE_URL + "/sprec";
    public static final String SAMPLE_CATEGORY = BASE_URL + "/sampleCategory";

    private Extension() {/* hide constructor */}

    public static List<String> getAllDomains() {
      final List<String> domains = new ArrayList<>();
      domains.add(SPREC);
      domains.add(SAMPLE_CATEGORY);
      domains.addAll(Patient.getAllDomains());
      domains.addAll(Sprec.getAllDomains());
      domains.addAll(Study.getAllDomains());
      domains.addAll(CrfTemplate.getAllDomains());
      domains.addAll(LaborValue.getAllDomains());
      domains.addAll(Hotline.getAllDomains());
      domains.addAll(Sample.getAllDomains());
      domains.addAll(Medication.getAllDomains());
      domains.addAll(Consent.getAllDomains());
      domains.addAll(Calendar.getAllDomains());
      domains.addAll(Task.getAllDomains());
      domains.addAll(Crf.getAllDomains());
      domains.addAll(StudyVisitItem.getAllDomains());
      return domains;
    }

    public static final class Sample {
      private static final String SAMPLE_BASE_URL = Extension.BASE_URL + "/sample";
      public static final String DERIVAL_DATE = SAMPLE_BASE_URL + "/derivalDate";
      public static final String REPOSITION_DATE = SAMPLE_BASE_URL + "/repositionDate";
      public static final String SAMPLE_LOCATION = SAMPLE_BASE_URL + "/sampleLocation";
      public static final String SAMPLE_LOCATION_PATH = SAMPLE_BASE_URL + "/sampleLocationPath";
      public static final String X_POSITION = SAMPLE_BASE_URL + "/xPosition";
      public static final String Y_POSITION = SAMPLE_BASE_URL + "/yPosition";

      private Sample() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return asList(DERIVAL_DATE, REPOSITION_DATE, SAMPLE_LOCATION, SAMPLE_LOCATION_PATH, X_POSITION, Y_POSITION);
      }

    }

    public static final class Patient {
      private static final String PATIENT_BASE_URL = Extension.BASE_URL + "/patient";
      public static final String ETHNICITIES = PATIENT_BASE_URL + "/ethnicities";
      public static final String ETHNICITY = ETHNICITIES + "/ethnicity";
      public static final String ETHNICITY_OVERRIDE = ETHNICITIES + "/override";

      private Patient() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return asList(ETHNICITIES, ETHNICITY, ETHNICITY_OVERRIDE);
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

      private Medication() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return asList(DOSE_VALUE, PRESCRIBER, TRANSCRIPTIONIST, TYPE, ORDINANCE_RELEASE_METHOD, IS_PRESCRIPTION, FON, PON, RESULTDATE);
      }
    }

    public static final class Study {
      private static final String STUDY_BASE_URL = Extension.BASE_URL + "/study";
      public static final String PHASES = STUDY_BASE_URL + "/phases";
      public static final String PHASE = PHASES + "/phase";
      public static final String PHASE_NAME = PHASE + "/name";
      public static final String PHASE_DESCRIPTION = PHASE + "/description";

      public static final String VISITS = STUDY_BASE_URL + "/visits";
      public static final String VISIT = VISITS + "/visit";
      public static final String VISIT_NAME = VISIT + "/name";
      public static final String VISIT_COPYVISITS = VISIT + "/copyVisits";

      private Study() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return asList(PHASES, PHASE, PHASE_NAME, PHASE_DESCRIPTION,
                      VISITS, VISIT, VISIT_NAME, VISIT_COPYVISITS);
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
    }

    public static final class CrfTemplate {
      private static final String CRFTEMPLATE_BASE_URL = Extension.BASE_URL + "/crfTemplate";
      public static final String MULTIPLE_USE_URL = CRFTEMPLATE_BASE_URL + "/multipleUse";

      private CrfTemplate() {/* hide constructor */}

      public static List<String> getAllDomains() {
        final List<String> domains = new ArrayList<>();
        domains.add(MULTIPLE_USE_URL);
        domains.addAll(Section.getAllDomains());
        return domains;
      }

      public static final class Section {
        private static final String SECTION_BASE_URL = CRFTEMPLATE_BASE_URL + "/section";
        public static final String INDEX = SECTION_BASE_URL + "/index";

        private Section() {/* hide constructor */}

        public static List<String> getAllDomains() {
          final List<String> domains = new ArrayList<>();
          domains.add(INDEX);
          domains.addAll(Field.getAllDomains());
          return domains;
        }

        public static final class Field {
          private static final String FIELD_BASE_URL = SECTION_BASE_URL + "/field";
          public static final String CRFFIELDTYPE = FIELD_BASE_URL + "/crfFieldType";
          public static final String TOOLTIP = FIELD_BASE_URL + "/toolTip";

          private Field() {/* hide constructor */}

          public static List<String> getAllDomains() {
            return asList(CRFFIELDTYPE, TOOLTIP);
          }
        }
      }
    }

    public static final class Crf {
      private static final String CRF_BASE_URL = Extension.BASE_URL + "/crf";
      public static final String CREATION_DATE = CRF_BASE_URL + "/creationDate";

      private Crf() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return singletonList(CREATION_DATE);
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
      public static final String UNIT = LABORVALUE_BASE_URL + "/unit";

      private LaborValue() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return asList(LABORVALUETYPE, MIN, MAX, DATE_PRECISION, OBSERVATION_METHOD, OBSERVATION_METHODS, IS_DEVIANT_VALUE, CHOICE_TYPE);
      }
    }

    public static final class Hotline {
      private static final String HOTLINE_BASE_URL = Extension.BASE_URL + "/hotline";
      public static final String IS_APP = HOTLINE_BASE_URL + "/app";

      private Hotline() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return singletonList(IS_APP);
      }
    }

    public static final class Consent {
      private static final String CONSENT_BASE_URL = Extension.BASE_URL + "/consent";
      public static final String USER_INFO_FILE = CONSENT_BASE_URL + "/userInfoFile";

      private Consent() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return singletonList(USER_INFO_FILE);
      }
    }

    public static final class Calendar {
      private static final String CALENDAR_BASE_URL = Extension.BASE_URL + "/calendar";
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

      public static List<String> getAllDomains() {
        final List<String> domains = new ArrayList<>();
        domains.addAll(asList(ALL_DAY,
                              DEPARTMENT, DRG, STAY_TYPE, LOCATION, RESOURCE, VISIBLE,
                              STUDY_CENTER, STUDY_VISIT_TEMPLATE, INVITATION_STATUS));
        domains.addAll(Attachment.getAllDomains());
        domains.addAll(Recurrence.getAllDomains());
        return domains;
      }

      public static final class Attachment {
        public static final String BASE = CALENDAR_BASE_URL + "/attachment";
        public static final String ATTACHMENT_PATIENT = BASE + "/patient";
        public static final String ATTACHMENT_CRF = BASE + "/crf";
        public static final String ATTACHMENT_SAMPLE = BASE + "/sample";
        public static final String ATTACHMENT_STUDY = BASE + "/study";
        public static final String ATTACHMENT_STUDYMEMBER = BASE + "/studyMember";

        private Attachment() { /* hide constructor */}

        public static List<String> getAllDomains() {
          return asList(BASE, ATTACHMENT_PATIENT, ATTACHMENT_CRF, ATTACHMENT_SAMPLE,
                        ATTACHMENT_STUDY, ATTACHMENT_STUDYMEMBER);
        }
      }

      public static final class Recurrence {
        public static final String BASE = CALENDAR_BASE_URL + "/recurrence";
        public static final String RECURRENCE_EXPRESSION = BASE + "/expression";
        public static final String RECURRENCE_ENDDATE = BASE + "/endDate";
        public static final String RECURRENCE_COUNT = BASE + "/count";

        private Recurrence() { /* hide constructor */}

        public static List<String> getAllDomains() { return asList(BASE, RECURRENCE_EXPRESSION, RECURRENCE_ENDDATE, RECURRENCE_COUNT); }
      }
    }

    public static final class Task {
      private static final String TASK_BASE_URL = Extension.BASE_URL + "/task";
      public static final String DESCRIPTION = TASK_BASE_URL + "/description";
      public static final String CAL_EVENT = TASK_BASE_URL + "/calendarEvent";
      public static final String NOTIFY_ON_RESOLVE = TASK_BASE_URL + "/notifyOnResolve";
      public static final String ASSIGNEE_GROUP = TASK_BASE_URL + "/assigneeGroup";

      private Task() { /* hide constructor */}

      public static List<String> getAllDomains() {
        return asList(DESCRIPTION, CAL_EVENT, NOTIFY_ON_RESOLVE, ASSIGNEE_GROUP);
      }
    }
  }

  public static final class System {
    private static final String BASE_URL = CXX_BASE_URL + "/system";
    public static final String SAMPLE_CATEGORY = BASE_URL + "/sampleCategory";
    public static final String MED_DEPARTMENT = BASE_URL + "/medDepartment";
    public static final String STAY_TYPE = BASE_URL + "/stayType";
    public static final String CRF = BASE_URL + "/crf";
    public static final String LABOR_MAPPING = BASE_URL + "/laborMapping";

    private System() {/* hide constructor */}

    public static final class Patient {
      private static final String BASE_URL = System.BASE_URL + "/patient";

      public static final String ETHNICITY = BASE_URL + "/ethnicity";

      private Patient() {/* hide constructor */}
    }

    public static final class Study {
      private static final String BASE_URL = System.BASE_URL + "/study";

      public static final String STUDY_CENTER = BASE_URL + "/studyCenter";
      public static final String STUDY_VISIT_TEMPLATE = BASE_URL + "/studyVisitTemplate";

      private Study() {/* hide constructor */}
    }

    public static final class Sprec {
      private static final String BASE_URL = System.BASE_URL + "/sprec";

      public static final String SPREC_TISSUE_COLLECTION_TYPE = BASE_URL + "/tissueCollectionType";
      public static final String WARM_ISCH_TIME = BASE_URL + "/warmIschTime";
      public static final String COLD_ISCH_TIME = BASE_URL + "/coldIschTime";
      public static final String STOCK_TYPE = BASE_URL + "/stockType";
      public static final String SPREC_FIXATION_TIME = BASE_URL + "/fixationTime";
      public static final String SPREC_PRIMARY_SAMPLE_CONTAINER = BASE_URL + "/primarySampleContainer";
      public static final String SPREC_PRE_CENTRIFUGATION_DELAY = BASE_URL + "/preCentrifugationDelay";
      public static final String SPREC_POST_CENTRIFUGATION_DELAY = BASE_URL + "/postCentrifugationDelay";
      public static final String STOCK_PROCESSING = BASE_URL + "/stockProcessing";
      public static final String SECOND_PROCESSING = BASE_URL + "/secondProcessing";

      private Sprec() {/* hide constructor */}
    }

    public static final class CrfTemplate {
      public static final String BASE_URL = System.BASE_URL + "/crfTemplate";

      private CrfTemplate() {/* hide constructor */}

      public static final class Section {
        public static final String BASE_URL = CrfTemplate.BASE_URL + "/section";

        private Section() {/* hide constructor */}

        public static final class Field {
          public static final String BASE_URL = Section.BASE_URL + "/field";

          private Field() {/* hide constructor */}
        }
      }
    }

    public static final class StudyProfile {
      public static final String BASE_URL = System.BASE_URL + "/studyProfile";

      private StudyProfile() {/* hide constructor */}
    }

    public static final class Consent {
      public static final String BASE_URL = System.BASE_URL + "/consent";

      private Consent() { /* hide constructor */}

      public static final class Type {
        public static final String BASE_URL = Consent.BASE_URL + "/type";

        private Type() {/* hide constructor */}
      }

      public static final class Action {
        public static final String BASE_URL = Consent.BASE_URL + "/action";

        private Action() {/* hide constructor */}
      }

      public static final class ConsentObject {
        public static final String BASE_URL = Consent.BASE_URL + "/object";

        private ConsentObject() {/* hide constructor */}
      }
    }

    public static final class Finding {
      public static final String BASE_URL = System.BASE_URL + "/finding";

      private Finding() {/* hide constructor */}

      public static final class AbnormalFlag {
        public static final String BASE_URL = Finding.BASE_URL + "/abnormalFlag";

        private AbnormalFlag() {/* hide constructor */}
      }

    }

    public static final class Medication {
      public static final String BASE_URL = System.BASE_URL + "/medication";
      public static final String AGENT = BASE_URL + "/agent";
      public static final String AGENT_GROUP = BASE_URL + "/agentGroup";
      public static final String DOSE_TYPE = BASE_URL + "/doseType";
      public static final String APPLICATION_METHOD = BASE_URL + "/applicationMethod";
      public static final String APPLICATION_MEDIUM = BASE_URL + "/applicationMedium";
      public static final String APPLICATION_FORM = BASE_URL + "/applicationForm";

      private Medication() {/* hide constructor */}
    }

    public static final class Calendar {
      public static final String BASE_URL = System.BASE_URL + "/calendar";
      public static final String TYPE = BASE_URL + "/type";
      public static final String PARTICIPANT_TYPE = BASE_URL + "/participantType";
      public static final String EVENT_ID = BASE_URL + "/eventId";
      public static final String RESOURCE = BASE_URL + "/resource";

      private Calendar() {/* hide constructor */}
    }

    public static final class Hotline {
      private static final String BASE_URL = System.BASE_URL + "/hotline";
      public static final String APPOINTMENT_LOCATION = BASE_URL + "/appointmentLocation";

      private Hotline() {/* hide constructor */}

    }

    public static final class Task {
      private static final String BASE_URL = System.BASE_URL + "/task";
      public static final String TASK_TYPE = BASE_URL + "/taskType";
      public static final String DECLINE_REASON = BASE_URL + "/declineReason";
      public static final String OTHER_DECLINE_REASON = BASE_URL + "/otherDeclineReason";
      public static final String ATTACHMENT_TYPE = BASE_URL + "/attachmentType";
      public static final String ASSIGNEE_GROUP = BASE_URL + "/assigneeGroup";

      private Task() {/* hide constructor */}

    }

    public static final class LaborValue {
      private static final String BASE_URL = System.BASE_URL + "/laborValue";
      public static final String UNIT = BASE_URL + "/unit";

      private LaborValue() {/* hide constructor */}
    }

    public static final class Document {
      private static final String BASE_URL = System.BASE_URL + "/document";
      public static final String MAPPING_TYPE = BASE_URL + "/mappingType";
      public static final String KIND = BASE_URL + "/kind";
      public static final String CATEGORY = BASE_URL + "/category";

      private Document() {/* hide constructor */}
    }

  }

  private FhirUrls() {/* hide constructor */}
}
