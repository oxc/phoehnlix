package de.esotechnik.phoehnlix.api.model

import kotlinx.serialization.Serializable

/**
 * @author Bernhard Frauendienst
 */
enum class MeasureType {
  Weight,
  BodyFatPercent, BodyWaterPercent, MuscleMassPercent,
  BodyMassIndex,
  MetabolicRate
}

@Serializable
data class MeasurementData(
  val senderId: SenderId,
  val timestamp: String,
  val weight: Double,
  val imp50: Double?,
  val imp5: Double?
)

@Serializable
data class SenderId(val bytes: List<Byte>) {
  init { require(bytes.size == 12) }
}

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
)

operator fun ProfileMeasurement.get(measureType: MeasureType) = when (measureType) {
  MeasureType.Weight -> weight
  MeasureType.BodyFatPercent -> bodyFatPercent
  MeasureType.BodyWaterPercent -> bodyWaterPercent
  MeasureType.MuscleMassPercent -> muscleMassPercent
  MeasureType.BodyMassIndex -> bodyMassIndex
  MeasureType.MetabolicRate -> metabolicRate
}