package de.esotechnik.phoehnlix.apiservice.data

import de.esotechnik.phoehnlix.api.model.MeasurementData
import de.esotechnik.phoehnlix.apiservice.BIAResults
import de.esotechnik.phoehnlix.apiservice.calculateBIAResults
import de.esotechnik.phoehnlix.apiservice.util.toInstant
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.math.abs

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
      val dataTimestamp = data.timestamp.toInstant()

      val selectedProfile = senderScale.connectedProfiles.findMatchingProfile(data.weight, dataTimestamp)

      val biaResults = selectedProfile?.let { data.calculateBIAResults(it.toProfileData(dataTimestamp)) }

      Measurement.new {
        scale = senderScale

        timestamp = dataTimestamp
        weight = data.weight
        imp50 = data.imp50
        imp5 = data.imp5

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

private fun SizedIterable<Profile>.findMatchingProfile(
  weight: Double,
  dataTimestamp: Instant
): Profile? {
  val profiles = this.filter { profile ->
    val lastMeasurements = Measurements
      .slice(Measurements.weight)
      .select(
  (
          Measurements.profile eq profile.id
          ) and (
          Measurements.timestamp lessEq dataTimestamp
          )
      )
      .orderBy(Measurements.timestamp, SortOrder.DESC)
      .limit(5)
      .map { it[Measurements.weight] }
    if (lastMeasurements.isEmpty()) {
      false
    } else {
      val avgWeight = lastMeasurements.average()
      val delta = abs(avgWeight - weight)
      log.debug("Profile ${profile.id} had an 5-average weight of $avgWeight (distance: $delta)")

      delta < 5
    }
  }
  when (profiles.size) {
    1 -> return profiles[0]
    0 -> log.info("No matching profiles for weight $weight")
    else -> log.info("Multiple profiles matching weight $weight: ${profiles.joinToString(", ") { it.id.toString() }}")
  }
  return null;
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
