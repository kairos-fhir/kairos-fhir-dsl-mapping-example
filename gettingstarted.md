![Kairos Logo](https://kairos.de/wp-content/uploads/2023/11/bildschirm_KAIROS_RGB_einfach-e1699976791799.png "Kairos Logo")

Getting started
===============
This is a brief tutorial on how to set up and configure a CentraXX FHIR custom export. For a detailed description, please read
the [How-To](CXX_FHIR_Custom_Export.pdf) documentation.
____________________________________________

# Quickstart steps

1. Configure centraxx-dev.properties
2. Create a project directory for an FHIR-export
3. Copy the Groovy scripts into the project directory
4. Restart CentraXX

After CentraXX-restart, the ```ProjectConfig.json``` file is created in the project directory.

5. Configure the export in the ```ProjectConfig.json```
6. Trigger the first export

At the first export, CentraXX creates two more configuration files.

7. Configure the resource mapping in the ```ExportResourceMappingConfig.json```, and the HTTP-Requests for export to target URL in
   the ```BundleRequestMethodConfig.json``` file.

# Configuration

## centraxx-dev.properties

Add the following configs to the centraxx-dev.properties file:

```
interfaces.fhir.custom.export.scheduled.enable=true
interfaces.fhir.custom.export.incremental.enable=true
interfaces.fhir.custom.mapping.dir=C:/applications/centraxx-home/fhir-custom-mappings
```

The path is an example. The specified directory will contain the individual export project folders. It must exist on the CentraXX application server.

## ProjectConfig.json

Each subdirectory of ```interfaces.fhir.custom.mapping.dir``` (_C:/applications/centraxx-home/fhir-custom-mappings_) represents an individual
project (e.g. _C:/applications/centraxx-home/fhir-custom-mappings/project1_) and is configured in this the ```ProjectConfig.json```. CentraXX creates
this file in a freshly added project directory after a restart. This file configures

* the patient filter for the export project
* the export mechanism
    - incremental export
    - scheduled export
* the export target
    - export to the filesystem
    - export to target URL

## ExportResourceMappingConfig.json

This file is created at the first export if it does not already exist in the directory. The file configures the Groovy scripts used to configure a
CentraXX entity and in which FHIR bundle resource type the result is exported.

## BundleRequestMethodConfig.json

This configuration specifies the HTTP-Request methods used for export to a target URL like a FHIR blaze store

Both the ```ExportResourceMappingConfig.json``` and the ```BundleRequestMethodConfig.json``` are created upon the first export attempt. They can be
changed during runtime and do not require a CentraXX restart.

**Note**: When you configure a scheduled export, CentraXX needs to be restarted to apply changes at the ```ProjectConfig.json```. CentraXX initializes
the Scheduler at the start reading in the ```ProjectConfig.json```. The incremental export does not require a restart after changes in
the ```ProjectConfig.json```.

_______________________________________________________

**Tips for setting up an export**:

The ```ExportResourceMappingConfig.json``` and the ```BundleRequestMethodConfig.json``` are created at the first export attempt. Therefore,
configuring a frequent export interval to timely trigger the first export is helpful. Furthermore, a higher export frequency allows examining the
effects of changes in the configuration and the Groovy scripts directly. Alternatively, you can activate the incremental export for testing and
trigger the export in a targeted manner if you made changes in the config or the scripts. You only have to change a test patient record in CentraXX to
start the incremental export. That prevents the export and accumulation of unnecessary FHIR resources in your target directory during the setup and
testing phase, which might happen when using a frequently scheduled export to the filesystem.
