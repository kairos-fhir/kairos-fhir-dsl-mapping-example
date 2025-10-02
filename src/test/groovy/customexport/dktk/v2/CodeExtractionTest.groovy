package customexport.dktk.v2

import org.junit.jupiter.api.Test

import javax.annotation.Nullable

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNull

class CodeExtractionTest {

  @Test
  void testThatCodesAreExtractedCorrectly() {
    assertEquals("code", extractCode("code"))
    assertEquals("code", extractCode("  code   "))
    assertEquals("code", extractCode("code code2 code3"))
    assertNull(extractCode(null))
    assertNull(extractCode("   "))
  }

  // Method under test (used in tnmp.groovy script)
  @Nullable
  private static String extractCode(@Nullable final String s) {
    if (s == null || s.trim().isEmpty()) return null
    final String[] parts = s.trim().split("\\s+")
    return parts.length > 0 ? parts[0] : null
  }

}
