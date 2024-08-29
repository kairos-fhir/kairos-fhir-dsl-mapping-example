![Kairos Logo](https://kairos.de/wp-content/uploads/2023/11/bildschirm_KAIROS_RGB_einfach-e1699976791799.png "Kairos Logo")

US Core Mappings
========================

* Specification: http://www.hl7.org/fhir/us/core/
* Mappings intended for FHIR Bulk export with the use case "US Core Data
  Interoperability": https://hl7.org/fhir/uv/bulkdata/#us-core-data-for-interoperability
* Available form CentraXX v.2022.1.0 (which includes kairos-fhir-dsl v.1.14.0)

## US CORE VITALSIGNS

The US Core provides very specific profiles for a set of vital signs. The observations are specified by LOINC codes and are mostly fixed values or
chosen from a bound value set. Therefore, corresponding CXX labor mappings must be defined in order to map data to US Core vitals sign FHIR profiles.
A set of CXX master data xml files is provided that specifies labor methods, labor values and units that need to be defined in CXX for the groovy
mappings to work.

* The files are located in `/cxx-masterdata-xml`
* Files can be imported over the CXX XML import interface for master data.
* The xml file name correspond to its groovy mapping file name pendant, e.g. `bloodPressure.xml` is the required labor method
  for `bloodPressure.groovy`
* These groovy mappings on the specified labor method XMLs have to be understood as examples. If observations should be filled with CXX measurement
  values from other labor methods or labor values, e.g. a HL7 profile, just change labor method and value code in the effected groovy file.

# CXX FHIR Import

The data corresponding to the eventually exported US-CORE profiled data sets can be imported over the FHIR import interface. Example FHIR messages for
the profiles are provided in `/cxx-fhir-import-example-messages`. Note that the system urls in codings must be adjusted, such that they contain the
correct OID of the catalog containing the code in the CXX database.

# CXX FHIR export

Examples of the resulting export FHIR messages are provided in `/cxx-fhir-export-example-messages` in ndjson format. For better readability, files are
exported with pretty print option.
