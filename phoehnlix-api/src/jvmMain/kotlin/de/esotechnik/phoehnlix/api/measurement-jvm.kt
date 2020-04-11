package de.esotechnik.phoehnlix.api

import de.esotechnik.phoehnlix.api.model.SenderId
import de.esotechnik.phoehnlix.util.toBigInt

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */

val SenderId.bridgeId get() = bytes.take(6).joinToString(separator = " ") { String.format("%02X", it) }

val SenderId.scaleId get() = sequenceOf(bytes.subList(6,9), bytes.subList(9,12))
  .flatMap { it.toBigInt().toString().padStart(8, '0').chunkedSequence(4) }
  .joinToString("-")