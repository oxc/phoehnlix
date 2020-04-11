package de.esotechnik.phoehnlix.apiservice

import de.esotechnik.phoehnlix.apiservice.data.setupDatabase
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.json
import io.ktor.util.KtorExperimentalAPI
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@KtorExperimentalAPI
@JvmOverloads
fun Application.module(testing: Boolean = false) {
  setupDatabase()

  install(CORS) {
    method(HttpMethod.Options)
    method(HttpMethod.Get)
    method(HttpMethod.Post)
    method(HttpMethod.Put)
    method(HttpMethod.Delete)
    method(HttpMethod.Patch)
    header(HttpHeaders.Authorization)
    allowCredentials = true
    anyHost()
  }
  install(ContentNegotiation) {
    json(
      json = Json(
        DefaultJsonConfiguration.copy(
          prettyPrint = true
        )
      )
    )
  }

  routing {
    route("/api") {
      apiservice()
    }
  }
}
