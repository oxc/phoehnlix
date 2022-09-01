package de.esotechnik.phoehnlix.frontend.util

/**
 * @author Bernhard Frauendienst
 */

fun entrySequence(obj: dynamic): Sequence<Pair<String, dynamic>> {
  val keyIt = js("Object").keys(obj).iterator()
  return generateSequence {
    if (keyIt.hasNext()) {
      keyIt.next().let { key -> key to obj[key] }
    } else null
  }
}