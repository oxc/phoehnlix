package de.esotechnik.phoehnlix.api.model

import kotlinx.serialization.Serializable

/**
 * @author Bernhard Frauendienst
 */
@Serializable
class PhoehnlixApiToken(
  val accessToken: String
)

@Serializable
class LoginResponse(
  val apiToken: PhoehnlixApiToken,
  val profile: Profile? = null,
  val profileDraft: ProfileDraft? = null
)

@Serializable
class ShibbolethLoginRequest(
  val profileId: Int
)