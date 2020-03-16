package de.esotechnik.phoehnlix.model

/**
 * @author Bernhard Frauendienst
 */
enum class Sex {
  Male, Female
}

enum class ActivityLevel {
  VeryLow, Low, Normal, High, VeryHigh
}

data class ProfileData(
  val height: Int,
  val age: Float,
  val sex: Sex,
  val activityLevel: ActivityLevel
)


