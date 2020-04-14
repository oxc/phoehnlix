package de.esotechnik.phoehnlix.apiservice.auth

import de.esotechnik.phoehnlix.api.google.SCOPE_FITNESS_BODY_WRITE
import de.esotechnik.phoehnlix.api.google.SCOPE_USERINFO_PROFILE
import de.esotechnik.phoehnlix.api.google.SCOPE_USER_BIRTHDAY_READ
import de.esotechnik.phoehnlix.apiservice.util.InstantAsStringTimestampSerializer
import de.esotechnik.phoehnlix.apiservice.util.ParameterOverridingContext
import de.esotechnik.phoehnlix.apiservice.util.SpaceSeparatedSerializer
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.auth.OAuth2RequestParameters
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings
import io.ktor.auth.oauthHandleCallback
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.http.parametersOf
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.pipeline.PipelineContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import java.time.Instant

/**
 * @author Bernhard Frauendienst
 */
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

@OptIn(KtorExperimentalAPI::class)
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
    authorizeUrlInterceptor = {
      parameters.append("access_type","offline")
    }
  )
}

@OptIn(KtorExperimentalAPI::class)
suspend fun PipelineContext<Unit, ApplicationCall>.oauth2RequestAccessToken(
  code: String,
  client: HttpClient,
  dispatcher: CoroutineDispatcher,
  provider: OAuthServerSettings.OAuth2ServerSettings,
  callbackUrl: String,
  configure: HttpRequestBuilder.() -> Unit = {},
  block: suspend (OAuthAccessTokenResponse.OAuth2) -> Unit
) {
  val pcontext = ParameterOverridingContext(this, parametersOf(
    OAuth2RequestParameters.State to listOf(""),
    OAuth2RequestParameters.Code to listOf(code)
  ))

  // we hacky-hackily re-use oauthHandleCallback here.
  // This will most likely break. Just implement it ourselves.
  pcontext.oauthHandleCallback(client, dispatcher, provider, callbackUrl, "", configure) {
    block(it as OAuthAccessTokenResponse.OAuth2)
  }
}

