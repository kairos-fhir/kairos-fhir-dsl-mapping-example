![Kairos Logo](https://www.kairos.de/app/uploads/kairos-logo-blue_iqvia.png "Kairos Logo")

DKTK Oncology Mappings*
======================

* Profile definitions: https://simplifier.net/oncology
* Mappings can be used to export to a samply blaze store: https://github.com/samply/blaze
* The changelog can be found below.

---
*With the kind support from  [CCP IT working group of DKTK/DKFZ](https://dktk.dkfz.de/en/clinical-platform/working-groups-partners/ccp-it).

# Changelog

## 2024-06-18

* added Scripts:
    * anzahlBefallenenLymphknoten.groovy
    * anzahlBefallenenSentinelLymphknoten.groovy
    * anzahlUntersuchtenLymphknoten.groovy
    * anzahlUntersuchtenSentinelLymphknoten.groovy
* TNM extended for additional category values L, V, Pn, S

## 2024-05-14

* KAIROS-FHIR-DSL updated to 1.33.0
* condition.evidence added
* operation.groovy: procedure.complication added
* nebenwirkung.groovy for adverseEvents added

## 2024-04-10

* ecog export added
* weitereKlassifikation export added

## 2024-03-14

* Specimen sampleType mapping extended for Mainz requirements (CENTRAXX-19010)

## 2024-02-29

* Reference to grading in histology hasMember added
* Export order changed to make sure that grading is exported before histology
* Unnecessary logging in specimen removed

## 2023-11-28

* genetischeVariante: condition reference removed

## 2023-11-16

* Further changes for "Kryo/Frisch (FF)", "Paraffin (FFPE)"
* „SNP“ für Kryo/Frisch (Fixierungsart)
* „NBF“ für FFPE (Fixierungsart)
* „NRT“ für Tumorgewebe (Probenart)
* „TBL“ für Vollblut (Probenart)

## 2023-11-03

* BBMRI SampleMaterialType mapped by site specific CXX SampleType codes

## 2023-11-01

* Surgery component codes as additional procedure/surgery codes added. The CCP-IT assumption is that all surgery components are documented on site
  with OPS codes.

## 2023-10-09

* Condition ICD-O-3-T added
* Seitenlokalisation added
* Intention OP: extension in operation.groovy added
* Strahlentherapie Stellung zu operativer Therapie: extension in strahlentherapie.groovy added
* Systemische Therapie Stellung zu operativer Therapie: extension in systemtherapie.groovy added

## CCP-IT JF 2023-09-21:

* Condition (Diagnoses) with an ICD10 code which starts not with c or d (which is not a tumor relevant diagnosis) is ignored.

## CCP-IT JF 2023-07-13:

* Encounter must not be exported anymore. The encounter resources and all references to encounter have been removed.
* Specimens must always be exported with aliquots and the parent reference to its aliquot group and master sample.
