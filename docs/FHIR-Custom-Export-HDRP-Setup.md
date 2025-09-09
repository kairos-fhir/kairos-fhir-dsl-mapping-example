<img src="images/Logo.png" width="250" alt="IQVIA Logo"/>

# FHIR Custom Export Documentation

## Table of Contents

<!-- TOC -->

* [FHIR Custom Export Documentation](#fhir-custom-export-documentation)
    * [Table of Contents](#table-of-contents)
    * [Introduction](#introduction)
    * [HDRP global configuration](#hdrp-global-configuration)
    * [Project Setup](#project-setup)
        * [Project-Specific Configuration Files](#project-specific-configuration-files)
            * [ProjectConfig.json](#projectconfigjson)
            * [ExportResourceMappingConfig.json](#exportresourcemappingconfigjson)
            * [BundleRequestMethodConfig.json](#bundlerequestmethodconfigjson)
    * [Patient Selection](#patient-selection)
    * [Export Mechanisms](#export-mechanisms)
        * [Incremental Export](#incremental-export)
        * [Scheduled Export](#scheduled-export)
        * [Bulk data export](#bulk-data-export)
    * [Export Targets](#export-targets)
        * [Filesystem Export](#filesystem-export)
        * [Target URL Export](#target-url-export)
        * [Useful Links](#useful-links)
        * [Glossary](#glossary)

<!-- TOC -->

## Introduction

The HDRP FHIR Custom Export functionality allows you to export data from HDRP in [FHIR R4](https://hl7.org/fhir/R4/index.html) format.
This document provides detailed instructions on how to set up, configure, and use this functionality.

FHIR (Fast Healthcare Interoperability Resources) is a standard for healthcare data exchange, published by HL7.
The HDRP FHIR Custom Export enables you to transform HDRP data into FHIR resources, which can then be used by other systems that support the
FHIR standard.

```mermaid
graph LR
    subgraph "HDRP Application"
        subgraph Database
            DB[(Database)]
        end

        subgraph "Data Loading"
            Entities[HDRP Entity]
        end

        subgraph "Serialization"
            EntityMaps[Key-Value Map]
        end

        subgraph "Transformation"
            GroovyEngine[Groovy Transformation Engine]
            FHIRResources[FHIR Resource]
        end
    end

    subgraph "File System"
        GroovyScripts[Groovy Scripts]
        ExportedBundles[Exported Bundle files]
    end

    subgraph "External FHIR Store"
        RESTEndpoint[FHIR REST Endpoint]
    end

    DB --> Entities
    Entities --> EntityMaps
    EntityMaps --> GroovyEngine
    GroovyScripts --> GroovyEngine
    GroovyEngine --> FHIRResources
    FHIRResources --> ExportedBundles
    FHIRResources --> RESTEndpoint
    classDef database fill: #f9f, stroke: #333, stroke-width: 2px;
    classDef entity fill: #bbf, stroke: #333, stroke-width: 1px;
    classDef transformation fill: #bfb, stroke: #333, stroke-width: 1px;
    classDef export fill: #fbb, stroke: #333, stroke-width: 1px;
    class DB database;
class EntityLoader, Entities, ConvertToMaps, EntityMaps entity;
class GroovyEngine,GroovyScripts, FHIRResources transformation;
class RESTEndpoint,ExportedBundles export;
```

## HDRP global configuration

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

## Project Setup

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

### Project-Specific Configuration Files

#### ProjectConfig.json

The `ProjectConfig.json` file configures:

- The patient filter for the export project
- The export modi used for the project
- The export target (filesystem and/or URL)

Example `ProjectConfig.json`:

```json
{
  "description": "",
  "scheduledExportEnable": {
    "value": true
  },
  "exportInterval": {
    "value": "0 0 12 * * *"
  },
  "incrementalExportEnable": {
    "value": false
  },
  "exportUser": {
    "value": "SYSTEM"
  },
  "bulkExportEnable": {
    "value": false
  },
  "incrementalDeleteExportEnable": {
    "value": false
  },
  "patientFilterValues": {
    "value": [
      "COVID-19-PATIENTID",
      "ANOTHER-PATIENT-ID"
    ]
  },
  "patientFilterType": {
    "value": "IDTYPE"
  },
  "exportToFileSystem": {
    "value": true
  },
  "exportFolder": {
    "value": "C:/centraxx-home/fhir-custom-export"
  },
  "uploadToUrl": {
    "value": false
  },
  "uploadUrl": {
    "value": "http://localhost:9090/fhir"
  },
  "uploadUser": {
    "value": "admin"
  },
  "uploadPassword": {
    "value": "admin"
  },
  "sslTrustAllEnable": {
    "value": false
  },
  "trustStoreEnable": {
    "value": false
  },
  "trustStore": {
    "value": "C:/centraxx-home/fhir-custom-truststore.jks"
  },
  "trustStorePassword": {
    "value": "changeit"
  },
  "prettyPrintEnable": {
    "value": false
  },
  "exportFormat": {
    "value": "JSON"
  },
  "fhirMessageLoggingEnable": {
    "value": false
  },
  "fhirResponseLoggingEnable": {
    "value": false
  },
  "fhirResponseValidationEnable": {
    "value": false
  },
  "pageSize": {
    "value": 10
  },
  "since": {
    "value": "2023-07-11T18:30:00"
  },
  "enableThsPseudonymization": {
    "value": false
  },
  "thsTargetId": {
    "value": "targetId"
  },
  "parallelEntityLoadingEnable": {
    "value": false
  },
  "parallelProcessingEnable": {
    "value": false
  }
}
```

| **Property**                                          | **Description**                                                                                                                                                                                            |
|-------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `scheduledExportEnable`                               | If true, this project will export all filtered HDRP patient data with the configured FHIR resources by the specified `exportInterval`.                                                                     |
| `incrementalExportEnable`                             | If true, this project will export filtered HDRP patient data with the configured FHIR resources by a detected change after a HDRP transaction.                                                             |
| `incrementalDeleteExportEnable`                       | If true, this project will export deletes of all mapped resources. Deleted entities are exported by the FHIR ID based on the resource type and HDRP OID.                                                   |
| `exportUser`                                          | The user used to execute the incremental and scheduled exports. Default is SYSTEM. A user can be configured in the user administration via the UI to enable access to clear text patient data for example. |
| `bulkExportEnable`                                    | If true, this project will export using the FHIR bulk export API.                                                                                                                                          |
| `exportToFileSystem`                                  | If true, the scheduler will export to the specified file system `exportFolder`.                                                                                                                            |
| `exportFolder`                                        | Specifies the folder where the export will be saved.                                                                                                                                                       |
| `uploadToUrl`                                         | If true, the scheduler will export to the specified `uploadUrl` REST endpoint.                                                                                                                             |
| `uploadUrl`                                           | The URL of the REST endpoint where the export will be uploaded.                                                                                                                                            |
| `uploadUser`                                          | BasicAuth username for the target system.                                                                                                                                                                  |
| `uploadPassword`                                      | BasicAuth password for the target system.                                                                                                                                                                  |
| `sslTrustAllEnable`                                   | If true, all SSL/TLS certificates are trusted.                                                                                                                                                             |
| `trustStoreEnable`                                    | If true, a separate trust store is used for verification, specified by `trustStore` and `trustStorePassword`.                                                                                              |
| `trustStore`                                          | Path to the trust store file.                                                                                                                                                                              |
| `trustStorePassword`                                  | Password for the trust store.                                                                                                                                                                              |
| `prettyPrintEnable`                                   | If true, enables pretty-printing of the FHIR message body.                                                                                                                                                 |
| `exportFormat`                                        | Exchange format of FHIR resources (e.g., JSON).                                                                                                                                                            |
| `fhirMessageLoggingEnable`                            | If true, HTTP message requests are logged in HDRP separately.                                                                                                                                              |
| `fhirResponseLoggingEnable`                           | If true, HTTP message responses are logged in HDRP separately.                                                                                                                                             |
| `fhirResponseValidationEnable`                        | If true, HTTP message responses are validated against their FHIR response status.                                                                                                                          |
| `patientFilterType`                                   | Filters patients and their data by a specified filter type (e.g., `PATIENTSTUDY`, `CONSENTTYPE`, etc.).                                                                                                    |
| `patientFilterValues`                                 | List of filter values used to filter patients.                                                                                                                                                             |
| `pageSize`                                            | The page size for queries in HDRP and resources in a FHIR bundle.                                                                                                                                          |
| `since`                                               | Resources will be included in the response if their related patients have changed after the specified date-time.                                                                                           |
| `enableThsPseudonymization`                           | If true, enables pseudonymization of patient identifiers using the `thsTargetId` property.                                                                                                                 |
| `thsTargetId`                                         | Target ID used for pseudonymization in combination with study profile and study code.                                                                                                                      |
| `exportInterval`                                      | Sets the export cron interval (e.g., `0 0 12 * * *` for once a day).                                                                                                                                       |
| `parallelEntityLoadingEnable` (since HDRP v.2025.2.3) | Allows parallelizing loading and initializing of entities from the databases in batches of 1000. Only useful for large page sizes > 1000.                                                                  |
| `parallelProcessingEnable` (since HDRP v.2025.2.3)    | The pseudonymization, serialization, and transformation is parallelized, which can improve performance for large page size exports                                                                         |                                                                                                                             |

**Note**: When you configure a scheduled export, HDRP needs to be restarted to apply changes to the `ProjectConfig.json`. HDRP initializes the
Scheduler at startup by reading the `ProjectConfig.json`. The incremental export does not require a restart after changes to the `ProjectConfig.json`.

#### ExportResourceMappingConfig.json

This file is created at the first export if it does not already exist in the directory.
It configures which Groovy scripts are used to transform HDRP entities and into which FHIR bundle resource type the result is exported.
The generated file contains the default mappings.

Example `ExportResourceMappingConfig.json`:

```json
{
  "mappings": [
    {
      "selectFromCxxEntity": "PATIENT",
      "transformByTemplate": "patient.groovy",
      "exportToFhirResource": "Patient"
    },
    {
      "selectFromCxxEntity": "SAMPLE",
      "transformByTemplate": "specimen.groovy",
      "exportToFhirResource": "Specimen"
    }
  ]
}
```

#### BundleRequestMethodConfig.json

This configuration specifies the HTTP request methods used for export to a target URL, such as a FHIR [Blaze](https://samply.github.io/blaze/) store.

Example `BundleRequestMethodConfig.json`:

```json
{
  "resourceTypeToHttpMethod": {
    "Patient": "PUT",
    "Specimen": "PUT"
  }
}
```

Both the `ExportResourceMappingConfig.json` and the `BundleRequestMethodConfig.json` are created upon the first export attempt. They can be changed
during runtime and do not require a HDRP restart.

## Patient Selection

The FHIR Custom Export dataset is filtered at patient level. Each exported HDRP entity is selected based on its linkage to the
Included patients. Patients can be filtered using the following filter types: These are specified in the `ProjectConfig.json` file.

```json
{
  "patientFilterValues": {
    "value": [
      "COVID-19-PATIENTID",
      "ANOTHER-PATIENT-ID"
    ]
  },
  "patientFilterType": {
    "value": "IDTYPE"
  }
}
```

The following filter types are supported:

| **Code**     | **Description**                                                                                                                                    |
|--------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| IDTYPE       | Patient has an ID of the specified ID type (IdContainerType code).                                                                                 |
| FLEXIFLAGDEF | Patient has an universal attribute of the specified universal attribute type (code)                                                                |
| ORGUNIT      | Patient has the specific organization unit (code).                                                                                                 |
| CONSENTTYPE  | Patient has a valid consent of the specified consent type (code).                                                                                  |
| PATIENTSTUDY | Patient belongs to a specified study in the study register. The study has to be specified by `{studyCode}${studyProfileCode}`, e.g. COVID19$BASIC. |

## Export Mechanisms

### Incremental Export

The incremental export is triggered when a HDRP entity that is configured for export is changed. This allows for real-time updates to the exported
FHIR resources.

To enable incremental export, set the following property in `centraxx-dev.properties`:

```
interfaces.fhir.custom.export.incremental.enable=true
```

and enable the incremantal export on the project level in the `ProjectConfig.json`:

```json
{
  "incrementalExportEnable": {
    "value": true
  }
}
```

### Scheduled Export

The scheduled export runs at specified intervals, exporting all entities that match the patient filter.

To enable scheduled export, set the following property in `centraxx-dev.properties`:

```
interfaces.fhir.custom.export.scheduled.enable=true
```

and enable on the project level and set a Spring chron expression in the `ProjectConfig.json`

```json
{
  "scheduledExportEnable": {
    "value": true
  },
  "exportInterval": {
    "value": "0 0 12 * * *"
  }
}
```

### Bulk data export

Bulk data export is used to export configured export customexport asynchronously to the file system or to a FHIR endpoint. The Configuration and usage
of the bulk data export is described [here](Bulk-Data-Export.md).

## Export Targets

### Filesystem Export

The filesystem export writes the FHIR resources to files on the local filesystem.

Configure the export target in `ProjectConfig.json`:

```json
{
  "exportToFileSystem": {
    "value": true
  },
  "exportFolder": {
    "value": "C:/centraxx-home/fhir-custom-export/example-project"
  }
}
```

The `exportFolder` property specifies the directory where the FHIR resources will be written.

### Target URL Export

The target URL export sends the FHIR resources to a specified URL using HTTP requests.

Configure the export target in `ProjectConfig.json`:

```json
{
  "uploadToUrl": {
    "value": true
  },
  "uploadUrl": {
    "value": "http://localhost:9090/fhir"
  }
}
```

The `uploadUrl` property specifies the endpoint of the target FHIR store.

### Useful Links

- [FHIR Specification](https://hl7.org/fhir/R4/index.html)
- [Groovy Documentation](https://groovy-lang.org/documentation.html)
- [HDRP](https://www.iqvia.com/locations/emea/iqvia-connected-healthcare-platform/iqvia-health-data-research-platform)

### Glossary

- **FHIR**: Fast Healthcare Interoperability Resources, a standard for healthcare data exchange.
- **HDRP**: Health Data Research Platform. A biobanking and clinical data management system.
- **Groovy**: A dynamic programming language for the Java virtual machine.
- **JSON**: JavaScript Object Notation, a lightweight data-interchange format
