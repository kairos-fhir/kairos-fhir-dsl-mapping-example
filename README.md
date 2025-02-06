![Kairos Logo](https://kairos.de/wp-content/uploads/2023/11/bildschirm_KAIROS_RGB_einfach-e1699976791799.png "Kairos Logo")

Example project for the Kairos FHIR DSL
========================================

This project contains Groovy example scripts for the use of the CentraXX FHIR custom export interface. The examples show the possibilities how to
export CXX data to FHIR in accordance to almost any FHIR R4 profile. More infos about CentraXX can be found on
the [Kairos Website](https://www.kairos.de/en/)

# Getting Started

A [brief tutorial](gettingstarted.md) on how to set up and configure the FHIR custom export in CentraXX..

# How-To

Detailed instructions to the interface and its DSL can be found in the [German how-to](/CXX_FHIR_Custom_Export.pdf).

# Requirements

* To write or modify custom export scripts, it is necessary to have a very good understanding of the source and target data models to transform into
  each other. Therefore, it is very helpful to use the kairos-fhir-dsl library as a dependency, which contains a CentraXX JPA meta model as a source,
  and the FHIR R4 model as a target.
* This project uses [Maven](https://maven.apache.org/) for build management to download all necessary dependencies
  from [Maven Central](https://mvnrepository.com/repos/central) or the kairos-fhir-dsl library
  from [GitHub Packages](https://github.com/kairos-fhir/kairos-fhir-dsl-mapping-example/packages/606516/versions).

# GitHub Authentication with Maven

* Because GitHub does not allow downloading packages without access token, use maven with the access token in the local [settings.xml](settings.xml)
  in this project.

  ```
  mvn install -s settings.xml
  ```

* IntelliJ user can override the user settings file by File -> Settings -> Build Tools -> Maven or create own Maven run configurations. It is also
  possible to add it to .mvn/maven.config or to copy and past the repository authentication to another existing settings.file

* The kairos-fhir-dsl binaries before v.1.5.0 have not been published on a public maven repository yet, but can be downloaded in
  the [assets section of the corresponding tag](https://github.com/kairos-fhir/kairos-fhir-dsl-mapping-example/releases)
  and [installed manually](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html).

# Versioning

* The versioning of this example projects will be parallel to the kairos-fhir-dsl library, which
  follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).
* All Groovy example scripts will contain a @since annotation that describes the first CentraXX version, that can interpret the respective script. The
  specified CentraXX version contains the necessary minimal version of the kairos-fhir-dsl library, CXX entity exporter, initializer and support for
  the ExportResourceMappingConfig.json.
* The master branch might contain scripts, which using methods of the SNAPSHOT version of the underlying KAIROS-FHIR-DSL that has not been released
  yet. Please use only scripts of release tags, intended for your installed CentraXX version.

# Contribution / Participation

* Everyone can fork the project.
* If you want to enrich the project with your own scripts, follow these steps:
    * Fork the project.
    * Create a new directory on your fork under src/main/groovy/projects
    * Add your new scripts.
    * Add a meaningful README.md file describing the purpose, sources, participants and CXX version.
    * Create a pull request with your changes against our master branch.
    * If possible, please follow the existing basic coding standards:
        * use 2 spaces for indent / tab size
        * use final keyword wherever possible
        * prefer explicit typization instead of the untyped def keyword
* If you discover errors or bugs in existing scripts, we would be happy to receiving a notices in the form of e-mails, issues or pull requests.

# License

Copyright 2021 [KAIROS GmbH](https://kairos.de)

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with the License. You may obtain a
copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under
the License.

# SNOMED CT

This project includes SNOMED Clinical Terms® (SNOMED CT®) which is used by permission of the InternationalHealth Terminology Standards Development
Organisation (IHTSDO). All rights reserved. SNOMED CT®, was originally created by The College of American Pathologists. “SNOMED” and “SNOMED CT” are
registered trademarks of the IHTSDO.

Please make sure, that you have a valid SNOMED CT license, if you use example scripts with SNOMED CT concepts. SNOMED CT concepts are recognizable by
the system url http://snomed.info/sct .

Example

```
coding {
  system = "http://snomed.info/sct"
  code = "261665006"
}

```

# Testing
## Intro
The project supports testing scripts using context maps that represent the CXX source data and the Groovy script. Given the flexible nature of CXX
source data, where fields may or may not be present, the testing framework is designed to run test scripts over a variety of different source data
sets. This approach ensures comprehensive test coverage and validates the transformation of CXX data to FHIR resources.

The project supports testing scripts using context maps that represent the CXX source data and the Groovy script.
The CXX test data needs to be provided as a JSON file containing an array of maps for each instance of a CXX entity. To create a test, extend the
`AbstractExportScriptTest` class and annotate the test class with the `@TestResources` annotation. The annotation takes
two arguments: `groovyScriptPath` and `contextMapsPath`, which are the paths to the Groovy script to test and the JSON file
with the CXX entity map data, respectively.

``` 
@TestResources(
  groovyScriptPath = "src/main/groovy/projects/mii_bielefeld/encounter.groovy",
  contextMapsPath = "src/test/resources/projects/mii_bielefeld/encounter.json"
)
class EpisodeExportScriptTest extends AbstractExportScriptTest<Encounter> {}
```

The `AbstractExportScriptTest` class will load the array if context map and the Groovy script, then apply the script to the given map. The context map and
the resulting resource are provided as arguments and can be used in each test method to run assertions. Annotate each test method with the
`@ExportScriptTest` annotation and declare the method parameters like this:

```
@ExportScriptTest
void testThatClassIsSet(final Context context, final Encounter resource) {
  Assumptions.assumeTrue(context.source[episode().stayType()] != null)

  assertTrue(resource.hasClass_())
  assertEquals("http://terminology.hl7.org/CodeSystem/v3-ActCode", resource.getClass_().getSystem())
  assertEquals(context.source[episode().stayType().code()], resource.getClass_().getCode())
}
```

The test will then be run for each pair of the context map (CXX/HDRP entity object graph) and the resulting FHIR resource. The ´@ExportScriptTest´
annotation indicates that the test function is run parametrized and the argument are provided by AbstractExportScriptTest class. 
The number of test run will the number of test annoted with the annotation times the number of CXX/HDRP entity maps given in the json file. 

## Resource validation
Additionally, HAPI validation can be used to validate the resulting resources against certain FHIR profiles. The required FHIR packages need
to be provided in a separate folder. Annotate the test with the `@Validate` annotation like this:

``` 
@TestResources(
  groovyScriptPath = "src/main/groovy/projects/mii_bielefeld/encounter.groovy",
  contextMapsPath = "src/test/resources/projects/mii_bielefeld/encounter.json"
)
@Validate(packageDir = "src/test/resources/fhirpackages")
class EpisodeExportScriptTest extends AbstractExportScriptTest<Encounter> {}
```
The test will load all package files from the given path and instantiate a HAPI validator. All resources created during the transformation will be
validated.

If the validation fails, the whole test will fail with a `ClassConfiguration` error. The validation message with errors will be displayed in the stack
traces.

Remember that for proper validation, you have to specify the profile a resource should be compliant with in the meta element.

## Where to get the test maps from
Currently, you would have to print the context map into the server log when testing with your local CXX/HDRP instance. To do this, add the following
line to a script:

```
package projects.mii_bielefeld

import com.fasterxml.jackson.databind.ObjectMapper

condition {
  new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(context.source)
}
```

You can also write this to a custom file in the file system if you do not want to fill the server log.

## Considerations for validation
The validation may fail when the profiling declares fields as mandatory, which are optional in CXX and, therefore, may not be present.
Ensure that the test data in CXX is complete and compliant with the FHIR profiling requirements.

