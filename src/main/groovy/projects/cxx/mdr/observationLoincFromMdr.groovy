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
 *  observationLoincFromMdr.json shows an example response for the laborValueCode 'Phosphat'
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

  context.source[laborMapping().laborFinding().laborFindingLaborValues()].each { final lflv ->
    component {
      code {
        final String laborValueCode = lflv[LaborFindingLaborValue.LABOR_VALUE]?.getAt(LaborValue.CODE) as String
        coding {
          system = "urn:centraxx"
          code = laborValueCode
        }

        final String loincCode = readFromCxxMdr(laborValueCode)
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

private static String readFromCxxMdr(final String laborValueCode) {
  final String bearerToken = getBearerToken()
  return queryMdr(bearerToken, laborValueCode)
}

/**
 * Gets an MDR OAuth2 bearer access token.
 * @return the token.
 */
private static String getBearerToken() {

  final String httpMethod = "POST"
  final String bodyMsg = "grant_type=password&username=install&password=kairos&scope=anyscope"
  final URL url = new URL("http://mdr-trunk-server.kairosbochum.de/oauth/token")

  final HttpURLConnection connection = url.openConnection() as HttpURLConnection
  connection.setRequestMethod(httpMethod)
  connection.setDoOutput(true)
  connection.setRequestProperty("Authorization", "Basic TURSX1VJOmthaXJvcw==") // Basic Auth contains client_id:client_secret, e.g. MDR_UI:kairos
  connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
  connection.setRequestProperty("Accept", "application/json")
  connection.getOutputStream().write(bodyMsg.getBytes(StandardCharsets.UTF_8))

  validateResponse(connection.getResponseCode(), httpMethod, url)
  final def json = connection.getInputStream().withCloseable { final inStream ->
    new JsonSlurper().parse(inStream as InputStream)
  }
  return json?.access_token
}

/**
 * Returns the MDR query result
 * @param bearerToken OAuth2 MDR Access Token
 * @param laborValueCode Speaking Labor Value Code in CXX
 * @return MDR definition code where caption.name ilike '%laborValueCode%' order by id offset 0 limit 1 (only the first if exists)
 */
private static String queryMdr(final String bearerToken, final String laborValueCode) {
  final String httpMethod = "GET"
  final String mdrQuery = "where%20caption.name%20ilike%20'%25" + laborValueCode + "%25'%20order%20by%20id%20limit%201%20offset%200"
  final URL url = new URL("http://mdr-trunk-server.kairosbochum.de/rest/v1/definitions?query=" + mdrQuery)

  final HttpURLConnection connection = url.openConnection() as HttpURLConnection
  connection.setRequestMethod(httpMethod)
  connection.setRequestProperty("Authorization", "Bearer " + bearerToken)
  connection.setRequestProperty("Accept", "application/json")

  validateResponse(connection.getResponseCode(), httpMethod, url)
  final def json = connection.getInputStream().withCloseable { final inStream ->
    new JsonSlurper().parse(inStream as InputStream)
  }
  return json?._embedded?.definitions?.iterator()?.next()?.id // id = code: the code contains usually the loinc code
}

/**
 * Validates the HTTP response. If a response is not valid (not 200), an exception is thrown and the transformation ends.
 */
private static void validateResponse(final int httpStatusCode, final String httpMethod, final URL url) {
  final int expectedStatusCode = 200
  if (httpStatusCode != expectedStatusCode) {
    throw new IllegalStateException("'" + httpMethod + "' request on '" + url + "' returned status code: " + httpStatusCode + ". Expected: " + expectedStatusCode)
  }
}

