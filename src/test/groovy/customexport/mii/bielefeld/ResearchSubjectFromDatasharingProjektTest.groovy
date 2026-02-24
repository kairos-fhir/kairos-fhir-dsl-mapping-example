package customexport.mii.bielefeld

import com.fasterxml.jackson.databind.ObjectMapper
import common.AbstractExportScriptTest
import common.ExportScriptTest
import common.TestResources
import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.ResearchSubject

import static org.junit.jupiter.api.Assertions.*

@TestResources(
        groovyScriptPath = "src/main/groovy/customexport/mii/bielefeld/researchSubjectFromDatasharingProjekt.groovy",
        contextMapsPath = "src/test/resources/customexport/mii/bielefeld_20260130/researchSubjectFromDatasharingProjekt"
)
class ResearchSubjectFromDatasharingProjektTest extends AbstractExportScriptTest<ResearchSubject> {


    @ExportScriptTest
    void testThatSubjectIsSet(final Context context, final ResearchSubject researchSubject) {
        assertNotNull(context.source["consent"]["consentType"]["flexiStudy"])
        def flexiStudy = context.source["consent"]["consentType"]["flexiStudy"]

        assertEquals("ResearchSubject/data-usage-project-" + flexiStudy["id"], researchSubject.id)

        assertEquals(flexiStudy["code"], researchSubject.identifier.first.value)

        println(new ObjectMapper().writeValueAsString(researchSubject))
    }

}
