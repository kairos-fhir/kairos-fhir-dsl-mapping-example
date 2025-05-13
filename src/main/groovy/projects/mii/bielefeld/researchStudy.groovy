package projects.mii.bielefeld

import de.kairos.fhir.centraxx.metamodel.FlexiStudyArm

import static de.kairos.fhir.centraxx.metamodel.RootEntities.flexiStudy

/**
 * HDRP v.2025.1.4, v.2025.2.0; fhir-dsl v.1.48.0
 *
 * examplary, needed as reference in research subject
 */
researchStudy {
  id = "ResearchStudy/" + context.source[flexiStudy().id()]

  context.source[flexiStudy().studyArms()].each {final def studyArm ->
    arm {
     name = studyArm[FlexiStudyArm.NAME]
    }
  }
}