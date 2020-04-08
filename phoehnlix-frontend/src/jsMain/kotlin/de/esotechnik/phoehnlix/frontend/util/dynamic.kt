package de.esotechnik.phoehnlix.frontend.util

import kotlinext.js.Object

/**
 * @author Bernhard Frauendienst
 */

fun entrySequence(obj: dynamic): Sequence<Pair<String, dynamic>> {
  val keyIt = Object.keys(obj).iterator()
  return generateSequence {
    if (keyIt.hasNext()) {
      keyIt.next().let { it to obj[it] }
    } else null
  }
}