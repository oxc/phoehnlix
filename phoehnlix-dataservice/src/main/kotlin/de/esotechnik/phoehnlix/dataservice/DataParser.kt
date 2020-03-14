package de.esotechnik.phoehnlix.dataservice

import de.esotechnik.phoehnlix.api.BIAData
import de.esotechnik.phoehnlix.api.BIAResults
import de.esotechnik.phoehnlix.api.ProfileData
import kotlinx.atomicfu.atomic
import java.math.BigInteger
import java.time.Instant

private val REQUEST_24: Byte = 0x24
private val TIMESTAMP_OFFSET = Instant.parse("2010-01-01T00:00:00.00Z")

/**
 * @author Bernhard Frauendienst
 */
object DataParser {
  fun parseData(raw: String): Data {
    // ?data=24 xxxxxxxxxxxx xxxxxx xxxxxx 01b8 132af242 23b4 0ee3 109f 0000000000 xxxxxxxx
    // ?data=24 xxxxxxxxxxxx xxxxxx xxxxxx 01b8 132b760e 233c 0ce0 0ee8 0000000000 xxxxxxxx
    //       ^ request type  ^ scale id 1-3 ^?  ^ timestamp   ^ R  ^ Xc            ^ crc32
    //          ^ bridge id 1-6     ^ scale id 4-6       ^ weight
    val i = atomic(0)
    fun next(n: Int) = BigInteger(raw.substring(i.value, i.addAndGet(n * 2)), 16)
    require(next(1).byteValueExact() == REQUEST_24) { "Request must be 0x24" }
    val bridgeId = BridgeId(next(6))
    val scaleId = ScaleId(next(3), next(3))
    val _unknown = next(2)
    val timestamp = TIMESTAMP_OFFSET.plusSeconds(next(4).toLong())
    val weight = next(2).toFloat() / 100
    // this is actually just a guess. Let's see if these values make sense
    val resistance = next(2).takeUnless { it.signum() == 0 }?.let { it.toFloat() / 10 }
    val reactance = next(2).takeUnless { it.signum() == 0 }?.let { it.toFloat() / 100 }
    val biaData = if (resistance != null && reactance != null) {
      BIAData(resistance, reactance)
    } else null

    return Data(
      bridgeId = bridgeId,
      scaleId = scaleId,
      timestamp = timestamp,
      weight = weight,
      biaData = biaData
    )
  }
}

data class Data(
  val bridgeId: BridgeId,
  val scaleId: ScaleId,
  val timestamp: Instant,
  val weight: Float,
  val biaData: BIAData?
) {
  val readableWeight get() = String.format("%.1f kg", weight)

  fun calculateBIAResults(profile: ProfileData) = biaData?.let { BIAResults(it, weight, profile) }

  fun toLogString() = """
        Bridge: $bridgeId
        Scale: $scaleId
        Timestamp: $timestamp
        Weight: $readableWeight
        BIA: $biaData
        """.trimIndent()
}

data class BridgeId(private val id: BigInteger) {
  override fun toString() = id.toByteArray()
    .takeLast(6)
    .joinToString(separator = " ") { String.format("%02X", it) }
}

data class ScaleId(private val first: BigInteger, private val second: BigInteger) {
  override fun toString(): String = sequenceOf(first, second)
    .flatMap { it.toString().padStart(8, '0').chunkedSequence(4) }
    .joinToString("-")
}
