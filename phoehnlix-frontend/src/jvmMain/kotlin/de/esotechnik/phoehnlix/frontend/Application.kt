package de.esotechnik.phoehnlix.frontend

import io.ktor.application.*
import io.ktor.features.BadRequestException
import io.ktor.features.ContentNegotiation
import io.ktor.html.respondHtml
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.*
import io.ktor.sessions.defaultSessionSerializer
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.getOrFail

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@KtorExperimentalAPI
@JvmOverloads
fun Application.module(testing: Boolean = false) {
  routing {
    get("/") {
      call.respondHtml {

      }
    }
  }
}