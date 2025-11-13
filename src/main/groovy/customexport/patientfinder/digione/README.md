<img src="/docs/images/Logo.png" width="250" alt="IQVIA Logo"/>

IQVIA HDRP to CTcue PatientFinder Mappings
========================

Mappings can be used to export data in IqTrial HDRP to PatientFinder system

# Change log

# 2025-11-13
* added scripts to map biomarkers finding to specimen and observation
* deleted obsolete scripts

# 2025-11-06
* remapped medications to medicationAdministration
* remapped RadioTherapy to MedicationAdministration only

# 2025-11-04
* added the surgical procedure category code to mark procedures as surgeries

# 2025-11-03
* added truncation to restrict IcdEntry description to max 500 chars.

# 2025-10-28
* added scripts to transform RadiationTherapy to medications and medicationAdministrations

# 2025-10-23
* added metamodel usage in tnm for sourceDict

# 2025-10-25
* added export of ECOG score to Condition.clinicalStatus
* added script for MedicationRequests

# 2025-09-22
* overworking the Tnm script

# 2025-09-17
* added filter to observation to remove diagnosis finding from export
* added export of diagnosisDate to Condition.onsetDateTime

# 2025-08-05
* added patient_ID as exported primary id

# 2025-07-24
* fixed multilingual accessing
* fixed entity type for med procedure in ExportResourceMappingConfig.json

# 2025-07-22 
* add fixed multilingual access on catalog entry

# 2025-06-27
* added project config

# 2025-04-10
* added export of catalogs in findings

# 2025-04-09
* initial mappings derived from HULL project

