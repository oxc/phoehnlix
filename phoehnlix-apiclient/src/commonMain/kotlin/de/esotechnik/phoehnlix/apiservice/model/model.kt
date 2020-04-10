package de.esotechnik.phoehnlix.apiservice.model

import de.esotechnik.phoehnlix.model.ActivityLevel
import de.esotechnik.phoehnlix.model.MeasureType
import de.esotechnik.phoehnlix.model.Sex
import de.esotechnik.phoehnlix.util.formatDecimalDigits
import kotlinx.serialization.Serializable

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

@Serializable
class ProfileMeasurement(
  val id: Long,

  val timestamp: String,
  val weight: Double,

  val bodyFatPercent: Double?,
  val bodyWaterPercent: Double?,
  val muscleMassPercent: Double?,
  val bodyMassIndex: Double?,
  val metabolicRate: Double?
}

operator fun ProfileMeasurement.get(measureType: MeasureType) = when (measureType) {
  MeasureType.Weight -> weight
  MeasureType.BodyFatPercent -> bodyFatPercent
  MeasureType.BodyWaterPercent -> bodyWaterPercent
  MeasureType.MuscleMassPercent -> muscleMassPercent
  MeasureType.BodyMassIndex -> bodyMassIndex
  MeasureType.MetabolicRate -> metabolicRate
}