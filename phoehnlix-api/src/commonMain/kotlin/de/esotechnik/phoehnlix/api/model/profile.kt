package de.esotechnik.phoehnlix.api.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.enumFromOrdinal

/**
 * @author Bernhard Frauendienst
 */
enum class Sex {
  Male, Female
}

enum class ActivityLevel(val index: Int) {
  VeryLow(1), Low(2), Normal(3), High(4), VeryHigh(5);

  companion object {
    fun fromIndex(index: Int) = when (index) {
      1 -> VeryLow
      2 -> Low
      3 -> Normal
      4 -> High
      5 -> VeryHigh
      else -> throw IllegalArgumentException("No such activity level: $index")
    }
  }
}

@Serializable
class Profile(
  val id: Int,
  val name: String,
  val sex: Sex,
  val birthday: String,
  val height: Int,
  val activityLevel: ActivityLevel,
  val targetWeight: Double? = null
)

@Serializable
data class ProfileDraft(
  var name: String? = null,
  var sex: Sex? = null,
  var birthday: String? = null,
  var height: Int? = null,
  var activityLevel: ActivityLevel? = null,
  var targetWeight: Double? = null
)

fun Profile.toProfileDraft() = ProfileDraft(
  name = name,
  sex = sex,
  birthday = birthday,
  height = height,
  activityLevel = activityLevel,
  targetWeight = targetWeight
)
