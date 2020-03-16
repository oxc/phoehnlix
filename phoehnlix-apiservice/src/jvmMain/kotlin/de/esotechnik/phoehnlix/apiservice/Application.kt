package de.esotechnik.phoehnlix.apiservice

import de.esotechnik.phoehnlix.data.Measurement
import de.esotechnik.phoehnlix.data.Measurements
import de.esotechnik.phoehnlix.data.setupDatabase
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.NotFoundException
import io.ktor.http.ContentType
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.optionalParam
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.serialization
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import de.esotechnik.phoehnlix.data.Profile as DbProfile

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@KtorExperimentalAPI
@JvmOverloads
fun Application.module(testing: Boolean = false) {
  setupDatabase()

  install(ContentNegotiation) {
    serialization(
      contentType = ContentType.Application.Json,
      json = Json(
        DefaultJsonConfiguration.copy(
          prettyPrint = true
        )
      )
    )
  }

  routing {
    route("/api") {
      route("/profile") {
        get {
          // TOOD: list all profiles
        }
        route("/{profile}") {
          get {
            val profileId = call.parameters.getOrFail<Int>("profile")
            val profile = db {
              DbProfile.findById(profileId)?.toProfile()
            } ?: throw NotFoundException()
            call.respond(profile)
          }
          get("/measurements") {
            val profileId = call.parameters.getOrFail<Int>("profile")
            val measurements = db {
              Measurement.find { Measurements.profile eq profileId }
            }.map { it.toMeasurement() }
            call.respond(measurements)
          }
        }
      }
    }
  }
}

suspend fun <T> db(statement: suspend Transaction.() -> T) =
  newSuspendedTransaction(Dispatchers.IO) {
    statement()
  }