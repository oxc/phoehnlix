package de.esotechnik.phoehnlix.apiservice

import de.esotechnik.phoehnlix.apiservice.model.Profile
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import java.time.format.DateTimeFormatter
import de.esotechnik.phoehnlix.data.Measurement as DbMeasurement
import de.esotechnik.phoehnlix.data.Profile as DbProfile

/**
 * @author Bernhard Frauendienst
 */

val BIRTHDAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
val TIMESTAMP_FORMATTER: DateTimeFormatter = DateTimeFormatter.ISO_INSTANT

fun DbProfile.toProfile() = Profile(
    id = id.value,
    name = name,
    sex = sex,
    birthday = BIRTHDAY_FORMATTER.format(birthday),
    height = height,
    activityLevel = activityLevel,
    targetWeight = targetWeight
)

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