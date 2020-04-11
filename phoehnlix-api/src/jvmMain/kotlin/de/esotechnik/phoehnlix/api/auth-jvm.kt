package de.esotechnik.phoehnlix.api

import de.esotechnik.phoehnlix.api.model.OAuth2Token
import io.ktor.auth.OAuthAccessTokenResponse

/**
 * @author Bernhard Frauendienst
 */

operator fun OAuth2Token.Companion.invoke(oauthToken: OAuthAccessTokenResponse.OAuth2) = OAuth2Token(
  accessToken = oauthToken.accessToken,
  tokenType = oauthToken.tokenType,
  expiresIn = oauthToken.expiresIn,
  refreshToken = oauthToken.refreshToken
)