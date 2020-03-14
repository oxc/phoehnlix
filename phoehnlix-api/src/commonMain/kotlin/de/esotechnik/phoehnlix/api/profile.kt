package de.esotechnik.phoehnlix.api

/**
 * @author Bernhard Frauendienst
 */
enum class Sex {
  Male, Female
}

data class ProfileData(
  val height: Float,
  val age: Int,
  val sex: Sex
)


