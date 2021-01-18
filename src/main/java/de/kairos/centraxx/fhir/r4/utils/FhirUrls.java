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
      domains.addAll(Sprec.getAllDomains());
      domains.addAll(Study.getAllDomains());
      domains.addAll(CrfTemplate.getAllDomains());
      domains.addAll(LaborValue.getAllDomains());
      return domains;
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

      private CrfTemplate() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return Section.getAllDomains();
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

          private Field() {/* hide constructor */}

          public static List<String> getAllDomains() {
            return singletonList(CRFFIELDTYPE);
          }
        }
      }
    }

    public static final class LaborValue {
      private static final String LABORVALUE_BASE_URL = Extension.BASE_URL + "/laborValue";
      public static final String LABORVALUETYPE = LABORVALUE_BASE_URL + "/laborValueType";
      public static final String MIN = LABORVALUE_BASE_URL + "/min";
      public static final String MAX = LABORVALUE_BASE_URL + "/max";

      private LaborValue() {/* hide constructor */}

      public static List<String> getAllDomains() {
        return asList(LABORVALUETYPE, MIN, MAX);
      }
    }
  }

  public static final class System {
    private static final String BASE_URL = CXX_BASE_URL + "/system";
    public static final String SAMPLE_CATEGORY = BASE_URL + "/sampleCategory";

    private System() {/* hide constructor */}

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
  }

  private FhirUrls() {/* hide constructor */}
}
