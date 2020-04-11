package de.esotechnik.phoehnlix.api.model

import kotlinx.serialization.Serializable

/**
 * @author Bernhard Frauendienst
 */
enum class Sex {
  Male, Female
}

enum class ActivityLevel {
  VeryLow, Low, Normal, High, VeryHigh
}

@Serializable
class Profile(
  val id: Int,
  val name: String,
  val sex: Sex,
  val birthday: String,
  val height: Int,
  val activityLevel: ActivityLevel,
  val targetWeight: Double
)