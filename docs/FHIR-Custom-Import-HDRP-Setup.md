<img src="images/Logo.png" width="250" alt="IQVIA Logo"/>

FHIR Custom Export Setup Documentation
======================================

<!-- TOC -->
* [FHIR Custom Export Setup Documentation](#fhir-custom-export-setup-documentation)
* [Introduction](#introduction)
* [HDRP global configuration](#hdrp-global-configuration)
* [Project Setup](#project-setup)
  * [Project-Specific Configuration Files](#project-specific-configuration-files)
  * [Useful Links](#useful-links)
* [Glossary](#glossary)
<!-- TOC -->

# Introduction

The HDRP FHIR Custom Import functionality allows you to Import data the by [FHIR R4](https://hl7.org/fhir/R4/index.html) format.
This document provides detailed instructions on how to set up, configure, and use this functionality.

The HDRP FHIR Custom Import enables you to transform FHIR bundle messages with any FHIR profiling into HDRP profiling and then import them.

@formatter:off
```mermaid
graph TD
    FHIR_Custom_Bundle[FHIR Custom Bundle]
    subgraph "HDRP Application"
        REST_Custom_Bundle_Endpoint[REST Custom Bundle Endpoint]
        Transformation[Transformation]
        FHIR_HDRP_Bundle[FHIR HDRP Bundle]
        HDRP_Bundle_Import[HDRP Bundle Import]
        Entities[Entities]
        DB[(Database)]
    end

    subgraph "File system"
        
        Trans_Script[Groovy Script]
        Source_Files[Source FHIR File]
        Target_Files[Target FHIR File]
    end

    FHIR_Custom_Bundle --> REST_Custom_Bundle_Endpoint
    REST_Custom_Bundle_Endpoint --> Transformation
    REST_Custom_Bundle_Endpoint -->|optional export| Source_Files
    Trans_Script--> Transformation
    Transformation --> FHIR_HDRP_Bundle
    FHIR_HDRP_Bundle --> HDRP_Bundle_Import
    FHIR_HDRP_Bundle -->|optional export| Target_Files
    HDRP_Bundle_Import --> Entities
    Entities --> DB

    classDef entity fill: #bbf, stroke: #333, stroke-width: 1px;
    classDef transformation fill: #bfb, stroke: #333, stroke-width: 1px;
    classDef import fill: #fbb, stroke: #333, stroke-width: 1px;
    classDef database fill: #f9f, stroke: #333, stroke-width: 2px;
    
    class Entities entity;
    class Trans_Script,Transformation transformation;
    class FHIR_Custom_Bundle,FHIR_HDRP_Bundle,Source_Files,Target_Files import;
    class DB database;
```
@formatter:on

# HDRP global configuration

To enable FHIR Custom Export, you need to add the following properties to the `centraxx-dev.properties` file:

```
interfaces.fhir.custom.export.scheduled.enable=<true|false>
interfaces.fhir.custom.export.incremental.enable=<true|false>
interfaces.fhir.custom.mapping.dir=C:/applications/hdrp-home/fhir-custom-mappings
```

The `interfaces.fhir.custom.mapping.dir` property specifies the directory that will contain the individual export project folders.
This directory must exist on the HDRP application server.

Each subdirectory represents an export project in the `interfaces.fhir.custom.mapping.dir`:

```
C:/applications/hdrp-home/fhir-custom-mappings/project1
C:/applications/hdrp-home/fhir-custom-mappings/project2
```

# Project Setup

To set up a new export project:

1. Create a new directory under the `interfaces.fhir.custom.mapping.dir`
2. Copy the necessary Groovy scripts file into this directory
3. Optional: Copy the [Configuration files](#project-specific-configuration-files) into this directory.
4. Restart HDRP

The directory structure will look like:

```
interfaces.fhir.custom.mapping.dir/
└── project1/
    ├── ProjectConfig.json
    ├── ExportResourceMappingConfig.json
    ├── BundleRequestMethodConfig.json
    ├── script1.groovy
    ├── script2.groovy
    └── ...
````

If not supplied, HDRP will create the `ProjectConfig.json` after restart and the `ExportResourceMappingConfig.json`
and `BundleRequestMethodConfig.json` after triggering the first export, respectively.

## Project-Specific Configuration Files

TODO

## Useful Links

- [FHIR Specification](https://hl7.org/fhir/R4/index.html)
- [Groovy Documentation](https://groovy-lang.org/documentation.html)
- [HDRP](https://www.iqvia.com/locations/emea/iqvia-connected-healthcare-platform/iqvia-health-data-research-platform)

# Glossary

- **FHIR**: Fast Healthcare Interoperability Resources, a standard for healthcare data exchange. See
- **HDRP**: Health Data Research Platform. A biobanking and clinical data management system.
- **Groovy**: A dynamic programming language for the Java virtual machine.
- **JSON**: JavaScript Object Notation, a lightweight data-interchange format
