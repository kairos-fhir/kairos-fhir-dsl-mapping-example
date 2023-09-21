![Kairos Logo](https://www.kairos.de/app/uploads/kairos-logo-blue_iqvia.png "Kairos Logo")

DKTK Oncology Mappings*
======================

* Profile definitions: https://simplifier.net/oncology
* Mappings can be used to export to a samply blaze store: https://github.com/samply/blaze
* The changelog can be found below

---
*With the kind support from  [CCP IT working group of DKTK/DKFZ](https://dktk.dkfz.de/en/clinical-platform/working-groups-partners/ccp-it).

# Changelog

## CCP-IT JF 2023-09-21:

* Condition (Diagnoses) with an ICD10 code which starts not with c or d (which is not a tumor relevant diagnosis) is ignored.

## CCP-IT JF 2023-07-13:

* Encounter must not be exported anymore. The encounter resources and all references to encounter have been removed.
* Specimens must always be exported with aliquots and the parent reference to its aliquot group and master sample.
