package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.ktor.setupForwardedFor
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.html.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
import org.slf4j.event.Level
import kotlin.collections.set

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {
  setupForwardedFor()
  install(CallLogging)

  val apiBaseUrl = environment.config.property("phoehnlix.apiUrl").getString()
  val googleClientId = environment.config.property("phoehnlix.googleOAuth.clientId").getString()
  routing {
    static("/static") {
      staticBasePackage = "de.esotechnik.phoehnlix.frontend.static"
      resource("/js/phoehnlix-frontend.js")
      resource("/js/phoehnlix-frontend.js.map")
    }
    get("/") {
      val jsUrl = call.parameters["jsUrl"] ?: "/static/js/phoehnlix-frontend.js"
      call.respondHtml {
        head {
          meta(name = "viewport", content = "minimum-scale=1, initial-scale=1, width=device-width")
          link(rel = "stylesheet", href = "https://fonts.googleapis.com/css?family=Roboto:300,400,500,700&display=swap")
          link(rel = "stylesheet", href = "https://fonts.googleapis.com/css?family=Days+One&display=swap")
          link(rel = "stylesheet", href = "https://fonts.googleapis.com/icon?family=Material+Icons")
        }
        body {
          div {
            id = "root"
            attributes["data-api-url"] = apiBaseUrl
            attributes["data-google-clientId"] = googleClientId
          }
          script(src = jsUrl) {}
        }
      }
    }
    get("/{path...}") {
      val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
      call.respondRedirect {
        encodedPath = "/"
        fragment = "!/$path"
      }
    }
  }
}
