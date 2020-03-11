package de.esotechnik.phoehnlix.dataservice

import kotlinx.atomicfu.atomic
import java.math.BigInteger
import java.time.Instant
import kotlin.math.sqrt

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
        fun next(n: Int) = BigInteger(raw.substring(i.value, i.addAndGet(n*2)), 16)
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
data class BIAData(val resistance: Float, val reactance: Float)
enum class Sex(val factor: Byte) {
    Male(1), Female(0)
}

data class ProfileData(val height: Float, val age: Int, val sex: Sex)


class BIAResults(biaData: BIAData, weight: Float, profile: ProfileData) {
    private val resistance = biaData.resistance
    private val reactance = biaData.reactance
    private val impedance = sqrt(resistance*resistance + reactance*reactance)
    private val sex = profile.sex.factor
    private val age = profile.age
    private val height = profile.height
    private val heightSqr = height.let { it * it }
    private val heightMeterSqr = (height /100.0).let { it * it }
    private val heightSqrByRes = heightSqr / resistance

    val fatFreeMass = // Kyle et al.
        -4.104 + 0.518*heightSqrByRes + 0.231*weight + 0.130*reactance + 4.229* sex

    //val totalBodyWater = // Heitmann.
    //    -17.58 + 0.240*heightSqrByRes - 0.172*weight + 0.040*sex*weight+0.165 * height
    val totalBodyWater = // Deurenberg et al.
        -6.653 + 0.36740*heightSqr/impedance + 0.17531*weight - 0.11*age + 2.83*sex
    
    val fatPercent = 100 - fatFreeMass/weight*100.0
    val waterPercent = totalBodyWater/weight*100.0

    val bodyMassIndex = weight / heightMeterSqr

    val readableFFM get() = String.format("%.2f", fatFreeMass)
    val readableTBW get() = String.format("%.2f", totalBodyWater)
    val readableFatPercent get() = String.format("%.2f%%", fatPercent)
    val readableWaterPercent get() = String.format("%.2f%%", waterPercent)
    val readableBMI get() = String.format("%.1f", bodyMassIndex)

    override fun toString() = """
        FFM: $readableFFM
        TBW: $readableTBW
        Fat%: $readableFatPercent
        Water%: $readableWaterPercent
        BMI: $readableBMI
    """.trimIndent()
}