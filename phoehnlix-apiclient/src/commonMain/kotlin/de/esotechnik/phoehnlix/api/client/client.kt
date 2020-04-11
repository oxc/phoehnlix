package de.esotechnik.phoehnlix.api.client

import de.esotechnik.phoehnlix.api.model.MeasurementData
import de.esotechnik.phoehnlix.api.model.OAuth2Token
import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.api.model.ProfileMeasurement
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.features.defaultRequest
import io.ktor.client.features.json.Json
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.ParametersBuilder
import io.ktor.http.URLBuilder
import io.ktor.http.contentType
import io.ktor.http.takeFrom

private typealias ProfileId = Int

/**
 * @author Bernhard Frauendienst
 */
class ApiClient private constructor(
  private val http: HttpClient,
  private val apiHost: String
) {
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
        apiToken?.let {
          header("Authorization", "Bearer $it")
        }
      }
      block()
    }, apiHost)
  }

  private fun URLBuilder.appendPath(vararg components: String) {
    takeFrom(apiHost)
    val prefix = this.encodedPath.takeUnless { it == "/" } ?: ""
    path(*components)
    encodedPath = prefix + encodedPath // both already have prefix="/"
  }

  private suspend inline fun <reified T> get(
    vararg components: String,
    params: ParametersBuilder.() -> Unit = {}
  ): T = http.get {
    url.appendPath(*components)
    url.parameters.apply(params)
  }

  private suspend inline fun <reified T> post(
    vararg  components: String,
    body: Any? = null,
    params: ParametersBuilder.() -> Unit = {}
  ): T = http.post {
    url.appendPath(*components)
    url.parameters.apply(params)
    body?.let {
      contentType(ContentType.Application.Json)
      this.body = it
    }
  }

  val profile = ProfilesClient()

  inner class ProfilesClient {
    suspend operator fun invoke(): List<Profile> = get("profile")
    suspend operator fun invoke(id: ProfileId): Profile = get("profile", id.toString())
    operator fun get(id: ProfileId) = ProfileClient(id)
  }

  inner class ProfileClient(private val id: ProfileId) {
    suspend operator fun invoke(): Profile = profile(id)

    val measurements = ProfileMeasurementsClient(id)
  }

  inner class ProfileMeasurementsClient(private val id: ProfileId) {
    suspend operator fun invoke(
      from: String? = null,
      to: String? = null
    ): List<ProfileMeasurement> = get("profile", id.toString(), "measurements") {
      from?.let { append("from", it) }
      to?.let { append("to", it) }
    }
  }

  val login = LoginClient()

  inner class LoginClient {
    suspend fun google(oauthToken: OAuth2Token): PhoehnlixApiToken = post("login", "google", body = oauthToken)
  }

  val measurement = MeasurementClient()

  inner class MeasurementClient {
    suspend fun post(measurement: MeasurementData): Unit = post("measurement", body = measurement)
  }
}