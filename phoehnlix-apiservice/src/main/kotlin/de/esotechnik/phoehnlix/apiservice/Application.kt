package de.esotechnik.phoehnlix.apiservice

import de.esotechnik.phoehnlix.apiservice.auth.SimpleJWT
import de.esotechnik.phoehnlix.apiservice.data.setupDatabase
import de.esotechnik.phoehnlix.ktor.setupForwardedFor
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.StatusPages
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.serialization.json
import kotlinx.serialization.json.Json
import org.apache.commons.codec.binary.Base64
import java.nio.charset.StandardCharsets
import java.security.SecureRandom

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {
  setupForwardedFor()
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

    val corsHosts = environment.config.propertyOrNull("phoehnlix.corsHosts")?.getList() ?: run {
      error("No CORS hosts configured. Please specify your domains as phoehnlix.corsHosts")
    }
    corsHosts.forEach {
      if (it.startsWith("http://") || it.startsWith("https://")) {
        val (scheme, host) = it.split("://")
        host(host, schemes = listOf(scheme))
      } else {
        host(it)
      }
    }
  }
  install(ContentNegotiation) {
    json(
      json = Json {
        prettyPrint = true
      }
    )
  }
  install(StatusPages) {
    exception<UnauthorizedException> { exception ->
      call.respond(HttpStatusCode.Unauthorized, mapOf("error" to exception.message))
    }
  }

  val secret = environment.config.propertyOrNull("phoehnlix.jwtSecret")?.getString() ?: run {
    val suggestedSecret = with(SecureRandom.getInstanceStrong()) {
      val secretBytes = ByteArray(32)
      nextBytes(secretBytes)
      Base64.encodeBase64URLSafeString(secretBytes)
    }

    error("No JWT secret configured. Here is a random value you might use: $suggestedSecret")
  }
  val simpleJwt = SimpleJWT(secret)
  install(Authentication) {
    jwt("phoehnlix-jwt") {
      verifier(simpleJwt.verifier)
      validate {
        with(simpleJwt) {
          it.getUserId()?.let { userId -> UserIdPrincipal(userId) }
        }
      }
    }
  }

  routing {
    route("/api") {
      apiservice(simpleJwt)
    }
  }
}


