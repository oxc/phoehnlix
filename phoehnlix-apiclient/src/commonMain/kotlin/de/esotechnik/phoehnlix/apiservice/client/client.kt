package de.esotechnik.phoehnlix.apiservice.client

import de.esotechnik.phoehnlix.apiservice.model.ApiTokenResponse
import de.esotechnik.phoehnlix.apiservice.model.Profile
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.host
import io.ktor.client.request.post
import io.ktor.client.utils.EmptyContent
import io.ktor.http.ParametersBuilder
import kotlinx.serialization.json.JsonElement

private typealias ProfileId = Int

/**
 * @author Bernhard Frauendienst
 */
class ApiClient private constructor(private val http: HttpClient) {
  companion object {
    operator fun <T : HttpClientEngineConfig> invoke(
      engineFactory: HttpClientEngineFactory<T>,
      apiHost: String,
      apiToken: String?,
      block: HttpClientConfig<T>.() -> Unit = {}
    ) = ApiClient(HttpClient(engineFactory) {
      Json {
        serializer = KotlinxSerializer()
      }
      defaultRequest {
        host = apiHost
        apiToken?.let {
          header("Authorization", "Bearer $it")
        }
      }
      block()
    })
  }

  private suspend inline fun <reified T> get(
    vararg components: String,
    params: ParametersBuilder.() -> Unit = {}
  ): T = http.get {
    url.path("api", *components)
    url.parameters.apply(params)
  }

  private suspend inline fun <reified T, reified B> post(
    vararg  components: String,
    body: B,
    params: ParametersBuilder.() -> Unit = {}
  ): T = http.post {
    url.path("api", *components)
    url.parameters.apply(params)
    this.body = body ?: EmptyContent
  }

  inner class LoginClient {
    suspend fun google(oauthToken: ): ApiTokenResponse = post("login", "google", body = googleOAuthResponse)
  }


  val profile = ProfilesClient()

  inner class ProfilesClient {
    suspend operator fun invoke(): List<Profile> = get("profile")
    suspend operator fun invoke(id: ProfileId): Profile = get("profile", id.toString())
    operator fun get(id: ProfileId) = ProfileClient(id)
  }

  inner class ProfileClient(private val id: ProfileId) {
    suspend operator fun invoke(): Profile = profile(id)

    val measurements = MeasurementsClient(id)
  }

  inner class MeasurementsClient(private val id: ProfileId) {
    suspend operator fun invoke(
      from: String? = null,
      to: String? = null
    ): List<ProfileMeasurement> = get("profile", id.toString(), "measurements") {
      from?.let { append("from", it) }
      to?.let { append("to", it) }
    }
  }
}