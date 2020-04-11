package de.esotechnik.phoehnlix.apiservice.util

import java.time.Instant

/**
 * @author Bernhard Frauendienst
 */
fun String.toInstant(): Instant {
  return Instant.parse(this)
}
