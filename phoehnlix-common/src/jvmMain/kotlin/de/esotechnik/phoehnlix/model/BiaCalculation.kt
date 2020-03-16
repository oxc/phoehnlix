package de.esotechnik.phoehnlix.model

import de.esotechnik.phoehnlix.model.ActivityLevel.*
import de.esotechnik.phoehnlix.util.roundToDigits

private operator fun <T> Sex.invoke(female: T, male: T): T = when (this) {
  Sex.Male -> male
  Sex.Female -> female
}

fun calculateBIAResults(biaData: BIAData, weight: Double, profile: ProfileData): BIAResults {
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
    val activityFactor = when (activityLevel) {
      VeryLow, Low, Normal -> 0.0
      High -> sex(female = 2.3, male = 2.5)
      VeryHigh -> sex(female = 4.1, male = 4.3)
    }
    val sexFactor = sex(female = 0.214, male = 0.251)
    val activitySexDiv = sex(female = 55.1, male = 65.5)
    1.847 * bodyMassIndex + sexFactor * age + 0.062 * imp50 - (activitySexDiv - activityFactor)
  }

  val bodyWaterPercent = run {
    val activityFactor = when (activityLevel) {
      VeryLow, Low, Normal -> sex(female = 0.0, male = 2.83)
      High -> sex(female = 0.4, male = 3.93)
      VeryHigh -> sex(female = 1.4, male = 5.33)
    }
    (0.3674 * height * height / imp50 + 0.17531 * weight - 0.11 * age + (6.53 + activityFactor)) / weight * 100.0
  }

  val muscleMassPercent = run {
    val activityFactor = when (activityLevel) {
      VeryLow, Low, Normal -> sex(female = 0.0f, male = 3.6224f)
      High -> sex(female = 0.0f, male = 4.3904f)
      VeryHigh -> sex(female = 1.664f, male = 5.4144f)
    }
    ((0.47027 / imp50 - 0.24196 / imp5) * height * height + 0.13796 * weight - 0.1152 * age + 5.12 + activityFactor) / weight * 100.0f
  }

  val metabolicRate = run {
    val activityFactor = when (activityLevel) {
      VeryLow -> 1.2
      Low -> 1.3
      Normal -> 1.5
      High -> 1.75
      VeryHigh -> 2.0
    }
    (muscleMassPercent * 0.4135 * weight + 369.0) * activityFactor
  }

  return BIAResults(
    bodyFatPercent = bodyFatPercent.roundToDigits(1),
    bodyWaterPercent = bodyWaterPercent.roundToDigits(1),
    muscleMassPercent = muscleMassPercent.roundToDigits(1),
    bodyMassIndex = bodyMassIndex.roundToDigits(1),
    metabolicRate = metabolicRate.roundToDigits(1)
  )
}
