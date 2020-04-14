package de.esotechnik.phoehnlix.api.google

import kotlinx.serialization.Serializable

/**
 * @author Bernhard Frauendienst <bernhard.frauendienst@markt.de>
 */

val SCOPE_USER_BIRTHDAY_READ = "https://www.googleapis.com/auth/user.birthday.read"
val SCOPE_USERINFO_PROFILE = "https://www.googleapis.com/auth/userinfo.profile"
val SCOPE_FITNESS_BODY_WRITE = "https://www.googleapis.com/auth/fitness.body.write"

@Serializable
class LoginRequest(
  val code: String
)