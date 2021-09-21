How to release
==============

* If you have permissions. ;)
* For example v.1.6.0.

# Checks

1. Resolve or release SNAPSHOT dependencies before, especially the fhir-dsl lib. Major versions should use the same version number as the fhir DSL.
   e.g. `<kairos-fhir-dsl.version>1.6.0-SNAPSHOT</kairos-fhir-dsl.version>`
   becomes `<kairos-fhir-dsl.version>1.6.0</kairos-fhir-dsl.version>`
2. Deploy the FHIR-DSL version to release to GitHub packages and make sure, that v.1.6.0 is available.
3. Push the updated How-To documents.

# Release

4. Create the bugfix branch, the release is the first bugfix version 1.6.0. Bugfixes will increase last version number 1.6.1, 1.6.2, etc.

``` mvn release:branch -DbranchName="bugfix-1.6" ```

The upcoming new version is `1.7.0-SNAPSHOT`.

6. Switch to the branch  `mvn release:branch -DbranchName="bugfix-1.6"` and perform QA etc.
7. Create the release tag

``` 
mvn release:prepare
mvn -Darguments="-Dmaven.javadoc.failOnError=false" release:perform
mvn release:clean 
```

7. Label the release tag by GitHub UI with the version number.
8. Change the FHIR-DSL version in accordance to the next SNAPSHOT version.
