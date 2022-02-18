package de.esotechnik.phoehnlix.apiservice.auth

import de.esotechnik.phoehnlix.api.google.SCOPE_FITNESS_BODY_WRITE
import de.esotechnik.phoehnlix.api.google.SCOPE_USERINFO_PROFILE
import de.esotechnik.phoehnlix.api.google.SCOPE_USER_BIRTHDAY_READ
import de.esotechnik.phoehnlix.apiservice.util.InstantAsStringTimestampSerializer
import de.esotechnik.phoehnlix.apiservice.util.SpaceSeparatedSerializer
import io.ktor.server.application.*
import io.ktor.server.auth.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.Instant

/**
 * @author Bernhard Frauendienst
 */
val GoogleJson = Json {
  ignoreUnknownKeys = true
}

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

@Serializable
class Userinfo(
  @SerialName("sub")
  val subject: String,
  @SerialName("name")
  val name: String,
  @SerialName("given_name")
  val givenName: String,
  @SerialName("family_name")
  val familyName: String,
  @SerialName("picture")
  val pictureUrl: String,
  @SerialName("locale")
  val locale: String
)

fun TokenInfo.hasScope(scope: String) = scope in scopes
fun TokenInfo.hasFitWriteScope() = hasScope(SCOPE_FITNESS_BODY_WRITE)

internal fun Application.createGoogleOAuth2Provider(): OAuthServerSettings.OAuth2ServerSettings {
  val config = environment.config.config("phoehnlix.googleOAuth")

  return OAuthServerSettings.OAuth2ServerSettings(
    name = "google",
    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
    accessTokenUrl = "https://oauth2.googleapis.com/token",
    requestMethod = io.ktor.http.HttpMethod.Post,

    clientId = config.property("clientId").getString(),
    clientSecret = config.property("clientSecret").getString(),
    defaultScopes = listOf(
      SCOPE_USERINFO_PROFILE,
      SCOPE_USER_BIRTHDAY_READ,
      SCOPE_FITNESS_BODY_WRITE
    ),
    extraTokenParameters = listOf("access_type" to "offline")
  )
}
