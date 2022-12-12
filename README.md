![Kairos Logo](https://www.kairos.de/app/uploads/kairos-logo-blue_iqvia.png "Kairos Logo")

Example project for the Kairos FHIR DSL
========================================

# Scope

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
