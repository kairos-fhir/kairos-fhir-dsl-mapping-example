![Kairos Logo](https://www.kairos.de/app/uploads/kairos-logo-blue_iqvia.png "Kairos Logo")

Kairos CentraXX to CTcue PatientFinder Mappings
========================

Mappings can be used to export data in CXX profiles, which has been imported before by CXX-PatientFinder interfaces.
The following imported data sets are supported/tested:

* [Cancer Outcomes and Services Data set (COSD)](https://digital.nhs.uk/ndrs/data/data-sets/cosd)
* [Systemic Anti-Cancer Therapy (SACT) data set](https://digital.nhs.uk/ndrs/data/data-sets/sact)
* Bahia Lorenzo imports at [Hull HUTH](https://www.hull.nhs.uk/)
* Bahia Histol imports at [Brno FNUSA](https://www.fnusa.cz/en/hp/)

# Change log

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
