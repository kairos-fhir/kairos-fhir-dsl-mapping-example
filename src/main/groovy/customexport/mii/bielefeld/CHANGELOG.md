# Change log

## 2025-11-20

* fix export order between LABOR_FINDING_LABOR_VALUE observation and LABOR_FINDING diagnosticReport

## 2025-06-04

* updated MII fhir packages to latest versions
* added export of patients without first and last name as MII Pseudo patient

## 2025-05-28

* fixed bug in gender mapping

## 2025-05-22

* added export of the LaborFindingLaborValue status and recordedOn date to Observations

## 2025-05-13

* fixed the consent script
* fixed ExportResourceMappingConfig.json
* added first script for research study

## 2025-02-17

* added new script and tests for MII consent

## 2025-02-04

* removed duplicate package
* moved tests

## 2024-10-21

* removed normalize date
* finished procedure script
* added all HDRP dates for condition
* updated fhir-dsl to 1.40.0
* set back the usage of address builder setters as extensions are not required as of now
* added ExportResourceMappingConfig for FNUSA
* added script for vital status based on HDRP Finding
