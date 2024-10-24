![Kairos Logo](https://kairos.de/wp-content/uploads/2023/11/bildschirm_KAIROS_RGB_einfach-e1699976791799.png "Kairos Logo")

FHIR repository connect import mapping examples
======================

* This directory shows an example project with a set of groovy mappings, which can be used to connect an external FHIR store to a CentraXX system with
  a project specific FHIR profiling.
* It targets a common source profiling and results in CentraXX profiling: https://simplifier.net/centraxx-structures/
* The examples can be used as starting point. Feel free to extend scripts or combine examples for new projects.
* We love to receive feedback oin the form of E-Mails, GitHub issues or pull requests.

# General ideas

* In CXX the user selects always a single patient from the repository connect FHIR search result to be staged, merged and imported to CentraXX.
  Therefore, all bundle sources should contain only one source patient resource.
* It is possible to transform multiple resources together, but it should result in exactly one patient bundle entry.
* All other resources of all bundles of all scripts will be stored to the first patient. Other patients are ignored.
* It is possible to transform all target resources in one or multiple scripts. Here we show a multi script solution for more overview.
* Because the repository connect search will load only related resources to the selected patient, during transformation it is not needed by default to
  filter related data by subject again or specify a subject reference. It is not intended to move data between patients during transformation.  
