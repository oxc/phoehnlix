package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.ktor.setupForwardedFor
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.html.respondHtml
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.http.content.staticBasePackage
import io.ktor.response.respondRedirect
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.util.getValue
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.script
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
      val jsUrl = call.parameters.get("jsUrl") ?: "/static/js/phoehnlix-frontend.js"
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
      val path: String by call.parameters
      call.respondRedirect {
        encodedPath = "/"
        fragment = "!/$path"
      }
    }
  }
}
