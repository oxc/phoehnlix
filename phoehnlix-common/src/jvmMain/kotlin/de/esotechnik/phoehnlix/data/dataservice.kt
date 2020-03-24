package de.esotechnik.phoehnlix.data

import de.esotechnik.phoehnlix.model.BIAResults
import de.esotechnik.phoehnlix.model.MeasurementData
import de.esotechnik.phoehnlix.model.calculateBIAResults
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger("de.esotechnik.phoehnlix.data.dataervice")
/**
 * @author Bernhard Frauendienst
 */

suspend fun insertNewMeasurement(data: MeasurementData) {
  val senderScale = newSuspendedTransaction(Dispatchers.IO) {
    val bytes = data.senderId.bytes.toByteArray()
    val existing = Scale.find { Scales.serial eq bytes }.singleOrNull()
    existing ?: Scale.new {
      serial = bytes
    }
  }

  try {
    newSuspendedTransaction(Dispatchers.IO) {
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
  } catch (e: ExposedSQLException) {
    if (e.message?.contains("duplicate key") == true) {
      // ignore
      log.warn("Ignoring duplicate entry: ${e.message}")
    } else throw e
  }
}

fun Measurement.setBIAResults(biaResults: BIAResults?) {
  bodyFatPercent = biaResults?.bodyFatPercent
  bodyWaterPercent = biaResults?.bodyWaterPercent
  muscleMassPercent = biaResults?.muscleMassPercent
  bodyMassIndex = biaResults?.bodyMassIndex
  metabolicRate = biaResults?.metabolicRate
}

fun Measurement.setProfile(selectedProfile: Profile?) {
  profile = selectedProfile
  sex = selectedProfile?.sex
  age = selectedProfile?.age(timestamp)
  height = selectedProfile?.height
  activityLevel = selectedProfile?.activityLevel
}
