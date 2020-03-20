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
import kotlinx.html.body
import kotlinx.html.canvas
import kotlinx.html.h1
import kotlinx.html.id
import kotlinx.html.script

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@KtorExperimentalAPI
@JvmOverloads
fun Application.module(testing: Boolean = false) {
  routing {
    get("/") {
      call.respondHtml {
        body {
          h1 {
            +"Hallo Weight"
          }
          canvas {
            id = "graph-canvas"
          }
          script(src="http://localhost:8080/phoehnlix-frontend.js") {}
        }
      }
    }
  }
}