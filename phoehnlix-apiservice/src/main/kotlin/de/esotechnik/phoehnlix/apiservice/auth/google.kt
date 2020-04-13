package de.esotechnik.phoehnlix.apiservice.auth

import de.esotechnik.phoehnlix.apiservice.util.InstantAsStringTimestampSerializer
import de.esotechnik.phoehnlix.apiservice.util.SpaceSeparatedSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.time.Instant

/**
 * @author Bernhard Frauendienst
 */
val SCOPE_USER_BIRTHDAY_READ = "https://www.googleapis.com/auth/user.birthday.read"
val SCOPE_USERINFO_PROFILE = "https://www.googleapis.com/auth/userinfo.profile"
val SCOPE_FITNESS_BODY_WRITE = "https://www.googleapis.com/auth/fitness.body.write"

val Json = Json(JsonConfiguration.Stable.copy(ignoreUnknownKeys = true))

@Serializable
class TokenInfo(
  @SerialName("aud")
  val audience: String,
  @SerialName("sub")
  val subject: String,
  @SerialName("scope")
  @Serializable(SpaceSeparatedSerializer::class)
  val scopes: List<String>,
  @SerialName("exp")
  @Serializable(InstantAsStringTimestampSerializer::class)
  val expires: Instant,
  @SerialName("access_type")
  val accessType: String
)