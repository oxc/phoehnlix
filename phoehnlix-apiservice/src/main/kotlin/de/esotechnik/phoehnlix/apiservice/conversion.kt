package de.esotechnik.phoehnlix.apiservice

import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.api.model.ProfileMeasurement
import java.time.format.DateTimeFormatter
import de.esotechnik.phoehnlix.apiservice.data.Measurement as DbMeasurement
import de.esotechnik.phoehnlix.apiservice.data.Profile as DbProfile

/**
 * @author Bernhard Frauendienst
 */

val BIRTHDAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
val TIMESTAMP_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

fun DbProfile.toProfile() = Profile(
    id = id.value,
    name = name,
    imageUrl = imageUrl,
    sex = sex,
    birthday = BIRTHDAY_FORMATTER.format(birthday),
    height = height,
    activityLevel = activityLevel,
    targetWeight = targetWeight,
)
val ProfileResponse = { it: DbProfile -> it.toProfile() }

fun DbMeasurement.toMeasurement() = ProfileMeasurement(
  id = id.value,
  timestamp = TIMESTAMP_FORMATTER.format(timestamp),
  weight = weight,
  bodyFatPercent = bodyFatPercent,
  bodyWaterPercent = bodyWaterPercent,
  muscleMassPercent = muscleMassPercent,
  bodyMassIndex = bodyMassIndex,
  metabolicRate = metabolicRate
)
val MeasurementResponse = { it: DbMeasurement -> it.toMeasurement() }