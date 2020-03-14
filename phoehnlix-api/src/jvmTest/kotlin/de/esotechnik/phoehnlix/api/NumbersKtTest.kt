package de.esotechnik.phoehnlix.api

import org.testng.annotations.Test
import kotlin.test.Asserter
import kotlin.test.assertEquals

/**
 * @author Bernhard Frauendienst
 */
internal class NumbersKtTest {

  @Test
  fun `floats with more digits are shortened`() {
    assertFormatsTo(12.345, 2, "12.34")
    assertFormatsTo(5.4321, 2, "5.43")
    assertFormatsTo(5.4321, 3, "5.432")
  }

  @Test
  fun `floats with right digits stay the same`() {
    assertFormatsTo(12.34, 2, "12.34")
    assertFormatsTo(5.43, 2, "5.43")
  }

  @Test
  fun `floats with less digits are padded`() {
    assertFormatsTo(12.3, 2, "12.30")
    assertFormatsTo(5.4, 3, "5.400")
    assertFormatsTo(42f, 1, "42.0")
  }

  @Test
  fun `ints are padded`() {
    assertFormatsTo(12, 2, "12.00")
    assertFormatsTo(5, 3, "5.000")
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun assertFormatsTo(num: Number, digits: Int, expected: String) {
    assertEquals(expected, num.formatDecimalDigits(digits))
  }
}