package de.esotechnik.phoehnlix.util

import kotlin.math.pow
import kotlin.math.roundToLong


/**
 * @author Bernhard Frauendienst
 */
fun Double.roundToDigits(digits: Int): Double {
  val fac = when (digits) {
    1 -> 10.0
    2 -> 100.0
    3 -> 1000.0
    else -> 10.0.pow(digits)
  }
  return (this * fac).roundToLong() / fac
}

/**
 * @author Bernhard Frauendienst
 */

/**
 * Formats a number to a floating point value
 * with `digits` digits after the dot.
 *
 * @param digits number of digits, must be at least 1
 */
fun Double.formatDecimalDigits(digits: Int): String {
  require(digits > 0) { "digits must be at least 1"}
  val s = roundToDigits(digits).toString()
  val dotPos = s.indexOf('.')
  val targetLen: Int
  val dotS = if (dotPos == -1) {
    targetLen = s.length + 1 + digits
    "$s."
  } else {
    targetLen = dotPos + 1 + digits
    s
  }
  return if (targetLen > dotS.length) {
    dotS.padEnd(targetLen, '0')
  } else {
    dotS.substring(0, targetLen)
  }
}

fun Double.formatDecimalDigits(digits: Int, suffix: String): String {
  return formatDecimalDigits(digits) + suffix
}