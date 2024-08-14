package common

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

class BigDecimalTest {

  @Test
  void convertToString() {
    assertEquals("100", new BigDecimal("100").toString())
    assertEquals("100", new BigDecimal("100").toPlainString())

    assertEquals("100", new BigDecimal("100").stripTrailingZeros().toPlainString())
    assertEquals("100", new BigDecimal("100.0000000000").stripTrailingZeros().toPlainString())

    assertEquals("1E+2", new BigDecimal("100").stripTrailingZeros().toString())
    assertEquals("1E+2", new BigDecimal("100.0000000000").stripTrailingZeros().toString())
  }
}
