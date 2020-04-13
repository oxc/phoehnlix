package de.esotechnik.phoehnlix.util

import org.testng.annotations.Test
import kotlin.test.assertEquals

/**
 * @author Bernhard Frauendienst
 */
internal class NumbersKtTest {

  @Test
  fun `doubles with more digits are shortened`() {
    assertFormatsTo(12.345, 2, "12.35")
    assertFormatsTo(5.4321, 2, "5.43")
    assertFormatsTo(5.4321, 3, "5.432")
  }

  @Test
  fun `doubles with right digits stay the same`() {
    assertFormatsTo(12.34, 2, "12.34")
    assertFormatsTo(5.43, 2, "5.43")
  }

  @Test
  fun `doubles with less digits are padded`() {
    assertFormatsTo(12.3, 2, "12.30")
    assertFormatsTo(5.4, 3, "5.400")
    assertFormatsTo(42.toDouble() , 1, "42.0")
  }

  @Suppress("NOTHING_TO_INLINE")
  private inline fun assertFormatsTo(num: Double, digits: Int, expected: String) {
    assertEquals(expected, num.formatDecimalDigits(digits))
  }
}