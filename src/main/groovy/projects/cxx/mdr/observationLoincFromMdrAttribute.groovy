package projects.cxx.mdr

import de.kairos.fhir.centraxx.metamodel.AbstractCatalog
import de.kairos.fhir.centraxx.metamodel.CatalogEntry
import de.kairos.fhir.centraxx.metamodel.IcdEntry
import de.kairos.fhir.centraxx.metamodel.LaborFindingLaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValue
import de.kairos.fhir.centraxx.metamodel.LaborValueNumeric
import de.kairos.fhir.centraxx.metamodel.Unity
import de.kairos.fhir.centraxx.metamodel.enums.LaborValueDType
import groovy.json.JsonSlurper
import org.hl7.fhir.r4.model.Observation
import org.slf4j.LoggerFactory

import java.nio.charset.StandardCharsets

import static de.kairos.fhir.centraxx.metamodel.RootEntities.laborMapping

/**
 * Represented by a CXX LaborMapping
 * @author Mike Wähnert
 * @since v.1.7.0, CXX.v.3.17.2
 *
 * Hints:
 *  Example covers no Enumerations and RadioOptionGroups yet
 *  The example shows, how to enrich FHIR messages with LOINC codes from a CentraXX MDRs
 *  TODO: change MDR server url and credentials basic auth (client_id:client_secret), username and password
 */
observation {

  id = "Observation/" + context.source[laborMapping().laborFinding().id()]

  status = Observation.ObservationStatus.UNKNOWN

  code {
    coding {
      system = "urn:centraxx"
      code = context.source[laborMapping().laborFinding().shortName()] as String
    }
  }

  effectiveDateTime {
    date = context.source[laborMapping().laborFinding().findingDate().date()]
  }

  subject {
    reference = "Patient/" + context.source[laborMapping().relatedPatient().id()]
  }

  method {
    coding {
      system = "urn:centraxx"
      version = context.source[laborMapping().laborFinding().laborMethod().version()]
      code = context.source[laborMapping().laborFinding().laborMethod().code()] as String
    }
  }

  final String mdrBaseUrl = "http://mdr-trunk-server.kairosbochum.de"
  final String bearerToken = getBearerToken(mdrBaseUrl)
  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->
    component {
      code {
        final String laborValueCode = lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE) as String
        coding {
          system = "urn:centraxx"
          code = laborValueCode
        }

        final String loincCode = readFromCxxMdr(mdrBaseUrl, bearerToken, laborValueCode)
        if (loincCode) {
          coding {
            system = "http://loinc.org"
            code = loincCode
          }
        }
      }

      if (isNumeric(lflv)) {
        valueQuantity {
          value = lflv[LaborFindingLaborValue.NUMERIC_VALUE]
          unit = lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValueNumeric.UNIT)?.getAt(Unity.CODE) as String
        }
      }
      if (isBoolean(lflv)) {
        valueBoolean(lflv[LaborFindingLaborValue.BOOLEAN_VALUE] as Boolean)
      }

      if (isDate(lflv)) {
        valueDateTime {
          date = lflv[LaborFindingLaborValue.DATE_VALUE]
        }
      }

      if (isTime(lflv)) {
        valueTime(lflv[LaborFindingLaborValue.TIME_VALUE] as String)
      }

      if (isString(lflv)) {
        valueString(lflv[LaborFindingLaborValue.STRING_VALUE] as String)
      }

      if (isCatalog(lflv)) {
        valueCodeableConcept {
          lflv[LaborFindingLaborValue.CATALOG_ENTRY_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/ValueList-" + entry[CatalogEntry.CATALOG]?.getAt(AbstractCatalog.ID)
              code = entry[CatalogEntry.CODE] as String
            }
          }
          lflv[LaborFindingLaborValue.ICD_ENTRY_VALUE].each { final entry ->
            coding {
              system = "urn:centraxx:CodeSystem/IcdCatalog-" + entry[IcdEntry.CATALOGUE]?.getAt(AbstractCatalog.ID)
              code = entry[IcdEntry.CODE] as String
            }
          }
        }
      }
    }
  }
}

private static boolean isDTypeOf(final Object lflv, final List<LaborValueDType> types) {
  return types.contains(lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.D_TYPE) as LaborValueDType)
}

private static boolean isBoolean(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.BOOLEAN])
}

private static boolean isNumeric(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.INTEGER, LaborValueDType.DECIMAL, LaborValueDType.SLIDER])
}


private static boolean isDate(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.DATE, LaborValueDType.LONGDATE])
}

private static boolean isTime(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.TIME])
}

private static boolean isEnumeration(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.ENUMERATION])
}

private static boolean isString(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.STRING, LaborValueDType.LONGSTRING])
}

private static boolean isCatalog(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.CATALOG])
}

