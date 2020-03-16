package de.esotechnik.phoehnlix.data

import de.esotechnik.phoehnlix.model.BIAResults
import de.esotechnik.phoehnlix.model.MeasurementData
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * @author Bernhard Frauendienst
 */

fun insertNewMeasurement(data: MeasurementData): Measurement {
  val senderScale = transaction {
    val bytes = data.senderId.bytes.toByteArray()
    val existing = Scale.find { Scales.serial eq bytes }.singleOrNull()
    existing ?: Scale.new {
      serial = bytes
    }
  }

  return transaction {
    // TODO: find correct profile out of multiple, find matching one even if only one profile
    val selectedProfile = senderScale.connectedProfiles.singleOrNull()

    val biaResults = selectedProfile?.let { data.calculateBIAResults(it.toProfileData(data.timestamp)) }

    Measurement.new {
      scale = senderScale

      timestamp = data.timestamp
      weight = data.weight
      imp50 = data.biaData?.imp50
      imp5 = data.biaData?.imp5

      setBIAResults(biaResults)
      setProfile(selectedProfile)
    }
  }
}

private fun Measurement.setBIAResults(biaResults: BIAResults?) {
  bodyFatPercent = biaResults?.bodyFatPercent
  bodyWaterPercent = biaResults?.bodyWaterPercent
  muscleMassPercent = biaResults?.muscleMassPercent
  bodyMassIndex = biaResults?.bodyMassIndex
  metabolicRate = biaResults?.metabolicRate
}

private fun Measurement.setProfile(selectedProfile: Profile?) {
  profile = selectedProfile
  sex = selectedProfile?.sex
  age = selectedProfile?.age(timestamp)
  height = selectedProfile?.height
  activityLevel = selectedProfile?.activityLevel
}
