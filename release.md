How to release
==============
* If you have permissions. ;)
* For example v.1.6.0.

1. Resolve or release SNAPSHOT dependencies before, especially the fhir-dsl lib. Major versions should use the same version number as the fhir DSL.
   e.g. ```<kairos-fhir-dsl.version>1.6.0-SNAPSHOT</kairos-fhir-dsl.version>```
   becomes ```<kairos-fhir-dsl.version>1.6.0</kairos-fhir-dsl.version>```
2. Deploy the FHIR-DSL version to release to GitHub packages and make sure, that v.1.6.0 is available.
3. Push the updated How-To documents.   
4. run ```mvn release:prepare``` to create the feature tag and change the version to the next SNAPSHOT. 
   v.1.6.0-SNAPSHOT is tagged to 1.6.0 and becomes v.1.7.0-SNAPSHOT.
   run ```mvn release:clean``` to remove mvn release backup files.
5. Change the FHIR-DSL version in accordance to the next SNAPSHOT version.
6. Label the release tag by GitHub UI with the version number.
