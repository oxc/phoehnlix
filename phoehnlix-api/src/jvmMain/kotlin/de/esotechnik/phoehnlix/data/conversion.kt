package de.esotechnik.phoehnlix.data

import de.esotechnik.phoehnlix.model.ProfileData
import java.time.Duration
import java.time.Instant

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */
fun Profile.toProfileData(timestamp: Instant) = ProfileData(
  height = height,
  age = Duration.between(birthday, timestamp).toDays() / 365f,
  sex = sex,
  activityLevel = activityLevel
)