<img src="/docs/images/Logo.png" width="250" alt="IQVIA Logo"/>

Laboratory values and vital parameters
============================================

This directory contains groovy scripts for the FHIR profiles defined 
as "laboratory values" or "vital parameters". 
Contrary to all other profiles, the data for these profiles 
is recorded as measurement profiles and not CRFs in CentraXX.
Hence, they were separated from the rest.

In order to use these groovy scripts, they have to be place in the project
folder that also contains the ExportResourceMappingConfig.json file which 
also has to be appended by the mappings written in the 
ExportResourceMappingConfig.json from this directory.



