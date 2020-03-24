package de.esotechnik.phoehnlix.dataservice

import de.esotechnik.phoehnlix.model.*
import de.esotechnik.phoehnlix.util.toBigInt
import kotlinx.atomicfu.atomic
import java.time.Instant

private val REQUEST_24: Byte = 0x24
private val TIMESTAMP_OFFSET = Instant.parse("2010-01-01T00:00:00.00Z")

/**
 * @author Bernhard Frauendienst
 */
object WebConnectProtocol {
  fun parseMeasurementData(raw: String): MeasurementData {
    // ?data=24 xxxxxxxxxxxx xxxxxx xxxxxx 01b8 132af242 23b4 0ee3 109f 0000000000 xxxxxxxx
    // ?data=24 xxxxxxxxxxxx xxxxxx xxxxxx 01b8 132b760e 233c 0ce0 0ee8 0000000000 xxxxxxxx
    //       ^ request type  ^ scale id 1-3 ^?  ^ timestamp   ^ imp50              ^ crc32
    //          ^ bridge id 1-6     ^ scale id 4-6       ^ weight  ^ imp5
    val i = atomic(0)
    fun nextBytes(n: Int) = raw.substring(i.value, i.addAndGet(n * 2)).hexStringBytes()
    fun next(n: Int) = nextBytes(n).toBigInt()
    require(nextBytes(1).single() == REQUEST_24) { "Request must be 0x24" }
    val senderId = SenderId(nextBytes(12))
    val _unknown = next(2)
    val timestamp = TIMESTAMP_OFFSET.plusSeconds(next(4).toLong())
    val weight = next(2).toDouble() / 100
    val imp50 = next(2).takeUnless { it.signum() == 0 }?.let { it.toDouble() / 10 }
    val imp5 = next(2).takeUnless { it.signum() == 0 }?.let { it.toDouble() / 10 }
    val biaData = if (imp50 != null && imp5 != null) {
      BIAData(imp50, imp5)
    } else null

    return MeasurementData(
      senderId = senderId,
      timestamp = timestamp,
      weight = weight,
      biaData = biaData
    )
  }

  fun serializeTimestamp(instant: Instant = Instant.now()): String {
    val seconds = instant.epochSecond - TIMESTAMP_OFFSET.epochSecond
    return seconds.toString(16).padStart(8, '0')
  }
}

class Request {

}