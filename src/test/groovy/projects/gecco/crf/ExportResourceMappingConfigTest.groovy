package projects.gecco.crf

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

class ExportResourceMappingConfigTest {

  @Test
  void testThatMappingHasNoDuplicatedTemplate() {
    final File file = new File("src/main/groovy/projects/gecco/crf/ExportResourceMappingConfig.json")
    def slurper = new JsonSlurper().parse(file)
    def mappings = slurper.mappings

    Set<String> uniqueTemplates = new HashSet<>()
    for (String templateName : mappings.transformByTemplate) {
      assertTrue(uniqueTemplates.add(templateName), "Template mapping '" + templateName + "' duplicated.")
    }
  }

  @Test
  void testThatEachMappingHasATemplateFile() {

    final File file = new File("src/main/groovy/projects/gecco/crf/ExportResourceMappingConfig.json")
    def slurper = new JsonSlurper().parse(file)
    def mappings = slurper.mappings

    for (String templateName : mappings.transformByTemplate) {
      File mappingFile = new File("src/main/groovy/projects/gecco/crf/" + templateName + ".groovy")
      assertTrue(mappingFile.exists(), "Gecco template groovy file for config'" + templateName + "' not found.")
    }
  }

  @Test
  void testThatEachTemplateFileHasAMapping() {

    String[] templateNames = new File("src/main/groovy/projects/gecco/crf/").list()

    final File file = new File("src/main/groovy/projects/gecco/crf/ExportResourceMappingConfig.json")
    def slurper = new JsonSlurper().parse(file)
    def mappings = slurper.mappings

    for (String templateName : templateNames) {
      if (templateName.endsWith(".json")) {
        continue
      }

      String foundTemplateMapping = findMappedTemplate(mappings, templateName)
      assertNotNull(foundTemplateMapping, "No Mapping for template groovy file '" + templateName + "' found")
    }
  }

  private static String findMappedTemplate(def mappings, String templateName) {
    for (String mappedTemplate : mappings.transformByTemplate) {
      if (templateName == mappedTemplate + ".groovy") {
        return mappedTemplate
      }
    }
    return null
  }
}
