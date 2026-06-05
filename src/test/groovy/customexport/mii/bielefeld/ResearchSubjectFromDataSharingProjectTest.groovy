package customexport.mii.bielefeld

import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.ResearchSubject

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull

@TestResources(
        groovyScriptPath = "src/main/groovy/customexport/mii/bielefeld/researchSubjectFromDataSharingProject.groovy",
        contextMapsPath = "src/test/resources/customexport/mii/bielefeld/researchSubjectFromDataSharingProject"
)
class ResearchSubjectFromDataSharingProjectTest extends AbstractExportScriptTest<ResearchSubject> {


    @ExportScriptTest
    void testThatSubjectIsSet(final Context context, final ResearchSubject researchSubject) {
        assertNotNull(context.source["consent"]["consentType"]["flexiStudy"])
      final def flexiStudy = context.source["consent"]["consentType"]["flexiStudy"]
      final def patientcontainer = context.source["patientcontainer"]

        assertEquals("ResearchSubject/" + flexiStudy["id"] + "-" + patientcontainer["id"], researchSubject.id)

        assertEquals(flexiStudy["code"], researchSubject.identifier.find().value)


    }

}
