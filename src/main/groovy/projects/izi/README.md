![Kairos Logo](https://www.kairos.de/app/uploads/kairos-logo-blue_iqvia.png "Kairos Logo")

IZI Sync*
========================

* This project contains of tree local sites installations which sync data by FHIR into one central site installation.
* Each local site has its own set of FHIR Groovy mapping scripts and a subdirectory for the well-defined master data definitions are prepared for
  XML-Import.

# Setup
* The incremental export is preferred.
* Sync deletes are enabled.
* ID type mapping must be enabled at leipzig central. Each local site needs its own FHIR API user on the target.
* All necessary master data must exist. Master data definitions can be imported by XML.  

---
*With the kind support from  [Fraunhofer Institute for Cell Therapy and Immunology IZI](https://www.izi.fraunhofer.de/en.html).

# Changelog

## 2024-02-19

* Add UNKNOWN precision date extensions in patient, specimen and observation, consent and condition added

## 2024-01-14

* Filter added for consents with unknown or without validFrom date.
