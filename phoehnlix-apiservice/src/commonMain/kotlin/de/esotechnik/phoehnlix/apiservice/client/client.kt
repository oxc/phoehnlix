package de.esotechnik.phoehnlix.apiservice.client

import de.esotechnik.phoehnlix.apiservice.model.Profile
import de.esotechnik.phoehnlix.apiservice.model.ProfileMeasurement
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.ParametersBuilder

private typealias ProfileId = Int

/**
 * @author Bernhard Frauendienst
 */
class ApiClient(
  val http: HttpClient,
  val apiBaseUrl: String
) {

  private suspend inline fun <reified T> get(
    vararg components: String,
    params: ParametersBuilder.() -> Unit = {}
  ) : T = http.get(apiBaseUrl) {
    url.path("api", *components)
    url.parameters.apply(params)
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