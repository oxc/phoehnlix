package de.esotechnik.phoehnlix.data

import de.esotechnik.phoehnlix.model.ProfileData
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */
fun Profile.toProfileData(timestamp: Instant) = ProfileData(
  height = height,
  age = age(timestamp),
  sex = sex,
  activityLevel = activityLevel
)

fun Profile.age(at: Instant) = Duration.between(
  birthday.atStartOfDay().toInstant(ZoneId.systemDefault().rules.getOffset(at)),
  at
).toDays() / 365f