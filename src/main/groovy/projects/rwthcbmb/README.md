![Kairos Logo](https://kairos.de/wp-content/uploads/2023/11/bildschirm_KAIROS_RGB_einfach-e1699976791799.png "Kairos Logo")

BBMRI.de / GBA Mappings* for RWTH cBMB
======================================

* Profile definitions: https://simplifier.net/bbmri.de
* Mappings can be used to export to a [samply blaze](https://github.com/samply/blaze) store
  to be found by the [BBMRI Sample Locator](https://samplelocator.bbmri.de)

RWTH cBMB uses the FHIR custom export only for some selected data points, that are documented by a CXX system.
Other data points are added by other export processes from other biobanking systems.

# Changes

## 2023-09-18

* Nullchecks for patients MPI and samples Lab-ID added.

## 2023-09-13

* Change logical FHIR IDs of Patient, Specimen, Condition to business identifier
* Sample.custodian extension changed from orgUnit id to code
* Sample.encounter removed

---
*With the kind support from

* [CCP IT working group of DKTK/DKFZ](https://dktk.dkfz.de/en/clinical-platform/working-groups-partners/ccp-it)
  and [GBN/GBA](https://www.bbmri.de/).
* [RWTH cBMB](https://www.cbmb.ukaachen.de)


