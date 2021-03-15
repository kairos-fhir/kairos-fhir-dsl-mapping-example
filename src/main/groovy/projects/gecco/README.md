### German Corona Consensus (GECCO) FHIR Mapping
This project aims to provide a mapping from CXX ressources to the GECCO dataset.
Profile definitions can be reviewed [here](https://simplifier.net/forschungsnetzcovid-19)
The mappings shown in this project are working examples. In order to use them in your CXX instance, they have to be adjusted (i.e. changing Labor-Finding CODES to match the ones from your CXX instance).

---

## Multi-profile groovy scripts
If not disclosed here, every script maps one single profile. Exceptions are listed below.

*chronicDiseases.groovy* maps:
 *  Cardiovascular Disease
 *  Chronic Kidney Disease
 *  Chronic Liver Disease
 *  Chronic Lung Disease
 *  Chronic Neurological or Mental Disease
 *  Diabetes Mellitus
 *  Gastrointestinal Ulcers
 *  Human Immunodeficiency Virus Disease
 *  Rheumatic Immunological Diseases 


## Referencing groovy scripts
Profiles that contain references to other profiles need to be imported prior to these referenced profiles. 
This order has to be configured in the *ExportResourceMappingConfig.json* file, which is also present in this directory.
Groovy-scripts that map profiles containing these references to other profiles are detailled here.

*observationBloodGasPanel.groovy* contains references to:
 * observationOxygenSaturation
 * observationPaCO2
 * observationPaO2
 * observationFiO2
 * observation_pH