package de.esotechnik.phoehnlix.data

import de.esotechnik.phoehnlix.model.BIAData
import de.esotechnik.phoehnlix.model.ProfileData
import de.esotechnik.phoehnlix.util.roundToDigits
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * @author Bernhard Frauendienst
 */
fun Profile.toProfileData(timestamp: Instant) = ProfileData(
  height = height,
  age = age(timestamp),
  sex = sex,
  activityLevel = activityLevel
)

fun Profile.age(at: Instant) = (
  Duration.between(
    birthday.atStartOfDay().toInstant(ZoneId.systemDefault().rules.getOffset(at)),
    at).toDays() / 365.0
  // we don't know the hour of birth anyway, so don't pretend we have high precision
  ).roundToDigits(3)

fun Measurement.toBiaData() = imp50?.let { imp50 -> imp5?.let { imp5 ->
  BIAData(imp50 = imp50, imp5 = imp5) }
}

fun Measurement.toProfileData(fallbackProfile: Profile)
  = toProfileData(fallbackProfile.toProfileData(timestamp))
fun Measurement.toProfileData(fallbackProfile: ProfileData) = ProfileData(
  height = height ?: fallbackProfile.height,
  age = age ?: fallbackProfile.age,
  sex = sex ?: fallbackProfile.sex,
  activityLevel = activityLevel ?: fallbackProfile.activityLevel
)