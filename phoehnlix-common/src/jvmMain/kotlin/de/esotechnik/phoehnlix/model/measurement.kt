package de.esotechnik.phoehnlix.model

import de.esotechnik.phoehnlix.util.toBigInt
import java.time.Instant

/**
 * @author Bernhard Frauendienst
 */
data class MeasurementData(
  val senderId: SenderId,
  val timestamp: Instant,
  val weight: Double,
  val biaData: BIAData?
) {
  val readableWeight get() = String.format("%.1f kg", weight)

  fun toLogString() = """
        Sender: $senderId
        Timestamp: $timestamp
        Weight: $readableWeight
        BIA: $biaData
        """.trimIndent()
}

data class SenderId(val bytes: List<Byte>) {
  init { require(bytes.size == 12) }

  val bridgeId get() = bytes.take(6).joinToString(separator = " ") { String.format("%02X", it) }

  val scaleId get() = sequenceOf(bytes.subList(6,9), bytes.subList(9,12))
    .flatMap { it.toBigInt().toString().padStart(8, '0').chunkedSequence(4) }
    .joinToString("-")

  override fun toString() = "Bridge: $bridgeId Scale: $scaleId"
}
