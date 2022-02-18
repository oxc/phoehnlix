package de.esotechnik.phoehnlix.frontend.util

/**
 * @author Bernhard Frauendienst
 */

fun entrySequence(obj: dynamic): Sequence<Pair<String, dynamic>> {
  val entryIt = js("Object").entries(obj).iterator()
  return generateSequence {
    if (entryIt.hasNext()) {
      val kv = entryIt.next()
      val key = kv[0] as String
      val value = kv[1]
      key to value
    } else null
  }
}