private static boolean isOptionGroup(final Object lflv) {
  return isDTypeOf(lflv, [LaborValueDType.OPTIONGROUP])
}

/**
 * Queries the MDR
 * @param bearerToken an MDR OAuth2 bearerToken
 * @param laborValueCode the laborValue code to query
 * @return the LOINC Code, if exists or null, if none exists.
 */
private static String readFromCxxMdr(final String mdrBaseUrl, final String bearerToken, final String laborValueCode) {
  final String version = queryDefinitionVersionById(mdrBaseUrl, bearerToken, laborValueCode)
  if (!version) {
    return null
  }

  final String tag = queryAttributeTagByCodeAndVersion(mdrBaseUrl, bearerToken, laborValueCode, version)
  if (!version) {
    return null
  }

  return queryCatalogEntryCodeByAttributeTag(mdrBaseUrl, bearerToken, tag)
}

/**
 * Gets an MDR OAuth2 bearer access token.
 * For an example response see /responses/authToken.json
 * @return the token.
 */
private static String getBearerToken(final String mdrBaseUrl) {

  final String httpMethod = "POST"
  final String bodyMsg = "grant_type=password&username=install&password=kairos&scope=anyscope"
  final URL url = new URL(mdrBaseUrl + "/oauth/token")

  final HttpURLConnection connection = url.openConnection() as HttpURLConnection
  connection.setRequestMethod(httpMethod)
  connection.setDoOutput(true)
  connection.setRequestProperty("Authorization", "Basic TURSX1VJOmthaXJvcw==") // Basic Auth contains client_id:client_secret, e.g. MDR_UI:kairos
  connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
  connection.setRequestProperty("Accept", "application/json")
  connection.getOutputStream().write(bodyMsg.getBytes(StandardCharsets.UTF_8))

  if (!validateResponse(connection.getResponseCode(), httpMethod, url)) {
    throw new IllegalStateException("No auth token received")
  }
  final def json = connection.getInputStream().withCloseable { final inStream ->
    new JsonSlurper().parse(inStream as InputStream)
  }
  return json?.access_token
}

/**
 * For an example response see /responses/getDefinitionByCode.json
 */
private static String queryDefinitionVersionById(final String mdrBaseUrl, final String bearerToken, final String laborValueCode) {
  final String httpMethod = "GET"
  final URL url = new URL(mdrBaseUrl + "/rest/v1/definitions/definition?code=" + laborValueCode)

  final def json = queryMdr(url, httpMethod, bearerToken)
  return json?.version
}

/**
 * For an example response see /responses/getAttributeByCodeAndVersion.json
 */
private static String queryAttributeTagByCodeAndVersion(final String mdrBaseUrl, final String bearerToken, final String laborValueCode, final String version) {
  final String httpMethod = "GET"
  final URL url = new URL(mdrBaseUrl + "/rest/v1/definitions/attribute/definition/version?code=" + laborValueCode + "&version=" + version + "&domainCode=Annotation&attributeCode=LOINC")

  final def json = queryMdr(url, httpMethod, bearerToken)
  return json?.value?.iterator()?.next()
}

/**
 * For an example response see /responses/getCatalogElementByAttributeValue.json
 */
private static String queryCatalogEntryCodeByAttributeTag(final String mdrBaseUrl, final String bearerToken, final String tag) {
  final String httpMethod = "GET"
  final URL url = new URL(mdrBaseUrl + "/rest/v1/catalogs/entry/tag?tag=" + tag)

  final def json = queryMdr(url, httpMethod, bearerToken)
  return json?.code
}

/**
 * Executes the REST query, validates and returns the result, if exists.
 * @return JsonSlurper with the REST response or null, if response was not valid.
 */
private static def queryMdr(final URL url, final String httpMethod, final String bearerToken) {
  final HttpURLConnection connection = url.openConnection() as HttpURLConnection
  connection.setRequestMethod(httpMethod)
  connection.setRequestProperty("Authorization", "Bearer " + bearerToken)
  connection.setRequestProperty("Accept", "application/json")

  if (!validateResponse(connection.getResponseCode(), httpMethod, url)) {
    return null
  }

  return connection.getInputStream().withCloseable { final inStream ->
    new JsonSlurper().parse(inStream as InputStream)
  }
}

/**
 * Validates the HTTP response
 * @return true, if status code is valid 200 or otherwise false. A false response is logged.
 */
private static boolean validateResponse(final int httpStatusCode, final String httpMethod, final URL url) {
  final int expectedStatusCode = 200
  if (httpStatusCode != expectedStatusCode) {
    LoggerFactory.getLogger(getClass()).warn("'" + httpMethod + "' request on '" + url + "' returned status code: " + httpStatusCode + ". Expected: " + expectedStatusCode)
    return false
  }
  return true
}
