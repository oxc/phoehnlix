package de.esotechnik.phoehnlix.frontend

import de.esotechnik.phoehnlix.api.client.ApiClient
import de.esotechnik.phoehnlix.api.invoke
import de.esotechnik.phoehnlix.api.model.OAuth2Token
import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.OAuthAccessTokenResponse
import io.ktor.auth.OAuthServerSettings
import io.ktor.auth.authenticate
import io.ktor.auth.authentication
import io.ktor.auth.oauth
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.features.CallLogging
import io.ktor.features.origin
import io.ktor.html.respondHtml
import io.ktor.http.HttpMethod
import io.ktor.http.content.resource
import io.ktor.http.content.static
import io.ktor.http.content.staticBasePackage
import io.ktor.request.host
import io.ktor.request.port
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h2
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.pre
import kotlinx.html.script
import kotlin.collections.listOf
import kotlin.collections.set

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@KtorExperimentalAPI
@JvmOverloads
fun Application.module(testing: Boolean = false) {
  install(CallLogging)

  setupOAuth()

  val apiBaseUrl = environment.config.property("phoehnlix.apiUrl").getString()
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
          }
          script(src = jsUrl) {}
        }
      }
    }
    authenticate("google-oauth") {
      route("/login/google") {
        handle {
          val principal = call.authentication.principal<OAuthAccessTokenResponse.OAuth2>()
            ?: error("No principal")

          // we don't have an apiToken yet, but we want to get one :)
          val apiClient = ApiClient(Apache, apiBaseUrl, apiToken = null)
          val apiToken = apiClient.login.google(OAuth2Token(principal))

          call.respondHtml {
            body {
              div {
                id = "root"
                //attributes["data-oauth-response"] =
                h2 { +"Principal:" }
                pre {
                  +principal.toString()
                }
                h2 { +"API Token:" }
                pre {
                  +apiToken.toString()
                }
              }
            }
          }
        }
      }
    }
  }
}

@KtorExperimentalAPI
fun Application.setupOAuth() {
  val config = environment.config.config("phoehnlix.googleOAuth")

  val googleOauthProvider = OAuthServerSettings.OAuth2ServerSettings(
    name = "google",
    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
    accessTokenUrl = "https://oauth2.googleapis.com/token",
    requestMethod = HttpMethod.Post,

    clientId = config.property("clientId").getString(),
    clientSecret = config.property("clientSecret").getString(),
    defaultScopes = listOf(
      "profile",
      "openid",
      "https://www.googleapis.com/auth/fitness.body.write",
      "https://www.googleapis.com/auth/fitness.body.read"
    )
  )

  install(Authentication) {
    oauth("google-oauth") {
      client = HttpClient(Apache)
      providerLookup = { googleOauthProvider }
      urlProvider = {
        redirectUrl("/login")
      }
    }
  }
}

private fun ApplicationCall.redirectUrl(path: String): String {
  val defaultPort = if (request.origin.scheme == "http") 80 else 443
  val hostPort = request.host() + request.port().let { port -> if (port == defaultPort) "" else ":$port" }
  val protocol = request.origin.scheme
  return "$protocol://$hostPort$path"
}
