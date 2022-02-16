![Kairos Logo](https://www.kairos.de/app/uploads/kairos-logo-blue_iqvia.png "Kairos Logo")

US Core Mappings
========================

* Specification: http://www.hl7.org/fhir/us/core/
* Mappings intended for FHIR Bulk export with the use case "US Core Data
  Interoperability": https://hl7.org/fhir/uv/bulkdata/#us-core-data-for-interoperability
* Available form CentraXX v.2022.1.0

## US CORE VITALSIGNS

The US Core provides very specific profiles for a set of Vital signs. The observations are specified by LOINC codes and are mostly fixed values or
chosen from a bound value set. However, CentraXX does not support LOINC codes for labor values. Therefore, corresponding labor mappings must be
defined in CXX in order to map US Core Vitals Signs. A set of CXX master data xml files is provided that specifies labor methods, labor values and
units that need to be defined in CXX for the groovy mappings to work. They can be imported over the CXX Xml import interface. The files are located
in `/cxx-us-core-vitalsigns`
