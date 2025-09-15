<img src="/docs/images/Logo.png" width="250" alt="IQVIA Logo"/>

Export to a bundle with multiple FHIR resources from one HDRP entity
========================
**Do not use it in production!**

* Creates one Bundle for every patient of a FHIR-custom-export containing all the resources of one patient

**Procedure**
1. Read all the patient files of a FHIR-custom export and creates a new aggreagte file for every exported patient.
2. Read all the other exported resource files and distributes the entries to the previously initiates aggregate files by subject reference.
3. Cleaning up export directory.