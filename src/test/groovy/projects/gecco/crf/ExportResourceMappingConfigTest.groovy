package projects.gecco.crf

import groovy.json.JsonSlurper
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Test to verify that ExportResourceMappingConfig is valid and covers all groovy scripts.
 */
class ExportResourceMappingConfigTest {

  @Test
  void testThatMappingHasNoDuplicatedTemplate() {
    final File file = new File("src/main/groovy/projects/gecco/crf/ExportResourceMappingConfig.json")
    final def slurper = new JsonSlurper().parse(file)
    final def mappings = slurper.mappings

    final Set<String> uniqueTemplates = new HashSet<>()
    for (final String templateName : mappings.transformByTemplate) {
      assertTrue(uniqueTemplates.add(templateName), "Template mapping '" + templateName + "' duplicated.")
    }
  }

  @Test
  void testThatEachMappingHasATemplateFile() {

    final File file = new File("src/main/groovy/projects/gecco/crf/ExportResourceMappingConfig.json")
    final def slurper = new JsonSlurper().parse(file)
    final def mappings = slurper.mappings

    for (final String templateName : mappings.transformByTemplate) {
      File mappingFile = new File("src/main/groovy/projects/gecco/crf/" + templateName + ".groovy")
      assertTrue(mappingFile.exists(), "Gecco template groovy file for config'" + templateName + "' not found.")
    }
  }

  @Test
  void testThatEachCrfTemplateFileHasAMapping() {

    final String[] templateNames = new File("src/main/groovy/projects/gecco/crf/").list()

    final File file = new File("src/main/groovy/projects/gecco/crf/ExportResourceMappingConfig.json")
    final def slurper = new JsonSlurper().parse(file)
    final def mappings = slurper.mappings

    for (final String templateName : templateNames) {
      if (templateName.endsWith(".json") || templateName.endsWith(".md") || templateName.equals("labVital")) {
        continue
      }

      String foundTemplateMapping = findMappedTemplate(mappings, templateName)
      assertNotNull(foundTemplateMapping, "No Mapping for template groovy file '" + templateName + "' found")
    }
  }

  @Test
  void testThatEachLabVitalTemplateFileHasAMapping() {

    final String[] templateNames = new File("src/main/groovy/projects/gecco/crf/labVital").list()

    final File file = new File("src/main/groovy/projects/gecco/crf/labVital/ExportResourceMappingConfig.json")
    final def slurper = new JsonSlurper().parse(file)
    final def mappings = slurper.mappings

    for (final String templateName : templateNames) {
      if (templateName.endsWith(".json") || templateName.endsWith(".md")) {
        continue
      }

      String foundTemplateMapping = findMappedTemplate(mappings, templateName)
      assertNotNull(foundTemplateMapping, "No Mapping for template groovy file '" + templateName + "' found")
    }
  }

  private static String findMappedTemplate(final def mappings, final String templateName) {
    for (final String mappedTemplate : mappings.transformByTemplate) {
      if (templateName == mappedTemplate + ".groovy") {
        return mappedTemplate
      }
    }
    return null
  }
}
