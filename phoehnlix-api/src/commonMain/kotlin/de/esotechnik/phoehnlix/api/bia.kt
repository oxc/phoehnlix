package de.esotechnik.phoehnlix.api

import kotlin.math.sqrt

/**
 * @author Bernhard Frauendienst
 */
val Sex.factor get() = when (this) {
  Sex.Male -> 1
  Sex.Female -> 0
}

data class BIAData(val resistance: Float, val reactance: Float)

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

  val readableFFM get() = fatFreeMass.formatDecimalDigits(2)
  val readableTBW get() = totalBodyWater.formatDecimalDigits(2)
  val readableFatPercent get() = fatPercent.formatDecimalDigits(1) + "%"
  val readableWaterPercent get() = waterPercent.formatDecimalDigits(1) + "%"
  val readableBMI get() = bodyMassIndex.formatDecimalDigits(1)

  override fun toString() = """
        FFM: $readableFFM
        TBW: $readableTBW
        Fat%: $readableFatPercent
        Water%: $readableWaterPercent
        BMI: $readableBMI
    """.trimIndent()
}