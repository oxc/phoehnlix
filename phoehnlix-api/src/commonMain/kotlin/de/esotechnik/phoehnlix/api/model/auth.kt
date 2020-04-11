package de.esotechnik.phoehnlix.api.model

import kotlinx.serialization.Serializable

/**
 * @author Bernhard Frauendienst
 */
@Serializable
class OAuth2Token(
  val accessToken: String,
  val tokenType: String,
  val expiresIn: Long,
  val refreshToken: String?
)

@Serializable
class PhoehnlixApiToken(

)