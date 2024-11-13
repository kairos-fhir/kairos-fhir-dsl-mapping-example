package projects.patientfinder

import common.AbstractDslBuilderTest
import org.junit.jupiter.api.Test

import javax.annotation.Nullable

import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class MedicationSanitizeScaleTest extends AbstractDslBuilderTest {

  @Nullable
  static BigDecimal sanitizeScale(final String numeric) {
    try {
      return BigDecimal.valueOf(Double.parseDouble(numeric))
    } catch (final NumberFormatException | NullPointerException ignored) {
      return null
    }
  }

  @Test
   void testSanitizeScale() {
    assertNull(sanitizeScale(null))
    assertNull(sanitizeScale(""))
    assertTrue(BigDecimal.valueOf(1.0).compareTo(sanitizeScale("1.00")) == 0)
    assertTrue(BigDecimal.valueOf(1234).compareTo(sanitizeScale("1234")) == 0)
  }
}
