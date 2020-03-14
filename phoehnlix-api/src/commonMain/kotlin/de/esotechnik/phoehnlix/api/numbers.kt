package de.esotechnik.phoehnlix.api

/**
 * @author Bernhard Frauendienst
 */

/**
 * Formats a number to a floating point value
 * with `digits` digits after the dot.
 *
 * @param digits number of digits, must be at least 1
 */
fun Number.formatDecimalDigits(digits: Int): String {
  require(digits > 0) { "digits must be at least 1"}
  val s = this.toString()
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