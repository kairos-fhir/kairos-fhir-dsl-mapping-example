package common

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Use on {@link AbstractExportScriptTest} to validate the resulting FHIR resource against the specified FHIR package.
 * FHIR packages can be downloaded for specific projects from https://simplifier.net/
 * @param String packageDir Path to the FHIR package used for validation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface Validate {
  String packageDir()
}