package common

import de.kairos.fhir.dsl.r4.context.Context
import org.hl7.fhir.r4.model.DomainResource
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * This annotation can be used on test methods that shall run parametrized in tests that extend
 * {@link AbstractExportScriptTest}
 * This annotation marks test that run as parametrized test. Such test must declare two parameters,
 * of type {@link Context} an one of a type that extends {@link DomainResource}
 * These arguments will be supplied by {@link AbstractExportScriptTest#getTestData} method and run
 * the test method on all supplied parameter pairs.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ParameterizedTest(name = "{0}")
@MethodSource(AbstractExportScriptTest.METHOD_SOURCE)
@interface ExportScriptTest {}