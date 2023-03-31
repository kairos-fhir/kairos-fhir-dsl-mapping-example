![Kairos Logo](https://www.kairos.de/app/uploads/kairos-logo-blue_iqvia.png "Kairos Logo")

Export to a custom json format
========================
**Do not use it in production!**

* Export a custom JSON structure instead of FHIR.
* [patient.groovy](patient.groovy) creates one file per patient with a predefined structure. Here an example: [Patient-101.json](Patient-101.json)
* [specimen.groovy](specimen.groovy) creates one file per sample with the full initialized sample data set.
