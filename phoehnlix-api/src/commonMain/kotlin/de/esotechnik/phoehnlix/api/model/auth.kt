package de.esotechnik.phoehnlix.api.model

import kotlinx.serialization.Serializable

/**
 * @author Bernhard Frauendienst
 */
@Serializable
class PhoehnlixApiToken(
  val accessToken: String
)