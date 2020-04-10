package de.esotechnik.phoehnlix.apiservice.model

import kotlinx.serialization.Serializable

/**
 * @author Bernhard Frauendienst
 */
@Serializable
class ApiTokenResponse(
  val accessToken: String,
  val tokenType: String,
  val expiresIn: Long,
  val refreshToken: String?
)