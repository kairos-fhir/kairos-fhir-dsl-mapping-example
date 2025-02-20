![Kairos Logo](https://kairos.de/wp-content/uploads/2023/11/bildschirm_KAIROS_RGB_einfach-e1699976791799.png "Kairos Logo")

Kairos CentraXX to CTcue PatientFinder Mappings
========================

Mappings can be used to export data in CXX profiles, which has been imported before by CXX-PatientFinder interfaces.
The following imported data sets are supported/tested:

* [Cancer Outcomes and Services Data set (COSD)](https://digital.nhs.uk/ndrs/data/data-sets/cosd)
* [Systemic Anti-Cancer Therapy (SACT) data set](https://digital.nhs.uk/ndrs/data/data-sets/sact)
* Bahia Lorenzo imports at [Hull HUTH](https://www.hull.nhs.uk/)
* Bahia Histol imports at [Brno FNUSA](https://www.fnusa.cz/en/hp/)

# Change log

## 2025-02-18
* filtered Hull patient identifiers for NHS id.

## 2025-02-12
* replaced string with new enum for MedicationServiceType
* removed id filter for NHS
* added export of LaborMethod name as Questionnaire description
* removed println statements from specimen script

## 2025-01-29
* changed the medication related export scripts to export medications and link them via identifier from finding
* added export scripts for Questionnaire and QuestionnaireResponse in Hull
* filtered Hull patient identifiers for NHS id.

## 2025-01-27
* removed export of onset end date for conditions as not desired anymore.

## 2025-01-16
* Fixed metamodel usage for CatalogEntry, which was migrated to Multilinguals

## 2025-01-10
* Added export of sample type in specimen export script

## 2025-01-07
* Worked on the Observation script to filter correctly
* Added processing to remove "specialty: " from Orgunit names

## 2024-11-29
* Added practitioner script to HULL to export AttendingDoctors
* added reference from MedicationAdministration to MedicationReequest
* Added Export of ObservationEnd to MedicationAdmin timing.event 


## 2024-11-26
* Update Specimen script for Hull

## 2024-11-19
* added export of AttendingDoctor as participant in Encounter script
* added usage of extensions in allergy intolerance script

## 2024-11-06
* moved telecom information from Patient.contact.telecom to Patient.telecom
* Removed unused fields from the MedicationRequest and MedicationAdministration scripts
* added Medication script

## 2024-10-17
* Added the export of MedicationRequest requester from LaborMapping

## 2024-10-14

* Added date normalization to allergyIntolerance
* added filter to only allow value set codes for AllergyIntoleranceSeverity

## 2024-10-09

* Fixed AllergyIntolerance script to avoid NPEs when DateValue is null in Finding

## 2024-10-08

* Added export of LaborMapping with LaborMethod code "Procedure_Profile" in medProcedure script.
* Condition.recordedDate added

## 2024-06-06

* Disabled diagnostic reports for Hull as long as Bahia has no data mapped to it.

## 2024-07-08

* Adjust example ProjectConfig.json for page size 10k and with another default export dir

## 2024-04-15

* labor value groups as diagnostic report categories added
* strange time zone offsets filtered in labor finding dates

## 2024-03-18

* export delete enabled for FNUSA mappings

## 2024-03-11

"histological and cytological findings" added to free text special transformation scripts

## 2024-02-08

* histology filtered from freeTextFilteredObservation export
* procedures extended with category if surgical procedure detected.

## 2024-01-22

* Null values in diagnostic report conclusion removed.
* Diagnostic report conclusion extended to more data types
* histoReport profile added to freeTextDiagnosticReport export

## 2023-12-05

* Exported all FNUSA SACT medications as MedicationAdministration

## 2023-11-29

* histoReport laborMethod code changed
* ExportResourceMappingConfig split between Hull and FNUSA to avoid useless LaborMapping queries

## 2023-11-16

* combined TNM for COSD/SACT changed for final treatment / integrated

## 2023-09-25

* histoReport script added

## 2023-09-21

* Observation with combined TNM code (tnmSimple) instead of separated TNM
* "FAKE" Encounters suppressed.

## 2023-09-14

* User defined catalog entries for condition and procedures added
* Display description of procedure, condition and observation catalog entries added.
* Encounters for SACT/COSD are suppressed.
