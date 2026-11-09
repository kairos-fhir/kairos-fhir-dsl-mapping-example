How to release
==============

* If you have permissions. ;)
* For example v.1.58.0.

# Checks

1. Resolve or release SNAPSHOT dependencies before, especially the fhir-dsl lib. Major versions should use the same version number as the fhir DSL.
   e.g. `<kairos-fhir-dsl.version>2.0.0-SNAPSHOT</kairos-fhir-dsl.version>`
   becomes `<kairos-fhir-dsl.version>2.0.0</kairos-fhir-dsl.version>`
2. Deploy the FHIR-DSL version to release to GitHub packages and make sure, that v.2.0.0 is available.
3. Push the updated How-To documents.

# Release

4. Create the bugfix branch, the release is the first bugfix version 2.0.0. Bugfixes will increase last version number 2.0.1, 2.0.2, etc.

``` 
mvn release:branch -DbranchName="bugfix-2.0" 
```

The upcoming new version is `2.1.0-SNAPSHOT`.

6. Switch to the branch release branch and perform QA etc.

```
git checkout bugfix-2.0
```

7. Create the release tag

``` 
mvn release:prepare
mvn release:clean 
```

There is no need for mvn release:perform 

Switch back to master

```
git checkout master
```

7. Label the release tag by GitHub UI with the version number.
8. Change the FHIR-DSL version in accordance to the next SNAPSHOT version.
