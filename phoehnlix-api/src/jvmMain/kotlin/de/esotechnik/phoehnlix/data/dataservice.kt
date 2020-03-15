package de.esotechnik.phoehnlix.data

import de.esotechnik.phoehnlix.model.MeasurementData
import de.esotechnik.phoehnlix.model.calculateBIAResults
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * @author Bernhard Frauendienst
 */

fun insertNewMeasurement(data: MeasurementData) {
  val senderScale = transaction {
    val bytes = data.senderId.bytes.toByteArray()
    val existing = Scale.find { Scales.serial eq bytes }.singleOrNull()
    existing ?: Scale.new {
      serial = bytes
    }
  }

  // TODO: insert measurement if it can directly be associated to a profile?
  val measurement = transaction {
    Measurement.new {
      scale = senderScale

      timestamp = data.timestamp
      weight = data.weight
      resistance = data.biaData?.imp50
      reactance = data.biaData?.imp5
    }
  }

  // TODO: find correct profile out of multiple, check weight even if only one profile
  val selectedProfile = senderScale.connectedProfiles.singleOrNull() ?: return

  val biaResults = data.calculateBIAResults(selectedProfile.toProfileData(data.timestamp))
}