@file:Suppress("NAME_SHADOWING", "NOTHING_TO_INLINE")

package de.esotechnik.phoehnlix.model

import de.esotechnik.phoehnlix.util.formatDecimalDigits

/**
 * @author Bernhard Frauendienst
 */
data class BIAData(
  val imp50: Double,
  val imp5: Double
)

data class BIAResults(
  val bodyFatPercent: Double,
  val bodyWaterPercent: Double,
  val muscleMassPercent: Double,
  val bodyMassIndex: Double,
  val metabolicRate: Double
) {

  val readableBodyFatPercent get() = bodyFatPercent.formatDecimalDigits(1) + "%"
  val readableBodyWaterPercent get() = bodyWaterPercent.formatDecimalDigits(1) + "%"
  val readableMuscleMassPercent get() = muscleMassPercent.formatDecimalDigits(1) + "%"
  val readableBMI get() = bodyMassIndex.formatDecimalDigits(1)
  val readableMetabolicRate get() = metabolicRate.formatDecimalDigits(1) + "kcal"

  override fun toString() = """
        Fat: $readableBodyFatPercent
        Water: $readableBodyWaterPercent
        Muscle: $readableMuscleMassPercent
        BMI: $readableBMI
        Metabolic Rate: $readableMetabolicRate
    """.trimIndent()
}