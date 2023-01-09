package projects.cxx.custom.aggregate

import ca.uhn.fhir.context.FhirContext
import org.apache.commons.io.output.FileWriterWithEncoding
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent
import org.hl7.fhir.r4.model.Condition
import org.hl7.fhir.r4.model.Encounter
import org.hl7.fhir.r4.model.Observation
import org.hl7.fhir.r4.model.Procedure
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.Specimen

import java.nio.charset.StandardCharsets
import java.nio.file.Paths

/**
 * Helper script to unite patient associated resources in one bundle
 * @author Jonas Küttner, Mike Wähnert
 * @since v.1.7.0, CXX.v.2022.3.0
 */

patient {
  final String exportDir = "C:/Development/fhir-export/mii"

  final List files = new File(exportDir).list().toList().findAll {
    final String file -> !file.contains("Aggregate")
  }

  final List patientFiles = files.findAll { final file ->
    file.contains("Patient")
  }

  final List otherFiles = files.findAll { final file ->
    !patientFiles.contains(file)
  }

  final Map<String, String> patientBundlePathMap = new HashMap<>()

  // load all Patient bundles and initialize a new bundle for each and write it to disk
  for (final String patientFile : patientFiles) {
    final List<BundleEntryComponent> bundleEntryComponents = loadBundle(exportDir, patientFile).getEntry()

    for (final BundleEntryComponent bundleEntryComponent : bundleEntryComponents) {
      final Bundle patientBundle = new Bundle()
      patientBundle.addEntry(bundleEntryComponent)

      final String patientRef = bundleEntryComponent.getFullUrl()
      final String uuid = UUID.randomUUID().toString()
      patientBundlePathMap.put(patientRef, uuid)
      writeToFile(exportDir, patientBundle, uuid)
    }
  }

  // load all other resource bundles and add them to the corresponding patient bundle
  for (final String otherFile : otherFiles) {
    final List<BundleEntryComponent> bundleEntryComponents = loadBundle(exportDir, otherFile).getEntry()
    for (final BundleEntryComponent bundleEntryComponent : bundleEntryComponents) {
      final String ref = getReference(bundleEntryComponent.getResource())
      addToAggregate(exportDir, patientBundlePathMap.get(ref), bundleEntryComponent)
    }
  }

  //clean directory
  files.forEach { final String file -> new File(exportDir, file).delete() }
}

static Bundle loadBundle(final String exportDir, final String fileName) {

  new File(exportDir, fileName).newInputStream().withCloseable { final def is ->
    return FhirContext.forR4().newJsonParser().parseResource(Bundle.class, is)
  }
}

static void writeToFile(final String exportDir, final Bundle bundleToExport, final String uuid) {

  final File bundleFile = Paths.get(exportDir, "Aggregate_" + uuid + ".json").toFile()
  bundleFile.createNewFile()
  final Writer writer = new FileWriterWithEncoding(bundleFile, StandardCharsets.UTF_8)

  writer.withCloseable {
    FhirContext.forR4()
        .newJsonParser()
        .setPrettyPrint(true)
        .encodeResourceToWriter(bundleToExport, writer)
  }
}

static addToAggregate(final String exportDir, final String uuid, final BundleEntryComponent entry) {
  final Bundle patientBundle = loadBundle(exportDir, "Aggregate_" + uuid + ".json")
  patientBundle.addEntry(entry)
  writeToFile(exportDir, patientBundle, uuid)
}

static getReference(final Resource resource) {
  if (resource.getResourceType() == ResourceType.Specimen) {
    return ((Specimen) resource).getSubject()?.getReference()
  }
  if (resource.getResourceType() == ResourceType.Condition) {
    return ((Condition) resource).getSubject()?.getReference()
  }
  if (resource.getResourceType() == ResourceType.Encounter) {
    return ((Encounter) resource).getSubject()?.getReference()
  }
  if (resource.getResourceType() == ResourceType.Procedure) {
    return ((Procedure) resource).getSubject().getReference()
  }
  if (resource.getResourceType() == ResourceType.Observation) {
    return ((Observation) resource).getSubject().getReference()
  }
}


