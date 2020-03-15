@file:Suppress("NAME_SHADOWING", "NOTHING_TO_INLINE")

package de.esotechnik.phoehnlix.model

/**
 * @author Bernhard Frauendienst
 */
data class BIAData(val imp50: Float, val imp5: Float)

data class BIAResults(
  val bodyFatPercent: Float,
  val bodyWaterPercent: Float,
  val muscleMassPercent: Float,
  val bodyMassIndex: Float,
  val metabolicRate: Float
) {

  val readableBodyFatPercent get() = bodyFatPercent.formatDecimalDigits(1) + "%"
  val readableBodyWaterPercent get() = bodyWaterPercent.formatDecimalDigits(1) + "%"
  val readableMuscleMassPercent get() = muscleMassPercent.formatDecimalDigits(1) + "%"
  val readableBMI get() = bodyMassIndex.formatDecimalDigits(1)
  val readableMetabolicRate get() = metabolicRate.toInt().toString()

  override fun toString() = """
        Fat: $readableBodyFatPercent
        Water: $readableBodyWaterPercent
        Muscle: $readableMuscleMassPercent
        BMI: $readableBMI
        Metabolic Rate: $readableMetabolicRate
    """.trimIndent()
}

private operator fun <T> Sex.invoke(female: T, male: T): T = when (this) {
  Sex.Male -> male
  Sex.Female -> female
}

fun calculateBIAResults(biaData: BIAData, weight: Float, profile: ProfileData): BIAResults {
  val imp50 = biaData.imp50
  val imp5 = biaData.imp5
  val activityLevel = profile.activityLevel
  val sex = profile.sex
  val age = profile.age
  val height = profile.height

  val bodyMassIndex = run {
    10000.0 * weight / (height * height)
  }

  val bodyFatPercent = run {
    val activityCorrFac = when (activityLevel) {
      ActivityLevel.VeryLow, ActivityLevel.Low, ActivityLevel.Normal -> 0.0
      ActivityLevel.High -> sex(female = 2.3, male = 2.5)
      ActivityLevel.VeryHigh -> sex(female = 4.1, male = 4.3)
    }
    val sexCorrFac = sex(female = 0.214, male = 0.251)
    val activitySexDiv = sex(female = 55.1, male = 65.5)
    1.847 * bodyMassIndex + sexCorrFac * age + 0.062 * imp50 - (activitySexDiv - activityCorrFac)
  }

  val bodyWaterPercent = run {
    val activityCorrFac = when (activityLevel) {
      ActivityLevel.VeryLow, ActivityLevel.Low, ActivityLevel.Normal -> sex(female = 0.0, male = 2.83)
      ActivityLevel.High -> sex(female = 0.4, male = 3.93)
      ActivityLevel.VeryHigh -> sex(female = 1.4, male = 5.33)
    }
    (0.3674 * height * height / imp50 + 0.17531 * weight - 0.11 * age + (6.53 + activityCorrFac)) / weight * 100.0
  }

  val muscleMassPercent = run {
    val activityCorrFac = when (activityLevel) {
      ActivityLevel.VeryLow, ActivityLevel.Low, ActivityLevel.Normal -> sex(female = 0.0f, male = 3.6224f)
      ActivityLevel.High -> sex(female = 0.0f, male = 4.3904f)
      ActivityLevel.VeryHigh -> sex(female = 1.664f, male = 5.4144f)
    }
    ((0.47027 / imp50 - 0.24196 / imp5) * height * height + 0.13796 * weight - 0.1152 * age + 5.12 + activityCorrFac) / weight * 100.0f
  }

  val metabolicRate = run {
    val activityFactor = when (activityLevel) {
      ActivityLevel.VeryLow -> 1.2
      ActivityLevel.Low -> 1.3
      ActivityLevel.Normal -> 1.5
      ActivityLevel.High -> 1.75
      ActivityLevel.VeryHigh -> 2.0
    }
    (muscleMassPercent * 0.4135 * weight + 369.0) * activityFactor
  }

  return BIAResults(
    bodyFatPercent = bodyFatPercent.toFloat(),
    bodyWaterPercent = bodyWaterPercent.toFloat(),
    muscleMassPercent = muscleMassPercent.toFloat(),
    bodyMassIndex = bodyMassIndex.toFloat(),
    metabolicRate = metabolicRate.toFloat()
  )
}