package de.esotechnik.phoehnlix.api.client

import de.esotechnik.phoehnlix.api.google.LoginRequest
import de.esotechnik.phoehnlix.api.model.LoginResponse
import de.esotechnik.phoehnlix.api.model.MeasurementData
import de.esotechnik.phoehnlix.api.model.PhoehnlixApiToken
import de.esotechnik.phoehnlix.api.model.Profile
import de.esotechnik.phoehnlix.api.model.ProfileDraft
import de.esotechnik.phoehnlix.api.model.ProfileMeasurement
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.*
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.kotlinx.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.core.Closeable
import kotlinx.serialization.json.Json

sealed class ProfileId(private val id: String) {
  override fun toString() = id

  object Me : ProfileId("me")
  class Id(id: Int) : ProfileId(id.toString())
}

/**
 * @author Bernhard Frauendienst
 */
class ApiClient private constructor(
  private val http: HttpClient,
  private val apiUrl: String
) : Closeable by http {
  companion object {
    operator fun <T : HttpClientEngineConfig> invoke(
      engineFactory: HttpClientEngineFactory<T>,
      apiUrl: String,
      apiToken: PhoehnlixApiToken?,
      block: HttpClientConfig<T>.() -> Unit = {}
    ) = ApiClient(HttpClient(engineFactory) {
      install(ContentNegotiation) {
        json(Json {
          prettyPrint = true
        })
      }
      defaultRequest {
        apiToken?.let {
          header("Authorization", "Bearer ${it.accessToken}")
        }
      }
      block()
    }, apiUrl)
  }

  private fun URLBuilder.appendPath(vararg components: String) {
    takeFrom(apiUrl)
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
  }.body()

  private suspend inline fun <reified T> post(
    vararg  components: String,
    body: Any? = null,
    params: ParametersBuilder.() -> Unit = {}
  ): T = http.post {
    url.appendPath(*components)
    url.parameters.apply(params)
    body?.let {
      contentType(ContentType.Application.Json)
      setBody(it)
    }
  }.body()

  val profile = ProfilesClient()

  inner class ProfilesClient {
    suspend operator fun invoke(): List<Profile> = get("profile")
    suspend operator fun invoke(id: ProfileId): Profile = get("profile", id.toString())
    suspend operator fun invoke(id: Int): Profile = invoke(ProfileId.Id(id))
    operator fun get(id: ProfileId) = ProfileClient(id)
    operator fun get(id: Int) = ProfileClient(ProfileId.Id(id))
  }

  inner class ProfileClient(private val id: ProfileId) {
    suspend operator fun invoke(): Profile = profile(id)
    suspend fun update(profileUpdate: ProfileDraft): Profile = post("profile", id.toString(), body = profileUpdate)

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
    suspend fun google(code: String): LoginResponse = post("login", "google") {
      append("code", code)
      append("state", "")
    }
  }

  val measurement = MeasurementClient()

  inner class MeasurementClient {
    suspend fun post(measurement: MeasurementData): Unit = post("measurement", body = measurement)
  }
}