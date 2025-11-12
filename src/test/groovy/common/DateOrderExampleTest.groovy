package common

import org.hl7.fhir.r4.model.DateTimeType
import org.hl7.fhir.r4.model.DateType
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * Just an example for unit testing within this project
 */
class DateOrderExampleTest {

  @Test
  void testThatFirstAndLastDateIsFound() {

    final def dateStrings = ["2021-07-03", "2021-07-01", "2021-07-07", "2021-07-02"]

    final def sortedList = dateStrings.sort()

    assertEquals("2021-07-01", sortedList.first())
    assertEquals("2021-07-07", sortedList.last())
  }

  @Test
  void testReasonableDate(){

    final def date = "0001-01-01"

    final DateType dateTimeType = new DateType(date)

    println(dateTimeType.getValue())
  }
}
